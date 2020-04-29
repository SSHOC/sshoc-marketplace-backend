package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.tools.PaginatedTools;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.mappers.tools.ToolMapper;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.tools.ToolRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.tools.ToolValidator;
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

    private final ToolValidator toolValidator;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

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
        Tool tool = toolValidator.validate(toolCore, null);
        itemService.updateInfoDates(tool);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        itemService.addInformationContributorToItem(tool, LoggedInUserHolder.getLoggedInUser());

        Item nextVersion = itemService.clearVersionForCreate(tool);
        tool = toolRepository.save(tool);
        itemService.switchVersion(tool, nextVersion);
        indexService.indexItem(tool);

        return itemService.completeItem(ToolMapper.INSTANCE.toDto(tool));
    }

    public ToolDto updateTool(Long id, ToolCore toolCore) {
        if (!toolRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Tool.class.getName() + " with id " + id);
        }
        Tool tool = toolValidator.validate(toolCore, id);
        itemService.updateInfoDates(tool);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        itemService.addInformationContributorToItem(tool, LoggedInUserHolder.getLoggedInUser());

        Item prevVersion = tool.getPrevVersion();
        Item nextVersion = itemService.clearVersionForUpdate(tool);
        tool = toolRepository.save(tool);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.indexItem(tool);

        return itemService.completeItem(ToolMapper.INSTANCE.toDto(tool));
    }

    public void deleteTool(Long id) {
        // TODO don't allow deleting without authentication (in WebSecurityConfig)
        if (!toolRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Tool.class.getName() + " with id " + id);
        }
        Tool tool = toolRepository.getOne(id);
        itemRelatedItemService.deleteRelationsForItem(tool);
        Item prevVersion = tool.getPrevVersion();
        Item nextVersion = itemService.clearVersionForDelete(tool);
        toolRepository.delete(tool);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.removeItem(tool);
    }

}
