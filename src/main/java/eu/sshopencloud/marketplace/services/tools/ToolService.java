package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.repositories.tools.ToolRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.tools.ToolValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
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

    private final ToolValidator toolValidator;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

    private final IndexService indexService;

    private final UserRepository userRepository;


    public PaginatedTools getTools(int page, int perpage) {
        Page<Tool> tools = toolRepository.findAll(PageRequest.of(page - 1, perpage, Sort.by(Sort.Order.asc("label"))));
        for (Tool tool : tools) {
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


    public Tool createTool(ToolCore toolCore) {
        Tool tool = toolValidator.validate(toolCore, null);
        ZonedDateTime now = ZonedDateTime.now();
        tool.setLastInfoUpdate(now);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug(authentication.toString());
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
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

    public Tool updateTool(Long id, ToolCore toolCore) {
        if (!toolRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Tool.class.getName() + " with id " + id);
        }
        Tool tool = toolValidator.validate(toolCore, id);
        ZonedDateTime now = ZonedDateTime.now();
        tool.setLastInfoUpdate(now);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug(authentication.toString());
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
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
