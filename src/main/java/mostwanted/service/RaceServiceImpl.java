package mostwanted.service;

import mostwanted.common.Constants;
import mostwanted.domain.dtos.races.EntryImportDto;
import mostwanted.domain.dtos.races.EntryImportRootDto;
import mostwanted.domain.dtos.races.RaceImportDto;
import mostwanted.domain.dtos.races.RaceImportRootDto;
import mostwanted.domain.entities.*;
import mostwanted.repository.DistrictRepository;
import mostwanted.repository.RaceEntryRepository;
import mostwanted.repository.RaceRepository;
import mostwanted.util.FileUtil;
import mostwanted.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RaceServiceImpl implements RaceService {

    private final static String RACES_XML_FILE_PATH = System.getProperty("user.dir")
            + "/src/main/resources/files/races.xml";

    private final FileUtil fileUtil;
    private final XmlParser xmlParser;
    private final ModelMapper modelMapper;
    private final RaceRepository raceRepository;
    private final DistrictRepository districtRepository;
    private final RaceEntryRepository raceEntryRepository;

    @Autowired
    public RaceServiceImpl(FileUtil fileUtil,
                           XmlParser xmlParser,
                           ModelMapper modelMapper,
                           RaceRepository raceRepository,
                           DistrictRepository districtRepository,
                           RaceEntryRepository raceEntryRepository) {
        this.fileUtil = fileUtil;
        this.xmlParser = xmlParser;
        this.modelMapper = modelMapper;
        this.raceRepository = raceRepository;
        this.districtRepository = districtRepository;
        this.raceEntryRepository = raceEntryRepository;
    }


    @Override
    public Boolean racesAreImported() {
        return raceRepository.count() != 0;
    }

    @Override
    public String readRacesXmlFile() {
        return fileUtil.readFile(RACES_XML_FILE_PATH);
    }

    @Override
    public String importRaces() throws JAXBException {
        List<String> importInfo = new ArrayList<>();

        RaceImportRootDto raceImportRootDto =
                xmlParser.parseXml(RaceImportRootDto.class, RACES_XML_FILE_PATH);

        for (RaceImportDto raceDto : raceImportRootDto.getRace()) {
            District district = districtRepository.findByName(raceDto.getDistrict()).orElse(null);

            if (district == null) {
                importInfo.add(Constants.INCORRECT_DATA_MESSAGE);
                continue;
            }


            Race race = modelMapper.map(raceDto, Race.class);

            Set<RaceEntry> raceEntries = new HashSet<>();

            for (EntryImportRootDto entryRoot : raceDto.getEntries()) {
                for (EntryImportDto entry : entryRoot.getEntries()) {
                    RaceEntry raceEntry = raceEntryRepository.findById(entry.getId()).orElse(null);

                    if (raceEntry != null) {
                        raceEntries.add(raceEntry);
                    }
                }
            }

            race.setEntries(raceEntries);
            race.setDistrict(district);

            raceRepository.saveAndFlush(race);
            importInfo.add(String.format(Constants.SUCCESSFUL_IMPORT_MESSAGE,
                    race.getClass().getSimpleName(),
                    race.getId())
            );
        }

        return String.join(System.lineSeparator(), importInfo);
    }
}