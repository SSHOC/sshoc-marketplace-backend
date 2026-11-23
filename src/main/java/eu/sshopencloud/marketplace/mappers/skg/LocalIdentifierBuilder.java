package eu.sshopencloud.marketplace.mappers.skg;

import eu.sshopencloud.marketplace.conf.SkgConfiguration;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import eu.sshopencloud.marketplace.model.skg.EntityType;
import org.springframework.stereotype.Service;

@Service
public class LocalIdentifierBuilder {

    private final SkgConfiguration skgConfiguration;

    public LocalIdentifierBuilder(SkgConfiguration skgConfiguration) {
        this.skgConfiguration = skgConfiguration;
    }

    public String buildGlobal(EntityType entityType){
        return skgConfiguration.getApiBaseUri() + "/skg/" + objectType(entityType);
    }

    public String buildForPrevPage(PaginatedResult paginatedResult, EntityType entityType){
        return skgConfiguration.getApiBaseUri() + "/skg/" + objectType(entityType) + "?page=" + (paginatedResult.getPage() - 1) + "&page_size=" + paginatedResult.getPerpage();
    }

    public String buildForNextPage(PaginatedResult paginatedResult, EntityType entityType){
        return skgConfiguration.getApiBaseUri() + "/skg/" + objectType(entityType) + "?page=" + (paginatedResult.getPage() + 1) + "&page_size=" + paginatedResult.getPerpage();
    }

    public String buildForFirstPage(PaginatedResult paginatedResult, EntityType entityType){
        return skgConfiguration.getApiBaseUri() + "/skg/" + objectType(entityType) + "?page=" + "1" + "&page_size=" + paginatedResult.getPerpage();
    }

    public String buildForLastPage(PaginatedResult paginatedResult, EntityType entityType){
        return skgConfiguration.getApiBaseUri() + "/skg/"+objectType(entityType)+"?page=" + paginatedResult.getPages() + "&page_size=" + paginatedResult.getPerpage();
    }

    public String buildForCurrentPage(PaginatedResult paginatedResult, EntityType entityType){
        return skgConfiguration.getApiBaseUri() + "/skg/"+objectType(entityType)+"?page=" + paginatedResult.getPage() + "&page_size=" + paginatedResult.getPerpage();
    }

    public String buildIdForActor(String id){
        return skgConfiguration.getApiBaseUri() + "/actors/" + id;
    }


    private String objectType(EntityType entityType){
        if(entityType == EntityType.PERSON) return "persons";
        if(entityType == EntityType.PRODUCT) return "products";
        return "null";
    }
}
