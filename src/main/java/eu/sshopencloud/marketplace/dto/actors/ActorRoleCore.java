package eu.sshopencloud.marketplace.dto.actors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotNull;


@Data
@Builder
@AllArgsConstructor
public class ActorRoleCore {

    private String code;

    @NotNull
    private String label;

    private Integer ord;
}
