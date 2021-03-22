package eu.sshopencloud.marketplace.domain.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;


interface MediaUploadRepository extends JpaRepository<MediaUpload, Long> {

    Optional<MediaUpload> findByMediaId(UUID mediaId);

    Stream<MediaUpload> findAllByUpdatedBefore(LocalDateTime retentionTimestamp);
}
