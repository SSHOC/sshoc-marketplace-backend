package eu.sshopencloud.marketplace.services.skg;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.actors.PaginatedActors;
import eu.sshopencloud.marketplace.mappers.skg.ItemToSkgMapper;
import eu.sshopencloud.marketplace.model.skg.JsonLdDocument;
import eu.sshopencloud.marketplace.services.actors.ActorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SkgPersonsService {

    private final ActorService actorService;
    private final ItemToSkgMapper itemToSkgMapper;

    public SkgPersonsService(ActorService actorService, ItemToSkgMapper itemToSkgMapper) {
        this.actorService = actorService;
        this.itemToSkgMapper = itemToSkgMapper;
    }

    public JsonLdDocument getPersons(PageCoords pageData) {

        try {
            PaginatedActors actors = actorService.getActors(pageData);
            return itemToSkgMapper.toSkg(actors);
        } catch (Exception ex) {
            return itemToSkgMapper.empty();
        }
    }

}
