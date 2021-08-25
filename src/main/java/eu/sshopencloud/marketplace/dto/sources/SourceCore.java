package eu.sshopencloud.marketplace.dto.sources;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class SourceCore {

    @NotNull
    private String label;

    @NotNull
    private String url;

    @NotNull
    private String urlTemplate;

}
