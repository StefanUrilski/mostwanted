package mostwanted.service;

import mostwanted.common.Constants;
import mostwanted.domain.dtos.raceentries.RaceEntryImportDto;
import mostwanted.domain.dtos.raceentries.RaceEntryImportRootDto;
import mostwanted.domain.entities.Car;
import mostwanted.domain.entities.RaceEntry;
import mostwanted.domain.entities.Racer;
import mostwanted.repository.CarRepository;
import mostwanted.repository.RaceEntryRepository;
import mostwanted.repository.RacerRepository;
import mostwanted.util.FileUtil;
import mostwanted.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

@Service
public class RaceEntryServiceImpl implements RaceEntryService {

    private final static String RACE_ENTRIES_XML_FILE_PATH = System.getProperty("user.dir") +
            "/src/main/resources/files/race-entries.xml";

    private final FileUtil fileUtil;
    private final XmlParser xmlParser;
    private final ModelMapper modelMapper;
    private final CarRepository carRepository;
    private final RacerRepository racerRepository;
    private final RaceEntryRepository raceEntryRepository;

    @Autowired
    public RaceEntryServiceImpl(FileUtil fileUtil,
                                XmlParser xmlParser,
                                ModelMapper modelMapper,
                                CarRepository carRepository,
                                RacerRepository racerRepository,
                                RaceEntryRepository raceEntryRepository) {
        this.fileUtil = fileUtil;
        this.xmlParser = xmlParser;
        this.modelMapper = modelMapper;
        this.carRepository = carRepository;
        this.racerRepository = racerRepository;
        this.raceEntryRepository = raceEntryRepository;
    }

    @Override
    public Boolean raceEntriesAreImported() {
        return raceEntryRepository.count() != 0;
    }

    @Override
    public String readRaceEntriesXmlFile() {
        return fileUtil.readFile(RACE_ENTRIES_XML_FILE_PATH);
    }

    @Override
    public String importRaceEntries() throws JAXBException {

        List<String> importInfo = new ArrayList<>();

        RaceEntryImportRootDto raceEntryImportRootDto =
                xmlParser.parseXml(RaceEntryImportRootDto.class, RACE_ENTRIES_XML_FILE_PATH);

        for (RaceEntryImportDto raceEntryDto : raceEntryImportRootDto.getRaceEntries()) {
            Racer racer = racerRepository.findByName(raceEntryDto.getRacer()).orElse(null);

            Car car = carRepository.findById(raceEntryDto.getCarId()).orElse(null);

            if (racer == null || car == null) {
                importInfo.add(Constants.INCORRECT_DATA_MESSAGE);
                continue;
            }

            RaceEntry raceEntry = modelMapper.map(raceEntryDto, RaceEntry.class);

            raceEntry.setCar(car);
            raceEntry.setRacer(racer);
            raceEntry.setRace(null);
            raceEntryRepository.saveAndFlush(raceEntry);
            importInfo.add(String.format(Constants.SUCCESSFUL_IMPORT_MESSAGE,
                    raceEntry.getClass().getSimpleName(),
                    raceEntry.getId())
            );
        }

        return String.join(System.lineSeparator(), importInfo);
    }
}
