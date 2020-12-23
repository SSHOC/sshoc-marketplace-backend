package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorRoleCore;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleDto;
import eu.sshopencloud.marketplace.mappers.actors.ActorRoleMapper;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.repositories.actors.ActorRoleRepository;
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
public class ActorRoleService {

    private final ActorRoleRepository actorRoleRepository;

    public List<ActorRoleDto> getAllActorRoles() {
        return ActorRoleMapper.INSTANCE.toDto(loadAllActorRoles());
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
                .ord(actorRoleCore.getOrd())
                .build();

        if (newActorRole.getCode() == null)
            throw new IllegalArgumentException("Actor's role's code is required.");

        validateActorRolePosition(newActorRole.getOrd());

        newActorRole = actorRoleRepository.save(newActorRole);
        reorderActorRoles(newActorRole.getCode(), newActorRole.getOrd());

        return ActorRoleMapper.INSTANCE.toDto(newActorRole);
    }

    public ActorRoleDto updateActorRole(String code, ActorRoleCore actorRoleCore) {
        ActorRole actorRole = actorRoleRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Actor role with code %s not found", code)));

        actorRole.setLabel(actorRoleCore.getLabel());
        actorRole.setOrd(actorRoleCore.getOrd());

        reorderActorRoles(code, actorRole.getOrd());

        return ActorRoleMapper.INSTANCE.toDto(actorRole);
    }

    public void deleteActorRole(String code) {
        if (actorRoleRepository.isActorRoleInUse(code))
            throw new IllegalArgumentException(String.format("Actor role %s is in use so it cannot be removed anymore", code));

        try {
            actorRoleRepository.deleteById(code);
            reorderActorRoles(code, null);
        }
        catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException(String.format("Actor role with code %s not found", code));
        }
    }

    private void reorderActorRoles(String actorRoleCode, Integer actorRoleOrd) {
        int ord = 1;

        for (ActorRole role : loadAllActorRoles()) {
            if (role.getCode().equals(actorRoleCode))
                continue;

            if (actorRoleOrd != null && ord == actorRoleOrd)
                ord++;

            if (role.getOrd() != ord)
                role.setOrd(ord);

            ord++;
        }
    }

    private List<ActorRole> loadAllActorRoles() {
        return actorRoleRepository.findAll(Sort.by(Sort.Order.asc("ord")));
    }

    private void validateActorRolePosition(int ord) {
        long rolesCount = actorRoleRepository.count();

        if (ord < 1 || ord > rolesCount + 1) {
            throw new IllegalArgumentException(
                    String.format("Invalid position index: %d (maximum possible: %d)", ord, rolesCount + 1)
            );
        }
    }
}
