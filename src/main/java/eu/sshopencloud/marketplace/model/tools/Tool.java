package eu.sshopencloud.marketplace.model.tools;

import eu.sshopencloud.marketplace.model.items.Item;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "tools")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Tool extends Item {

    public Tool(Tool baseTool) {
        super(baseTool);
    }
}
