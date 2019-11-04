package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.tools.Software;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.tools.ToolRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import eu.sshopencloud.marketplace.services.items.ItemContributorService;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.licenses.LicenseService;
import eu.sshopencloud.marketplace.services.search.SearchService;
import eu.sshopencloud.marketplace.services.vocabularies.CategoryService;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptDisallowedException;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ToolService {

    private final ToolRepository toolRepository;

    private final CategoryService categoryService;

    private final ItemService itemService;

    private final LicenseService licenseService;

    private final ItemContributorService itemContributorService;

    private final PropertyService propertyService;

    private final ItemRelatedItemService itemRelatedItemService;

    private  final SearchService searchService;

    public PaginatedTools getTools(int page, int perpage) {
        Page<Tool> tools = toolRepository.findAll(PageRequest.of(page - 1, perpage, new Sort(Sort.Direction.ASC, "label")));
        for (Tool tool: tools) {
            tool.setRelatedItems(itemRelatedItemService.getItemRelatedItems(tool.getId()));
            tool.setOlderVersions(itemService.getOlderVersionsOfItem(tool));
            tool.setNewerVersions(itemService.getNewerVersionsOfItem(tool));
            itemService.fillAllowedVocabulariesForPropertyTypes(tool);
        }
        return new PaginatedTools(tools, page, perpage);
    }

    public Tool getTool(Long id) {
        Tool tool = toolRepository.getOne(id);
        toolRepository.flush();
        tool.setRelatedItems(itemRelatedItemService.getItemRelatedItems(id));
        tool.setOlderVersions(itemService.getOlderVersionsOfItem(tool));
        tool.setNewerVersions(itemService.getNewerVersionsOfItem(tool));
        itemService.fillAllowedVocabulariesForPropertyTypes(tool);
        return tool;
    }

    private Tool validate(ToolCore newTool, String toolTypeCode) throws DataViolationException, ConceptDisallowedException {
        Tool result = createToolEntity(toolTypeCode);
        result.setCategory(ItemCategory.TOOL);
        if (StringUtils.isBlank(newTool.getLabel())) {
            throw new DataViolationException("label", newTool.getLabel());
        }
        result.setLabel(newTool.getLabel());
        result.setVersion(newTool.getVersion());
        if (StringUtils.isBlank(newTool.getDescription())) {
            throw new DataViolationException("description", newTool.getDescription());
        }
        result.setDescription(newTool.getDescription());
        result.setLicenses(licenseService.validate("licenses", newTool.getLicenses()));
        result.setContributors(itemContributorService.validate("contributors", newTool.getContributors(), result));
        result.setProperties(propertyService.validate("properties", newTool.getProperties()));
        result.setAccessibleAt(newTool.getAccessibleAt());
        if (newTool.getPrevVersionId() != null) {
            Optional<Tool> prevVersion = toolRepository.findById(newTool.getPrevVersionId());
            if (!prevVersion.isPresent()) {
                throw new DataViolationException("prevVersionId", newTool.getDescription());
            }
            result.setPrevVersion(prevVersion.get());
        }
        return result;
    }

    private Tool createToolEntity(String toolTypeCode) {
        switch (toolTypeCode) {
            case "software":
                return new Software();
            case "service":
                return new eu.sshopencloud.marketplace.model.tools.Service();
            default:
                return null; // validation is done earlier
        }
    }

    public Tool createTool(ToolCore newTool) throws DataViolationException, ConceptDisallowedException {
        String toolTypeCode = categoryService.getToolCategoryCode(newTool.getToolType());
        Tool tool = validate(newTool, toolTypeCode);
        // TODO change previous versions by older and newer versions
        tool = toolRepository.save(tool);
        if (itemService.isNewestVersion(tool)) {
            if (tool.getPrevVersion() != null) {
                searchService.removeItem(tool.getPrevVersion());
            }
            searchService.indexItem(tool);
        }
        return tool;
    }

    public Tool updateTool(Long id, Tool newTool) {
        // TODO check ID
        newTool.setId(id);
        Tool tool = toolRepository.save(newTool);
        // TODO index in SOLR
        return tool;
    }

    public void deleteTool(Long id) {
        Tool tool = toolRepository.getOne(id);
        itemRelatedItemService.deleteRelationsForItem(tool);
        itemService.switchVersionForDelete(tool);
        toolRepository.delete(tool);
    }

}
