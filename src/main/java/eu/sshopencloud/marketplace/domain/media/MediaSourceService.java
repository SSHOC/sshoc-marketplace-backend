package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.common.BaseOrderableEntityService;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
class MediaSourceService extends BaseOrderableEntityService<MediaSource, String> implements MediaSourceCrudService {

    private final MediaSourceRepository mediaSourceRepository;


    @Override
    public List<MediaSourceDetails> getAllMediaServices() {
        return loadAllEntries()
                .stream()
                .map(this::toMediaSourceDetails)
                .collect(Collectors.toList());
    }

    @Override
    public MediaSourceDetails getMediaService(String mediaSourceCode) {
        MediaSource mediaSource = loadMediaSource(mediaSourceCode);
        return toMediaSourceDetails(mediaSource);
    }

    @Override
    public MediaSourceDetails registerMediaService(MediaSourceCore mediaSourceCore) {
        String code = mediaSourceCore.getCode();
        if (code == null)
            throw new IllegalArgumentException("Media source's code is not present");

        if (mediaSourceRepository.existsById(code))
            throw new IllegalArgumentException(String.format("Media source with code '%s' is already present", code));

        MediaSource mediaSource = new MediaSource(code, mediaSourceCore.getServiceUrl(), mediaSourceCore.getMediaCategory());

        placeEntryAtPosition(mediaSource, mediaSource.getOrd(), true);
        mediaSource = mediaSourceRepository.save(mediaSource);

        return toMediaSourceDetails(mediaSource);
    }

    @Override
    public MediaSourceDetails updateMediaService(MediaSourceCore mediaSourceCore) {
        String code = mediaSourceCore.getCode();
        if (code == null)
            throw new IllegalArgumentException("Media source's code is not present");

        MediaSource mediaSource = loadMediaSource(code);
        mediaSource.setServiceUrl(mediaSourceCore.getServiceUrl());
        mediaSource.setMediaCategory(mediaSource.getMediaCategory());

        placeEntryAtPosition(mediaSource, mediaSourceCore.getOrd(), false);

        return toMediaSourceDetails(mediaSource);
    }

    @Override
    public void removeMediaService(String mediaSourceCode) {
        try {
            mediaSourceRepository.deleteById(mediaSourceCode);
            removeEntryFromPosition(mediaSourceCode);
        }
        catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException(String.format("Media source with code %s not found", mediaSourceCode));
        }
    }

    private MediaSource loadMediaSource(String mediaSourceCode) {
        if (mediaSourceCode == null)
            throw new IllegalArgumentException("Media source's code is not present");

        return mediaSourceRepository.findById(mediaSourceCode)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Media source with code %s not found", mediaSourceCode)));
    }

    private MediaSourceDetails toMediaSourceDetails(MediaSource mediaSource) {
        return MediaSourceDetails.builder()
                .code(mediaSource.getCode())
                .serviceUrl(mediaSource.getServiceUrl())
                .mediaCategory(mediaSource.getMediaCategory())
                .build();
    }

    @Override
    protected JpaRepository<MediaSource, String> getEntityRepository() {
        return mediaSourceRepository;
    }
}
