package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
public class Service extends  Tool {

    private ToolType toolType = ToolType.SERVICE;

}
