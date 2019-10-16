package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.model.tools.ToolType;
import eu.sshopencloud.marketplace.repositories.tools.ToolTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolTypeService {

    private final ToolTypeRepository toolTypeRepository;

    public List<ToolType> getAllToolTypes() {
        return toolTypeRepository.findAll(new Sort(Sort.Direction.ASC, "ord"));
    }

}
