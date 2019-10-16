package eu.sshopencloud.marketplace.repositories.tools;

import eu.sshopencloud.marketplace.model.tools.ToolType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolTypeRepository extends JpaRepository<ToolType, String> {

}
