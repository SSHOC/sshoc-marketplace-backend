package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.tools.PaginatedTools;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.mappers.tools.ToolMapper;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.items.ToolRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.tools.ToolFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ToolService {
    private final ToolRepository toolRepository;
    private final ToolFactory toolFactory;
    private final ItemCrudService itemService;
    private final IndexService indexService;


    public PaginatedTools getTools(PageCoords pageCoords) {
        Page<Tool> toolsPage = toolRepository.findAll(PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));
        List<ToolDto> tools = toolsPage.stream().map(ToolMapper.INSTANCE::toDto)
                .map(tool -> {
                    itemService.completeItem(tool);
                    return tool;
                })
                .collect(Collectors.toList());

        return PaginatedTools.builder().tools(tools)
                .count(toolsPage.getContent().size()).hits(toolsPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(toolsPage.getTotalPages())
                .build();
    }

    public ToolDto getTool(Long id) {
        Tool tool = toolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Tool.class.getName() + " with id " + id));
        return itemService.completeItem(ToolMapper.INSTANCE.toDto(tool));
    }

    public ToolDto createTool(ToolCore toolCore) {
        return createNewToolVersion(toolCore, null);
    }

    public ToolDto updateTool(Long id, ToolCore toolCore) {
        if (!toolRepository.existsById(id))
            throw new EntityNotFoundException("Unable to find " + Tool.class.getName() + " with id " + id);

        return createNewToolVersion(toolCore, id);
    }

    private ToolDto createNewToolVersion(ToolCore toolCore, Long prevToolId) {
        Tool prevTool = (prevToolId != null) ? toolRepository.getOne(prevToolId) : null;
        Tool tool = toolFactory.create(toolCore, prevTool);

        tool = toolRepository.save(tool);
        indexService.indexItem(tool);

        return itemService.completeItem(ToolMapper.INSTANCE.toDto(tool));
    }

    public void deleteTool(Long id) {
        if (!toolRepository.existsById(id))
            throw new EntityNotFoundException("Unable to find " + Tool.class.getName() + " with id " + id);

        Tool tool = toolRepository.getOne(id);

        itemService.cleanupItem(tool);
        toolRepository.delete(tool);
        indexService.removeItem(tool);
    }
}
