package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.repositories.actors.ActorRoleRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActorRoleService {

    private final ActorRoleRepository actorRoleRepository;

    public List<ActorRole> getAllActorRoles() {
        return actorRoleRepository.findAll(new Sort(Sort.Direction.ASC, "ord"));
    }

    public ActorRole validate(String prefix, ActorRoleId role) throws DataViolationException {
        if (role.getCode() == null) {
            throw new DataViolationException(prefix + "code", role.getCode());
        }
        Optional<ActorRole> result = actorRoleRepository.findById(role.getCode());
        if (!result.isPresent()) {
            throw new DataViolationException(prefix + "code", role.getCode());
        }
        return result.get();
    }

}
