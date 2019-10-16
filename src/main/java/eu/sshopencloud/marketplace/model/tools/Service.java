package eu.sshopencloud.marketplace.model.tools;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "services")
@Data
public class Service extends  Tool {

    public Service() {
        ToolType toolType = new ToolType();
        toolType.setCode("service");
        this.setToolType(toolType);
    }

}
