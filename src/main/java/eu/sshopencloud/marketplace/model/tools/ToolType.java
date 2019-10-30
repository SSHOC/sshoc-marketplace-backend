package eu.sshopencloud.marketplace.model.tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "tool_types")
@Data
@NoArgsConstructor
public class ToolType {

    @Id
    private String code;

    @Basic
    @Column(nullable = false)
    @JsonIgnore
    private Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

}
