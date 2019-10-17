package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.services.PaginatedResult;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PaginatedTools extends PaginatedResult {

    public PaginatedTools(Page<Tool> tools, int page, int perpage) {
        this.setTools(tools.getContent());
        this.setHits(tools.getTotalElements());
        this.setCount(this.getTools().size());
        this.setPage(page);
        this.setPerpage(perpage);
        this.setPages(tools.getTotalPages());
    }

    private List<Tool> tools;

}
