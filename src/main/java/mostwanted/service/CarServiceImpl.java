package mostwanted.service;

import com.google.gson.Gson;
import mostwanted.common.Constants;
import mostwanted.domain.dtos.CarImportDto;
import mostwanted.domain.dtos.RacerImportDto;
import mostwanted.domain.entities.Car;
import mostwanted.domain.entities.Racer;
import mostwanted.domain.entities.Town;
import mostwanted.repository.CarRepository;
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
public class CarServiceImpl implements CarService{

    private final static String CARS_JSON_FILE_PATH = System.getProperty("user.dir")+"/src/main/resources/files/cars.json";

    private final Gson gson;
    private final FileUtil fileUtil;
    private final ModelMapper modelMapper;
    private final CarRepository carRepository;
    private final RacerRepository racerRepository;

    @Autowired
    public CarServiceImpl(Gson gson,
                          FileUtil fileUtil,
                          ModelMapper modelMapper,
                          CarRepository carRepository,
                          RacerRepository racerRepository) {
        this.gson = gson;
        this.fileUtil = fileUtil;
        this.modelMapper = modelMapper;
        this.carRepository = carRepository;
        this.racerRepository = racerRepository;
    }

    @Override
    public Boolean carsAreImported() {
        return carRepository.count() != 0;
    }

    @Override
    public String readCarsJsonFile() {
        return fileUtil.readFile(CARS_JSON_FILE_PATH);
    }

    @Override
    public String importCars(String carsFileContent) {
        List<String> importInfo = new ArrayList<>();

        CarImportDto[] carImportDto = gson.fromJson(carsFileContent, CarImportDto[].class);

        for (CarImportDto carDto : carImportDto) {
            Racer racer = racerRepository.findByName(carDto.getRacerName()).orElse(null);

            if (racer == null) {
                importInfo.add(Constants.INCORRECT_DATA_MESSAGE);
                continue;
            }

            Car car = modelMapper.map(carDto, Car.class);

            String carInfo = String.format("%s %s @ %s",
                    car.getBrand(),
                    car.getModel(),
                    car.getYearOfProduction()
            );

            car.setRacer(racer);
            carRepository.saveAndFlush(car);
            importInfo.add(String.format(Constants.SUCCESSFUL_IMPORT_MESSAGE,
                    car.getClass().getSimpleName(),
                    carInfo
            ));
        }

        return String.join(System.lineSeparator(), importInfo);
    }
}
