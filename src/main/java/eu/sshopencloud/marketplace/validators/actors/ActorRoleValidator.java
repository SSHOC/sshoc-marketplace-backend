package eu.sshopencloud.marketplace.validators.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.repositories.actors.ActorRoleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ActorRoleValidator {

    private final ActorRoleRepository actorRoleRepository;

    public ActorRole validate(ActorRoleId roleId, Errors errors) {
        if (StringUtils.isBlank(roleId.getCode())) {
            errors.rejectValue("code", "field.required", "Actor role code is required.");
            return null;
        }
        Optional<ActorRole> roleHolder = actorRoleRepository.findById(roleId.getCode());
        if (!roleHolder.isPresent()) {
            errors.rejectValue("code", "field.notExist", "Actor role does not exist.");
            return null;
        } else {
            return roleHolder.get();
        }
    }

}
