package eu.sshopencloud.marketplace.dto.collections;

import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionThumbnailDto {
    private UUID mediaId;
    private String caption;
    private MediaLocation location;
    private String filename;
    private String mimeType;

}
