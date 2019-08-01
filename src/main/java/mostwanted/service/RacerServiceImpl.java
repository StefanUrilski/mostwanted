package mostwanted.service;

import com.google.gson.Gson;
import mostwanted.common.Constants;
import mostwanted.domain.dtos.RacerImportDto;
import mostwanted.domain.entities.Car;
import mostwanted.domain.entities.Racer;
import mostwanted.domain.entities.Town;
import mostwanted.repository.RacerRepository;
import mostwanted.repository.TownRepository;
import mostwanted.util.FileUtil;
import mostwanted.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RacerServiceImpl implements RacerService {

    private final static String RACERS_JSON_FILE_PATH = System.getProperty("user.dir")
            + "/src/main/resources/files/racers.json";

    private final static String NEW_LINE = System.lineSeparator();

    private final Gson gson;
    private final FileUtil fileUtil;
    private final ModelMapper modelMapper;
    private final TownRepository townRepository;
    private final RacerRepository racerRepository;

    @Autowired
    public RacerServiceImpl(Gson gson,
                            FileUtil fileUtil,
                            ModelMapper modelMapper,
                            TownRepository townRepository,
                            RacerRepository racerRepository) {
        this.gson = gson;
        this.fileUtil = fileUtil;
        this.modelMapper = modelMapper;
        this.townRepository = townRepository;
        this.racerRepository = racerRepository;
    }

    @Override
    public Boolean racersAreImported() {
        return racerRepository.count() != 0;
    }

    @Override
    public String readRacersJsonFile() {
        return fileUtil.readFile(RACERS_JSON_FILE_PATH);
    }

    @Override
    public String importRacers(String racersFileContent) {
        List<String> importInfo = new ArrayList<>();

        RacerImportDto[] racerImportDto = gson.fromJson(racersFileContent, RacerImportDto[].class);

        for (RacerImportDto racerDto : racerImportDto) {

            Racer racer = racerRepository.findByName(racerDto.getName()).orElse(null);

            if (racer != null) {
                importInfo.add(Constants.DUPLICATE_DATA_MESSAGE);
                continue;
            }

            Town town = townRepository.findByName(racerDto.getHomeTown()).orElse(null);

            if (town == null) {
                importInfo.add(Constants.INCORRECT_DATA_MESSAGE);
                continue;
            }

            racer = modelMapper.map(racerDto, Racer.class);

            racer.setHomeTown(town);
            racerRepository.saveAndFlush(racer);
            importInfo.add(String.format(Constants.SUCCESSFUL_IMPORT_MESSAGE,
                    racer.getClass().getSimpleName(),
                    racer.getName())
            );
        }

        return String.join(System.lineSeparator(), importInfo);
    }

    @Override
    public String exportRacingCars() {
        List<Racer> racersWithAge = racerRepository.findAllByAgeIsNotNullOrderByCarsDesc();

        StringBuilder racingCarsInfo = new StringBuilder();

        for (Racer racer : racersWithAge) {
            String age = ", Age: " + racer.getAge();
            racingCarsInfo.append("Name: ").append(racer.getName())
                    .append(racer.getAge() == null ? "" : age).append(NEW_LINE);
            racingCarsInfo.append(" Cars: ").append(NEW_LINE);
            racer.getCars().forEach(car ->
                racingCarsInfo.append(String.format("   %s %s %s", car.getBrand(), car.getModel(),
                        car.getYearOfProduction())).append(NEW_LINE)
            );
            racingCarsInfo.append(NEW_LINE);
        }

        return racingCarsInfo.toString();
    }
}
