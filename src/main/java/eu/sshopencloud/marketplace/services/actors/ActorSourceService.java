package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorSourceCore;
import eu.sshopencloud.marketplace.dto.actors.ActorSourceDto;
import eu.sshopencloud.marketplace.mappers.actors.ActorSourceMapper;
import eu.sshopencloud.marketplace.model.actors.ActorSource;
import eu.sshopencloud.marketplace.repositories.actors.ActorSourceRepository;
import eu.sshopencloud.marketplace.domain.common.BaseOrderableEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class ActorSourceService extends BaseOrderableEntityService<ActorSource, String> {

    private final ActorSourceRepository actorSourceRepository;


    public List<ActorSourceDto> getAllActorSources() {
        return ActorSourceMapper.INSTANCE.toDto(loadAllEntries());
    }

    public ActorSourceDto getActorSource(String code) {
        ActorSource actorSource = loadActorSource(code)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Actor source with code %s not found", code)));

        return ActorSourceMapper.INSTANCE.toDto(actorSource);
    }

    public Optional<ActorSource> loadActorSource(String code) {
        return actorSourceRepository.findById(code);
    }

    public ActorSourceDto createActorSource(ActorSourceCore actorSourceCore) {
        ActorSource actorSource = new ActorSource(
                actorSourceCore.getCode(), actorSourceCore.getLabel()
        );

        if (actorSource.getCode() == null)
            throw new IllegalArgumentException("Actor's source's code is required.");

        placeEntryAtPosition(actorSource, actorSourceCore.getOrd(), true);
        actorSource = actorSourceRepository.save(actorSource);

        return ActorSourceMapper.INSTANCE.toDto(actorSource);
    }

    public ActorSourceDto updateActorSource(String code, ActorSourceCore actorSourceCore) {
        ActorSource actorSource = actorSourceRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Actor source with code %s not found", code)));

        actorSource.setLabel(actorSourceCore.getLabel());
        placeEntryAtPosition(actorSource, actorSourceCore.getOrd(), false);

        return ActorSourceMapper.INSTANCE.toDto(actorSource);
    }

    public void deleteActorSource(String code) {
        if (actorSourceRepository.isActorSourceInUse(code))
            throw new IllegalArgumentException(String.format("Actor source %s is in use so it cannot be removed anymore", code));

        try {
            actorSourceRepository.deleteById(code);
            removeEntryFromPosition(code);
        }
        catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException(String.format("Actor source with code %s not found", code));
        }
    }

    @Override
    protected JpaRepository<ActorSource, String> getEntityRepository() {
        return actorSourceRepository;
    }
}
