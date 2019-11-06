package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemComment;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import eu.sshopencloud.marketplace.model.tools.Software;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToolService {

    private final ToolRepository toolRepository;

    private final CategoryService categoryService;

    private final ItemService itemService;

    private final LicenseService licenseService;

    private final ItemContributorService itemContributorService;

    private final PropertyService propertyService;

    private final ItemRelatedItemService itemRelatedItemService;

    private final SearchService searchService;

    private final UserRepository userRepository;

    public PaginatedTools getTools(int page, int perpage) {
        Page<Tool> tools = toolRepository.findAll(PageRequest.of(page - 1, perpage, new Sort(Sort.Direction.ASC, "label")));
        for (Tool tool: tools) {
            tool = complete(tool);
        }
        return new PaginatedTools(tools, page, perpage);
    }

    public Tool getTool(Long id) {
        Tool tool = toolRepository.getOne(id);
        tool = complete(tool);
        return tool;
    }

    private Tool complete(Tool tool) {
        tool.setRelatedItems(itemRelatedItemService.getItemRelatedItems(tool.getId()));
        tool.setOlderVersions(itemService.getOlderVersionsOfItem(tool));
        tool.setNewerVersions(itemService.getNewerVersionsOfItem(tool));
        itemService.fillAllowedVocabulariesForPropertyTypes(tool);
        return tool;
    }

    private Tool validate(ToolCore newTool, Long toolId, String toolTypeCode) throws DataViolationException, ConceptDisallowedException {
        Tool result = createOrGetTool(toolId, toolTypeCode);
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
        if (result.getLicenses() != null) {
            result.getLicenses().clear();
            result.getLicenses().addAll(licenseService.validate("licenses", newTool.getLicenses()));
        } else {
            result.setLicenses(licenseService.validate("licenses", newTool.getLicenses()));
        }
        if (result.getContributors() != null) {
            result.getContributors().clear();
            result.getContributors().addAll(itemContributorService.validate("contributors", newTool.getContributors(), result));
        } else {
            result.setContributors(itemContributorService.validate("contributors", newTool.getContributors(), result));
        }
        if (result.getProperties() != null) {
            result.getProperties().clear();
            result.getProperties().addAll(propertyService.validate("properties", newTool.getProperties()));
        } else {
            result.setProperties(propertyService.validate("properties", newTool.getProperties()));
        }
        result.setAccessibleAt(newTool.getAccessibleAt());
        if (newTool.getPrevVersionId() != null) {
            Optional<Tool> prevVersion = toolRepository.findById(newTool.getPrevVersionId());
            if (!prevVersion.isPresent()) {
                throw new DataViolationException("prevVersionId", newTool.getPrevVersionId());
            }
            if (toolId != null) {
                if (result.getId().equals(newTool.getPrevVersionId())) {
                    throw new DataViolationException("prevVersionId", newTool.getPrevVersionId());
                }
            }
            // switch version before assigning the new one
            if (toolId == null) {
                itemService.switchVersionForCreate(result);
            } else {
                itemService.switchVersionForUpdate(result);
            }
            result.setPrevVersion(prevVersion.get());
        }
        return result;
    }

    private Tool createOrGetTool(Long toolId, String toolTypeCode) {
        if (toolId != null) {
            return toolRepository.getOne(toolId);
        }
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
        // TODO move validation to the validate method (when vocabularies are refactored)
        String toolTypeCode = categoryService.getToolCategoryCode(newTool.getToolType());
        Tool tool = validate(newTool, null, toolTypeCode);
        ZonedDateTime now = ZonedDateTime.now();
        tool.setLastInfoUpdate(now);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        log.debug(authentication.toString());
        if (! (authentication instanceof AnonymousAuthenticationToken)) {
            User user = userRepository.findUserByUsername(authentication.getName());
            List<User> informationContributors = new ArrayList<User>();
            informationContributors.add(user);
            tool.setInformationContributors(informationContributors);
        }

        tool = saveTool(tool);
        return tool;
    }

    public Tool updateTool(Long id, ToolCore newTool) throws DataViolationException, ConceptDisallowedException, DisallowedToolTypeChangeException {
        if (!toolRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Tool.class.getName() + " with id " + id);
        }
        Tool currentTool = toolRepository.getOne(id);
        // change between software and service is not allowed - TODO change into field and allow (add validations in validate method)
        String toolTypeCode = currentTool.getToolType().getCode();
        if (newTool.getToolType() != null) {
            if (!toolTypeCode.equals(newTool.getToolType().getCode())) {
                throw new DisallowedToolTypeChangeException(toolTypeCode, newTool.getToolType().getCode());
            }
        }
        Tool tool = validate(newTool, id, toolTypeCode);
        ZonedDateTime now = ZonedDateTime.now();
        tool.setLastInfoUpdate(now);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        log.debug(authentication.toString());
        if (! (authentication instanceof AnonymousAuthenticationToken)) {
            User user = userRepository.findUserByUsername(authentication.getName());
            if (tool.getInformationContributors() != null) {
                if (!tool.getInformationContributors().contains(user)) {
                    tool.getInformationContributors().add(user);
                }
            } else {
                List<User> informationContributors = new ArrayList<User>();
                informationContributors.add(user);
                tool.setInformationContributors(informationContributors);
            }
        }

        tool = saveTool(tool);
        return tool;
    }

    private Tool saveTool(Tool tool) {
        tool = toolRepository.save(tool);
        if (itemService.isNewestVersion(tool)) {
            if (tool.getPrevVersion() != null) {
                searchService.removeItem(tool.getPrevVersion());
            }
            searchService.indexItem(tool);
        }
        tool = complete(tool);
        return tool;
    }

    public void deleteTool(Long id) {
        // TODO don't allow deleting without authentication (in WebSecurityConfig)
        if (!toolRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Tool.class.getName() + " with id " + id);
        }
        Tool tool = toolRepository.getOne(id);
        itemRelatedItemService.deleteRelationsForItem(tool);
        itemService.switchVersionForDelete(tool);
        toolRepository.delete(tool);
    }

}
