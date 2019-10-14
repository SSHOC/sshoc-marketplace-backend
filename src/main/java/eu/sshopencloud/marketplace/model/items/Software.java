package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name = "software")
@Data
public class Software extends Tool {

    public Software() {
        this.setToolType(ToolType.SOFTWARE);
    }

}
