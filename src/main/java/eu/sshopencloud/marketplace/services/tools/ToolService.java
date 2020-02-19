package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.repositories.tools.ToolRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import eu.sshopencloud.marketplace.services.items.ItemContributorService;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.licenses.LicenseService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.text.MarkdownConverter;
import eu.sshopencloud.marketplace.services.vocabularies.*;
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
import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ToolService {

    private final ToolRepository toolRepository;

    private final ItemService itemService;

    private final LicenseService licenseService;

    private final ItemContributorService itemContributorService;

    private final PropertyService propertyService;

    private final ItemRelatedItemService itemRelatedItemService;

    private final IndexService indexService;

    private final UserRepository userRepository;

    public PaginatedTools getTools(int page, int perpage) {
        Page<Tool> tools = toolRepository.findAll(PageRequest.of(page - 1, perpage, new Sort(Sort.Direction.ASC, "label")));
        for (Tool tool: tools) {
            complete(tool);
        }

        return PaginatedTools.builder().tools(tools.getContent())
                .count(tools.getContent().size()).hits(tools.getTotalElements()).page(page).perpage(perpage).pages(tools.getTotalPages())
                .build();
    }

    public Tool getTool(Long id) {
        Optional<Tool> tool = toolRepository.findById(id);
        if (!tool.isPresent()) {
            throw new EntityNotFoundException("Unable to find " + Tool.class.getName() + " with id " + id);
        }
        return complete(tool.get());
    }

    private Tool complete(Tool tool) {
        tool.setRelatedItems(itemRelatedItemService.getItemRelatedItems(tool.getId()));
        tool.setOlderVersions(itemService.getOlderVersionsOfItem(tool));
        tool.setNewerVersions(itemService.getNewerVersionsOfItem(tool));
        itemService.fillAllowedVocabulariesForPropertyTypes(tool);
        return tool;
    }

    private Tool validate(ToolCore newTool, Long toolId)
            throws DataViolationException, ConceptDisallowedException, DisallowedObjectTypeException, TooManyObjectTypesException {
        Tool result = getOrCreateTool(toolId);
        result.setCategory(ItemCategory.TOOL);
        if (StringUtils.isBlank(newTool.getLabel())) {
            throw new DataViolationException("label", newTool.getLabel());
        }
        result.setLabel(newTool.getLabel());
        result.setVersion(newTool.getVersion());
        if (StringUtils.isBlank(newTool.getDescription())) {
            throw new DataViolationException("description", newTool.getDescription());
        }
        result.setDescription(MarkdownConverter.convertHtmlToMarkdown(newTool.getDescription()));
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
            result.getProperties().addAll(propertyService.validate(ItemCategory.TOOL, "properties", newTool.getProperties(), result));
        } else {
            result.setProperties(propertyService.validate(ItemCategory.TOOL, "properties", newTool.getProperties(), result));
        }

        result.setAccessibleAt(newTool.getAccessibleAt());
        result.setRepository(newTool.getRepository());
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
            result.setNewPrevVersion(prevVersion.get());
        }
        return result;
    }

    private Tool getOrCreateTool(Long toolId) {
        if (toolId != null) {
            return toolRepository.getOne(toolId);
        } else {
            return new Tool();
        }
    }

    public Tool createTool(ToolCore newTool)
            throws DataViolationException, ConceptDisallowedException, DisallowedObjectTypeException, TooManyObjectTypesException {
        Tool tool = validate(newTool, null);
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

        Item nextVersion = itemService.clearVersionForCreate(tool);
        tool = toolRepository.save(tool);
        itemService.switchVersion(tool, nextVersion);
        indexService.indexItem(tool);
        return complete(tool);
    }

    public Tool updateTool(Long id, ToolCore newTool)
            throws DataViolationException, ConceptDisallowedException, DisallowedObjectTypeException, TooManyObjectTypesException {
        if (!toolRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Tool.class.getName() + " with id " + id);
        }
        Tool tool = validate(newTool, id);
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

        Item prevVersion = tool.getPrevVersion();
        Item nextVersion = itemService.clearVersionForUpdate(tool);
        tool = toolRepository.save(tool);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.indexItem(tool);
        return complete(tool);
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
