package eu.sshopencloud.marketplace.domain.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;


interface MediaDataRepository extends JpaRepository<MediaData, UUID> {

    @Query("select m from MediaData m where m.linkCount = 0 and m.touchTimestamp < :retentionTimestamp")
    Stream<MediaData> streamStaleMedia(@Param("retentionTimestamp") LocalDateTime retentionTimestamp);

    @Query("SELECT m FROM MediaData m WHERE m.id IN :listOfMediaIds")
    List<MediaData> finaAllByIds(List<UUID> listOfMediaIds);
}
