package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.tools.ToolTypeId;
import eu.sshopencloud.marketplace.repositories.tools.ToolTypeRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CategoryService {

    // TODO change logic when toolTypes, trainingMaterialTypes, dataTypes will be one vocabulary with hierarchy

    private final ToolTypeRepository toolTypeRepository;

    public String getToolCategoryCode(ToolTypeId toolType) throws DataViolationException {
        if (toolType == null || toolType.getCode() == null) {
            throw new DataViolationException("toolType.code", "null");
        }
        if (!toolTypeRepository.existsById(toolType.getCode())) {
            throw new DataViolationException("toolType.code", toolType.getCode());
        }
        return toolType.getCode();
    }

}
