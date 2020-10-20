package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.publications.Publication;
import org.springframework.stereotype.Repository;


@Repository
public interface PublicationRepository  extends ItemVersionRepository<Publication> {
}
