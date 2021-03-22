package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceDto;

import java.util.List;

public interface MediaSourceCrudService {

    List<MediaSourceDto> getAllMediaSources();
    MediaSourceDto getMediaSource(String mediaSourceCode);

    MediaSourceDto registerMediaSource(MediaSourceCore mediaSourceCore);
    MediaSourceDto updateMediaSource(String mediaSourceCode, MediaSourceCore mediaSourceCore);

    void saveMediaSources(List<MediaSourceCore> mediaSources);
    long countAllMediaSources();

    void removeMediaSource(String mediaSourceCode);
}
