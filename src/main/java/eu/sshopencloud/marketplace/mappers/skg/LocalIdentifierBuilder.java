package eu.sshopencloud.marketplace.mappers.skg;

import eu.sshopencloud.marketplace.conf.SkgConfiguration;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import org.springframework.stereotype.Service;

@Service
public class LocalIdentifierBuilder {

    private final SkgConfiguration skgConfiguration;

    public LocalIdentifierBuilder(SkgConfiguration skgConfiguration) {
        this.skgConfiguration = skgConfiguration;
    }

    public String buildForPrevPage(PaginatedResult paginatedResult){
        return skgConfiguration.getBaseUri() + "products?page=" + (paginatedResult.getPages() - 1) + "&page_size=" + paginatedResult.getPerpage();
    }

    public String buildForNextPage(PaginatedResult paginatedResult){
        return skgConfiguration.getBaseUri() + "products?page=" + (paginatedResult.getPage() + 1) + "&page_size=" + paginatedResult.getPerpage();
    }

    public String buildForLastPage(PaginatedResult paginatedResult){
        return skgConfiguration.getBaseUri() + "products?page=" + paginatedResult.getPages() + "&page_size=" + paginatedResult.getPerpage();
    }

    public String buildForCurrentPage(PaginatedResult paginatedResult){
        return skgConfiguration.getBaseUri() + "products?page=" + paginatedResult.getPages() + "&page_size=" + paginatedResult.getPerpage();
    }
}
