package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceDetails;

import java.util.List;

public interface MediaSourceCrudService {

    List<MediaSourceDetails> getAllMediaServices();
    MediaSourceDetails getMediaService(String mediaSourceCode);

    MediaSourceDetails registerMediaService(MediaSourceCore mediaSourceCore);
    MediaSourceDetails updateMediaService(MediaSourceCore mediaSourceCore);

    void removeMediaService(String mediaSourceCode);
}
