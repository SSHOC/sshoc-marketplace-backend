package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceDetails;

import java.util.List;

public interface MediaSourceCrudService {

    List<MediaSourceDetails> getAllMediaSources();
    MediaSourceDetails getMediaSource(String mediaSourceCode);

    MediaSourceDetails registerMediaSource(MediaSourceCore mediaSourceCore);
    MediaSourceDetails updateMediaSource(String mediaSourceCode, MediaSourceCore mediaSourceCore);

    void saveMediaSources(List<MediaSourceCore> mediaSources);
    long countAllMediaSources();

    void removeMediaSource(String mediaSourceCode);
}
