package eu.sshopencloud.marketplace.controllers.tools;

import eu.sshopencloud.marketplace.controllers.items.ItemContributorConverter;
import eu.sshopencloud.marketplace.controllers.licenses.LicenseConverter;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.controllers.vocabularies.PropertyConverter;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.tools.Service;
import eu.sshopencloud.marketplace.model.tools.Software;
import eu.sshopencloud.marketplace.model.tools.Tool;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ToolConverter {

    public Tool convert(ToolCore tool, String toolTypeCode) {
        Tool result = createTool(toolTypeCode);
        result.setCategory(ItemCategory.TOOL);
        result.setLabel(tool.getLabel());
        result.setVersion(tool.getVersion());
        result.setDescription(tool.getDescription());
        result.setLicenses(LicenseConverter.convert(tool.getLicenses()));
        result.setContributors(ItemContributorConverter.convert(tool.getContributors(), result));
        result.setProperties(PropertyConverter.convert(tool.getProperties()));
        result.setAccessibleAt(tool.getAccessibleAt());
        if (tool.getPrevVersionId() != null) {
            Tool prevVersion = createTool(toolTypeCode);
            prevVersion.setId(tool.getPrevVersionId());
            result.setPrevVersion(prevVersion);
        }
        return result;
    }

    private Tool createTool(String toolTypeCode) {
        switch (toolTypeCode) {
            case "software":
                return new Software();
            case "service":
                return new Service();
            default:
                return null; // validation is done earlier
        }
    }

}
