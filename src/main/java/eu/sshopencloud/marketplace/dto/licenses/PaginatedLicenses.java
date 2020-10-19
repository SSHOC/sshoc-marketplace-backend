package eu.sshopencloud.marketplace.dto.licenses;

import com.fasterxml.jackson.annotation.JsonGetter;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class PaginatedLicenses extends PaginatedResult<LicenseDto> {

    private List<LicenseDto> licenses;

    @Override
    @JsonGetter("licenses")
    public List<LicenseDto> getResults() {
        return licenses;
    }
}
