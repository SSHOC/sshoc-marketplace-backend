package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.common.BaseOrderableEntityService;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MediaSourceService extends BaseOrderableEntityService<MediaSource, String> {

    private final MediaSourceRepository mediaSourceRepository;


    public Optional<MediaSource> resolveMediaSource(URL mediaUrl) {
        return mediaSourceRepository.findAll()
                .stream()
                .filter(mediaSource -> checkMediaSourceMatchesUrl(mediaSource, mediaUrl))
                .findFirst();
    }

    private boolean checkMediaSourceMatchesUrl(MediaSource mediaSource, URL mediaUrl) {
        URL serviceUrl = mediaSource.getServiceUrl();
        return mediaUrl.getPath().startsWith(serviceUrl.getPath());
    }

    public List<MediaSourceDto> getAllMediaSources() {
        return loadAllEntries()
                .stream()
                .map(this::toMediaSourceDetails)
                .collect(Collectors.toList());
    }

    public MediaSourceDto getMediaSource(String mediaSourceCode) {
        MediaSource mediaSource = loadMediaSource(mediaSourceCode);
        return toMediaSourceDetails(mediaSource);
    }

    public MediaSourceDto registerMediaSource(MediaSourceCore mediaSourceCore) {
        String code = mediaSourceCore.getCode();
        if (code == null)
            throw new IllegalArgumentException("Media source's code is not present");

        if (mediaSourceRepository.existsById(code))
            throw new IllegalArgumentException(String.format("Media source with code '%s' is already present", code));

        URL serviceUrl = parseServiceUrl(mediaSourceCore.getServiceUrl());
        MediaSource mediaSource = new MediaSource(code, serviceUrl, mediaSourceCore.getMediaCategory());

        placeEntryAtPosition(mediaSource, mediaSourceCore.getOrd(), true);
        mediaSource = mediaSourceRepository.save(mediaSource);

        return toMediaSourceDetails(mediaSource);
    }

    public MediaSourceDto updateMediaSource(String mediaSourceCode, MediaSourceCore mediaSourceCore) {
        String code = mediaSourceCore.getCode();
        if (code == null)
            throw new IllegalArgumentException("Media source's code is not present");

        MediaSource mediaSource = loadMediaSource(code);
        URL serviceUrl = parseServiceUrl(mediaSourceCore.getServiceUrl());

        mediaSource.setServiceUrl(serviceUrl);
        mediaSource.setMediaCategory(mediaSourceCore.getMediaCategory());

        placeEntryAtPosition(mediaSource, mediaSourceCore.getOrd(), false);
        mediaSource = mediaSourceRepository.save(mediaSource);

        return toMediaSourceDetails(mediaSource);
    }

    private URL parseServiceUrl(String serviceUrl) {
        try {
            return new URL(serviceUrl);
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Invalid url: %s", serviceUrl), e);
        }
    }

    public void removeMediaSource(String mediaSourceCode) {
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

    private MediaSourceDto toMediaSourceDetails(MediaSource mediaSource) {
        return MediaSourceDto.builder()
                .code(mediaSource.getCode())
                .serviceUrl(mediaSource.getServiceUrl().toString())
                .mediaCategory(mediaSource.getMediaCategory())
                .ord(mediaSource.getOrd())
                .build();
    }

    @Override
    protected JpaRepository<MediaSource, String> getEntityRepository() {
        return mediaSourceRepository;
    }

    public void saveMediaSources(List<MediaSourceCore> mediaSources) {
        mediaSources.forEach(this::registerMediaSource);
    }

    public long countAllMediaSources() {
        return mediaSourceRepository.count();
    }

}
