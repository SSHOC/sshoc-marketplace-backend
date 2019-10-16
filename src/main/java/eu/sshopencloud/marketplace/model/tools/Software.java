package eu.sshopencloud.marketplace.model.tools;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "software")
@Data
public class Software extends Tool {

    public Software() {
        ToolType toolType = new ToolType();
        toolType.setCode("software");
        this.setToolType(toolType);
    }

}
