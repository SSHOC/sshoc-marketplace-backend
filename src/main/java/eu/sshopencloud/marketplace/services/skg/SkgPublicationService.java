package eu.sshopencloud.marketplace.services.skg;

import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.mappers.skg.ItemToSkgMapper;
import eu.sshopencloud.marketplace.model.skg.JsonLdDocument;
import eu.sshopencloud.marketplace.services.items.PublicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class SkgPublicationService {

    private final PublicationService publicationService;
    private final ItemToSkgMapper itemToSkgMapper;

    public SkgPublicationService(PublicationService publicationService, ItemToSkgMapper itemToSkgMapper) {
        this.publicationService = publicationService;
        this.itemToSkgMapper = itemToSkgMapper;
    }

    public JsonLdDocument getPublication(String persistentId) {
        PublicationDto latestPublication = publicationService.getLatestPublication(persistentId, false, true, false);
        return itemToSkgMapper.toSkg(latestPublication);
    }


}
