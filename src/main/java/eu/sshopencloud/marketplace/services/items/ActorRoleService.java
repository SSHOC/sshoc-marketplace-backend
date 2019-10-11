package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.ActorRole;
import eu.sshopencloud.marketplace.repositories.items.ActorRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActorRoleService {

    private final ActorRoleRepository actorRoleRepository;

    public List<ActorRole> getAllActorRoles() {
        return actorRoleRepository.findAll(new Sort(Sort.Direction.ASC, "ord"));
    }

}
