package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorSourceCore;
import eu.sshopencloud.marketplace.dto.actors.ActorSourceDto;
import eu.sshopencloud.marketplace.mappers.actors.ActorSourceMapper;
import eu.sshopencloud.marketplace.model.actors.ActorSource;
import eu.sshopencloud.marketplace.repositories.actors.ActorSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class ActorSourceService {

    private final ActorSourceRepository actorSourceRepository;


    public List<ActorSourceDto> getAllActorSources() {
        return ActorSourceMapper.INSTANCE.toDto(loadAllActorSources());
    }

    public ActorSourceDto getActorSource(String code) {
        ActorSource actorSource = actorSourceRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Actor source with code %s not found", code)));

        return ActorSourceMapper.INSTANCE.toDto(actorSource);
    }

    public ActorSourceDto createActorSource(ActorSourceCore actorSourceCore) {
        ActorSource actorSource = new ActorSource(
                actorSourceCore.getCode(), actorSourceCore.getLabel(), actorSourceCore.getOrd()
        );

        if (actorSource.getCode() == null)
            throw new IllegalArgumentException("Actor's source's code is required.");

        validateActorSourcePosition(actorSource.getOrd());

        actorSource = actorSourceRepository.save(actorSource);
        reorderActorSources(actorSource.getCode(), actorSource.getOrd());

        return ActorSourceMapper.INSTANCE.toDto(actorSource);
    }

    public ActorSourceDto updateActorSource(String code, ActorSourceCore actorSourceCore) {
        ActorSource actorSource = actorSourceRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Actor source with code %s not found", code)));

        actorSource.setLabel(actorSourceCore.getLabel());
        actorSource.setOrd(actorSourceCore.getOrd());

        validateActorSourcePosition(actorSource.getOrd());
        reorderActorSources(code, actorSource.getOrd());

        return ActorSourceMapper.INSTANCE.toDto(actorSource);
    }

    public void deleteActorSource(String code) {
//        if (actorSourceRepository.isActorSourceInUse(code))
//            throw new IllegalArgumentException(String.format("Actor source %s is in use so it cannot be removed anymore", code));

        try {
            actorSourceRepository.deleteById(code);
            reorderActorSources(code, null);
        }
        catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException(String.format("Actor source with code %s not found", code));
        }
    }

    private void reorderActorSources(String actorSourceCode, Integer actorSourceOrd) {
        int ord = 1;

        for (ActorSource actorSource : loadAllActorSources()) {
            if (actorSource.getCode().equals(actorSourceCode))
                continue;

            if (actorSourceOrd != null && ord == actorSourceOrd)
                ord++;

            if (actorSource.getOrd() != ord)
                actorSource.setOrd(ord);

            ord++;
        }
    }

    private List<ActorSource> loadAllActorSources() {
        return actorSourceRepository.findAll(Sort.by(Sort.Order.asc("ord")));
    }

    private void validateActorSourcePosition(int ord) {
        long sourcesCount = actorSourceRepository.count();

        if (ord < 1 || ord > sourcesCount + 1) {
            throw new IllegalArgumentException(
                    String.format("Invalid position index: %d (maximum possible: %d)", ord, sourcesCount + 1)
            );
        }
    }
}
