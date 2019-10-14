package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "services")
@Data
public class Service extends  Tool {

    public Service() {
        this.setToolType(ToolType.SERVICE);
    }

}
