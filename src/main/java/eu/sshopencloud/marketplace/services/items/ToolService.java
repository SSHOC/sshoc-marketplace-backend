package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.Tool;
import eu.sshopencloud.marketplace.repositories.items.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolService {

    private final ToolRepository toolRepository;

    public List<Tool> getAllTools() {
        return toolRepository.findAll(new Sort(Sort.Direction.ASC, "label"));
    }

    public Tool getTool(Long id) {
        return toolRepository.getOne(id);
    }

}
