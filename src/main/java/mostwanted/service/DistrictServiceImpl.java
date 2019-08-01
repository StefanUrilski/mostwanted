package mostwanted.service;

import com.google.gson.Gson;
import mostwanted.common.Constants;
import mostwanted.domain.dtos.DistrictImportDto;
import mostwanted.domain.entities.District;
import mostwanted.domain.entities.Town;
import mostwanted.repository.DistrictRepository;
import mostwanted.repository.TownRepository;
import mostwanted.util.FileUtil;
import mostwanted.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DistrictServiceImpl implements DistrictService {

    private final static String DISTRICT_JSON_FILE_PATH = System.getProperty("user.dir") + "/src/main/resources/files/districts.json";

    private final Gson gson;
    private final FileUtil fileUtil;
    private final ModelMapper modelMapper;
    private final ValidationUtil validator;
    private final TownRepository townRepository;
    private final DistrictRepository districtRepository;

    public DistrictServiceImpl(Gson gson,
                               FileUtil fileUtil,
                               ModelMapper modelMapper,
                               ValidationUtil validator,
                               TownRepository townRepository,
                               DistrictRepository districtRepository) {
        this.gson = gson;
        this.fileUtil = fileUtil;
        this.modelMapper = modelMapper;
        this.validator = validator;
        this.townRepository = townRepository;
        this.districtRepository = districtRepository;
    }

    @Override
    public Boolean districtsAreImported() {
        return districtRepository.count() != 0;
    }

    @Override
    public String readDistrictsJsonFile() {
        return fileUtil.readFile(DISTRICT_JSON_FILE_PATH);
    }

    @Override
    public String importDistricts(String districtsFileContent) {
        List<String> importInfo = new ArrayList<>();

        DistrictImportDto[] districtImportDto = gson.fromJson(districtsFileContent, DistrictImportDto[].class);

        for (DistrictImportDto districtDto : districtImportDto) {
            if (!validator.isValid(districtDto)) {
                importInfo.add(Constants.INCORRECT_DATA_MESSAGE);
                continue;
            }

            District district = districtRepository.findByName(districtDto.getName()).orElse(null);

            if (district != null) {
                importInfo.add(Constants.DUPLICATE_DATA_MESSAGE);
                continue;
            }

            district = modelMapper.map(districtDto, District.class);

            Town town = townRepository.findByName(districtDto.getTownName()).orElse(null);

            if (town == null) {
                importInfo.add(Constants.INCORRECT_DATA_MESSAGE);
                continue;
            }

            district.setTown(town);
            districtRepository.saveAndFlush(district);
            importInfo.add(String.format(Constants.SUCCESSFUL_IMPORT_MESSAGE,
                    district.getClass().getSimpleName(),
                    district.getName())
            );
        }

        return String.join(System.lineSeparator(), importInfo);
    }
}
