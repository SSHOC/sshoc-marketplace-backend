package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorRoleCore;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleDto;
import eu.sshopencloud.marketplace.mappers.actors.ActorRoleMapper;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.repositories.actors.ActorRoleRepository;
import eu.sshopencloud.marketplace.domain.common.BaseOrderableEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class ActorRoleService extends BaseOrderableEntityService<ActorRole, String> {

    private final ActorRoleRepository actorRoleRepository;

    public List<ActorRoleDto> getAllActorRoles() {
        return ActorRoleMapper.INSTANCE.toDto(loadAllEntries());
    }

    public ActorRoleDto getActorRole(String code) {
        ActorRole actorRole = actorRoleRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Actor role with code %s not found", code)));

        return ActorRoleMapper.INSTANCE.toDto(actorRole);
    }

    public ActorRoleDto createActorRole(ActorRoleCore actorRoleCore) {
        ActorRole newActorRole = ActorRole.builder()
                .code(actorRoleCore.getCode())
                .label(actorRoleCore.getLabel())
                .build();

        if (newActorRole.getCode() == null)
            throw new IllegalArgumentException("Actor's role's code is required.");

        placeEntryAtPosition(newActorRole, actorRoleCore.getOrd(), true);
        newActorRole = actorRoleRepository.save(newActorRole);

        return ActorRoleMapper.INSTANCE.toDto(newActorRole);
    }

    public ActorRoleDto updateActorRole(String code, ActorRoleCore actorRoleCore) {
        ActorRole actorRole = actorRoleRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Actor role with code %s not found", code)));

        actorRole.setLabel(actorRoleCore.getLabel());
        placeEntryAtPosition(actorRole, actorRoleCore.getOrd(), false);

        return ActorRoleMapper.INSTANCE.toDto(actorRole);
    }

    public void deleteActorRole(String code) {
        if (actorRoleRepository.isActorRoleInUse(code))
            throw new IllegalArgumentException(String.format("Actor role %s is in use so it cannot be removed anymore", code));

        try {
            actorRoleRepository.deleteById(code);
            removeEntryFromPosition(code);
        }
        catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException(String.format("Actor role with code %s not found", code));
        }
    }

    @Override
    protected JpaRepository<ActorRole, String> getEntityRepository() {
        return actorRoleRepository;
    }
}
