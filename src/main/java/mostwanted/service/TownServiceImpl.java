package mostwanted.service;

import com.google.gson.Gson;
import mostwanted.common.Constants;
import mostwanted.domain.dtos.ExportRacingTownsDto;
import mostwanted.domain.dtos.TownImportDto;
import mostwanted.domain.entities.Town;
import mostwanted.repository.TownRepository;
import mostwanted.util.FileUtil;
import mostwanted.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class TownServiceImpl implements TownService{

    private final static String TOWNS_JSON_FILE_PATH = System.getProperty("user.dir") + "/src/main/resources/files/towns.json";

    private final Gson gson;
    private final FileUtil fileUtil;
    private final ModelMapper modelMapper;
    private final ValidationUtil validator;
    private final TownRepository townRepository;

    @Autowired
    public TownServiceImpl(Gson gson, FileUtil fileUtil, ModelMapper modelMapper, ValidationUtil validator, TownRepository townRepository) {
        this.gson = gson;
        this.fileUtil = fileUtil;
        this.modelMapper = modelMapper;
        this.validator = validator;
        this.townRepository = townRepository;
    }

    @Override
    public Boolean townsAreImported() {
        return townRepository.count() != 0;
    }

    @Override
    public String readTownsJsonFile() {
        return fileUtil.readFile(TOWNS_JSON_FILE_PATH);
    }

    @Override
    public String importTowns(String townsFileContent) {
        TownImportDto[] townImportDto = gson.fromJson(townsFileContent, TownImportDto[].class);
        List<String> importInfo = new ArrayList<>();
        for (TownImportDto townDto : townImportDto) {
            if (! validator.isValid(townDto)) {
                importInfo.add(Constants.INCORRECT_DATA_MESSAGE);
                continue;
            }

            Town town = townRepository.findByName(townDto.getName()).orElse(null);

            if (town != null) {
                importInfo.add(Constants.DUPLICATE_DATA_MESSAGE);
                continue;
            }

            town = modelMapper.map(townDto, Town.class);

            townRepository.saveAndFlush(town);
            importInfo.add(String.format(Constants.SUCCESSFUL_IMPORT_MESSAGE,
                    town.getClass().getSimpleName(),
                    town.getName())
            );
        }

        return String.join(System.lineSeparator(), importInfo);
    }

    @Override
    public String exportRacingTowns() {
        StringBuilder townsInfo = new StringBuilder();
        List<Map<String, String>> allRacersWithTowns = townRepository.findAllByCountOfRacers();
        List<ExportRacingTownsDto> townsInfoDto = new ArrayList<>();

        for (Map<String, String> townWithRacers : allRacersWithTowns) {
            ExportRacingTownsDto currTown = modelMapper.map(townWithRacers, ExportRacingTownsDto.class);

            townsInfoDto.add(currTown);
        }

        townsInfoDto.forEach(town -> townsInfo.append(town.toString()));

        return townsInfo.toString();
    }
}
