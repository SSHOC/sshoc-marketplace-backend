package eu.sshopencloud.marketplace.services.skg;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.tools.PaginatedTools;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.mappers.skg.ItemToSkgMapper;
import eu.sshopencloud.marketplace.model.skg.JsonLdDocument;
import eu.sshopencloud.marketplace.services.items.ToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Responsible for generating SKK-IF Research product entities based on the SSHOM entities (tool or service)
 */
@Service
@Slf4j
public class SkgResearchProductService {

    private final ToolService toolService;
    private final ItemToSkgMapper itemToSkgMapper;

    public SkgResearchProductService(ToolService toolService, ItemToSkgMapper itemToSkgMapper) {
        this.toolService = toolService;
        this.itemToSkgMapper = itemToSkgMapper;
    }

    public JsonLdDocument getResearchProduct(String identifier) {
        try {
            String persistentId = IdentifierExtractor.extractPersistentId(identifier);
            ToolDto latestTool = toolService.getLatestTool(persistentId, false, true, false);
            return itemToSkgMapper.toSkg(latestTool);
        } catch (Exception ex) {
            return itemToSkgMapper.empty();
        }
    }

    public JsonLdDocument getResearchProducts(PageCoords pageData) {
        try {
            PaginatedTools tools = toolService.getTools(pageData, true);
            return itemToSkgMapper.toSkg(tools.getTools());
        } catch (Exception ex) {
            return itemToSkgMapper.empty();
        }
    }
}
