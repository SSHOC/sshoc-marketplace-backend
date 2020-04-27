package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorRoleDto;
import eu.sshopencloud.marketplace.mappers.actors.ActorRoleMapper;
import eu.sshopencloud.marketplace.repositories.actors.ActorRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ActorRoleService {

    private final ActorRoleRepository actorRoleRepository;

    public List<ActorRoleDto> getAllActorRoles() {
        return ActorRoleMapper.INSTANCE.toDto(actorRoleRepository.findAll(Sort.by(Sort.Order.asc("ord"))));
    }

}
