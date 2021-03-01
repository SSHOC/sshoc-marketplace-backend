package eu.sshopencloud.marketplace.domain.media;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


interface MediaUploadRepository extends JpaRepository<MediaUpload, Long> {

    Optional<MediaUpload> findByMediaId(UUID mediaId);
}
