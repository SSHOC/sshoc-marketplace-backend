package eu.sshopencloud.marketplace.domain.media;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


interface MediaFileUploadRepository extends JpaRepository<MediaFileUpload, UUID> {
}
