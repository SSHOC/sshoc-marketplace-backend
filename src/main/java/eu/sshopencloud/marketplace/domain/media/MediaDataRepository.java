package eu.sshopencloud.marketplace.domain.media;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface MediaDataRepository extends JpaRepository<MediaData, UUID> {
}
