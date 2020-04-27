package eu.sshopencloud.marketplace.dto.licenses;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class LicenseDto {

    private String code;

    private String label;

    private List<LicenseTypeDto> types;

    private String accessibleAt;

}
