package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.tools.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Long> {

}
