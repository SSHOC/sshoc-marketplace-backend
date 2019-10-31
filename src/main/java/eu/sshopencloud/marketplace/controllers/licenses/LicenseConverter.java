package eu.sshopencloud.marketplace.controllers.licenses;

import eu.sshopencloud.marketplace.dto.licenses.LicenseId;
import eu.sshopencloud.marketplace.model.licenses.License;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class LicenseConverter {

    public License convert(LicenseId license) {
        License result = new License();
        result.setCode(license.getCode());
        return result;
    }

    public List<License> convert(List<LicenseId> licenses) {
        List<License> result = new ArrayList<License>();
        if (licenses != null) {
            for (LicenseId license : licenses) {
                result.add(convert(license));
            }
        }
        return result;
    }

}
