package eu.sshopencloud.marketplace.mappers.skg;

import eu.sshopencloud.marketplace.conf.SkgConfiguration;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import eu.sshopencloud.marketplace.dto.actors.ActorDto;
import eu.sshopencloud.marketplace.dto.actors.PaginatedActors;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.items.ItemDto;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.dto.tools.PaginatedTools;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.model.skg.*;
import eu.sshopencloud.marketplace.model.skg.meta.Meta;
import eu.sshopencloud.marketplace.model.skg.meta.PageInfo;
import eu.sshopencloud.marketplace.model.skg.meta.PartOf;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ItemToSkgMapper {

    private final SkgConfiguration skgConfiguration;
    private final LocalIdentifierBuilder localIdentifierBuilder;

    public ItemToSkgMapper(SkgConfiguration skgConfiguration, LocalIdentifierBuilder localIdentifierBuilder) {
        this.skgConfiguration = skgConfiguration;
        this.localIdentifierBuilder = localIdentifierBuilder;
    }

    public JsonLdDocument empty() {
        return new JsonLdDocument(skgConfiguration.getBaseUri());
    }

    public JsonLdDocument toSkg(ItemDto item) {
        JsonLdDocument doc = new JsonLdDocument(skgConfiguration.getBaseUri());

        doc.addEntity(
                ResearchProduct.builder()
                        .localIdentifier(generateIdentifier(item))
                        .productType(generateProductType(item))
                        .entityType(EntityType.PRODUCT.toString())
                        .titles(Map.of("none", List.of(item.getLabel())))
                        .abstracts(Map.of("none", List.of(item.getDescription())))
                        .contributions(generateContributions(item))
                        .relatedProducts(generateRelatedProducts(item))
                        .identifiers(generateIdentifiers(item))
                        .manifestations(List.of(Map.of("dates", Map.of("modified", item.getLastInfoUpdate().toString()))))
                        .build()
        );
        return doc;
    }

    public JsonLdDocument toSkg(PaginatedTools tools) {
        JsonLdDocument doc = new JsonLdDocument(skgConfiguration.getBaseUri());

        mapMetaPart(tools, doc);
        mapGraph(tools, doc);
        return doc;
    }

    public JsonLdDocument toSkg(PaginatedActors actors) {
        JsonLdDocument doc = new JsonLdDocument(skgConfiguration.getBaseUri());

        mapMetaPart(actors, doc);
        mapGraph(actors, doc);
        return doc;

    }


    private void mapMetaPart(PaginatedTools tools, JsonLdDocument doc) {

        Meta.MetaBuilder builder = Meta.builder();

        if (tools.getPage() != 1) {
            builder.prevPage(PageInfo.builder()
                    .entityType(EntityType.SEARCH_RESULT_PAGE.toString())
                    .localIdentifier(localIdentifierBuilder.buildForPrevPage(tools, EntityType.PRODUCT)).build());
        }

        if (tools.getPage() <= tools.getPages()) {
            builder.nextPage(PageInfo.builder()
                    .entityType(EntityType.SEARCH_RESULT_PAGE.toString())
                    .localIdentifier(localIdentifierBuilder.buildForNextPage(tools, EntityType.PRODUCT)).build());
        }

        builder.localIdentifier(localIdentifierBuilder.buildForCurrentPage(tools, EntityType.PRODUCT))
                .entityType(EntityType.SEARCH_RESULT_PAGE.toString());

        builder.partOf(PartOf.builder()
                        .entityType(EntityType.SEARCH_RESULT.toString())
                        .localIdentifier(skgConfiguration.getApiBaseUri() + "/products")
                        .totalItems(tools.getHits())
                        .firstPage(
                            PageInfo.builder()
                                .entityType(EntityType.SEARCH_RESULT_PAGE.toString())
                                .localIdentifier(localIdentifierBuilder.buildForFirstPage(tools, EntityType.PRODUCT))
                            .build())
                        .lastPage(
                            PageInfo.builder()
                                .entityType(EntityType.SEARCH_RESULT_PAGE.toString())
                                .localIdentifier(localIdentifierBuilder.buildForLastPage(tools, EntityType.PRODUCT))
                            .build())
                .build());

        doc.addMetaPart(builder.build());
    }

    private void mapMetaPart(PaginatedActors paginatedActors, JsonLdDocument doc) {
        mapMetaPart(paginatedActors,doc, EntityType.PERSON);
    }

    private void mapMetaPart(PaginatedResult<?> paginatedResult, JsonLdDocument doc, EntityType entityType) {

        Meta.MetaBuilder builder = Meta.builder();

        if (paginatedResult.getPage() != 1) {
            builder.prevPage(PageInfo.builder()
                    .entityType(EntityType.SEARCH_RESULT_PAGE.toString())
                    .localIdentifier(localIdentifierBuilder.buildForPrevPage(paginatedResult, entityType)).build());
        }

        if (paginatedResult.getPage() <= paginatedResult.getPages()) {
            builder.nextPage(PageInfo.builder()
                    .entityType(EntityType.SEARCH_RESULT_PAGE.toString())
                    .localIdentifier(localIdentifierBuilder.buildForNextPage(paginatedResult, EntityType.PERSON)).build());
        }

        builder.localIdentifier(localIdentifierBuilder.buildForCurrentPage(paginatedResult, EntityType.PERSON))
                .entityType(EntityType.SEARCH_RESULT_PAGE.toString());

        builder.partOf(PartOf.builder()
                .entityType(EntityType.SEARCH_RESULT.toString())
                .localIdentifier(localIdentifierBuilder.buildGlobal(EntityType.PERSON))
                .totalItems(paginatedResult.getHits())
                .firstPage(
                        PageInfo.builder()
                                .entityType(EntityType.SEARCH_RESULT_PAGE.toString())
                                .localIdentifier(localIdentifierBuilder.buildForFirstPage(paginatedResult, EntityType.PERSON))
                                .build())
                .lastPage(
                        PageInfo.builder()
                                .entityType(EntityType.SEARCH_RESULT_PAGE.toString())
                                .localIdentifier(localIdentifierBuilder.buildForLastPage(paginatedResult, EntityType.PERSON))
                                .build())
                .build());

        doc.addMetaPart(builder.build());
    }

    private void mapGraph(PaginatedTools tools, JsonLdDocument doc) {
        for (ItemDto item : tools.getTools()) {
            doc.addEntity(
                    ResearchProduct.builder()
                            .localIdentifier(generateIdentifier(item))
                            .productType(generateProductType(item))
                            .entityType(EntityType.PRODUCT.toString())
                            .titles(Map.of("none", List.of(item.getLabel())))
                            .abstracts(Map.of("none", List.of(item.getDescription())))
                            .contributions(generateContributions(item))
                            .relatedProducts(generateRelatedProducts(item))
                            .identifiers(generateIdentifiers(item))
                            .manifestations(List.of(Map.of("dates", Map.of("modified", item.getLastInfoUpdate().toString()))))
                            .build()
            );
        }
    }

    private void mapGraph(PaginatedActors actors, JsonLdDocument doc) {
        for (ActorDto actor : actors.getActors()) {
            doc.addEntity(
                    Agent.builder()
                            .localIdentifier(skgConfiguration.getApiBaseUri() + "/api/actors/" + actor.getId())
                            .entityType("person")
                            .identifiers(Collections.emptyList())
                            .name(actor.getName())
                            .givenName("")
                            .familyName("")
                            .affiliations(generateActorAffiliations(actor))
                            .shortName("")
                            .otherNames(Collections.emptyList())
                            .website(actor.getWebsite())
                            .country("")
                            .types(Collections.emptyList())
                            .build()
            );
        }
    }

    private List<AgentAffiliations> generateActorAffiliations(ActorDto actor) {
        List<AgentAffiliations> agentAffiliations = new ArrayList<>();

        for (ActorDto affiliation : actor.getAffiliations()) {
            agentAffiliations.add(AgentAffiliations
                    .builder()
                            .affiliation(localIdentifierBuilder.buildIdForActor(affiliation.getId()+""))
                    .build());

        }
        return agentAffiliations;
    }

    private String generateIdentifier(ItemDto item) {
        return switch (item) {
            case ToolDto ignored -> skgConfiguration.getBaseUri() + "/tool-or-service/" + item.getPersistentId();
            case DatasetDto ignored -> skgConfiguration.getBaseUri() + "/dataset/" + item.getPersistentId();
            case PublicationDto ignored -> skgConfiguration.getBaseUri() + "/publication/" + item.getPersistentId();
            case null, default -> "";
        };
    }

    private String generateProductType(ItemDto item){
        return switch (item) {
            case ToolDto ignored -> "research software";
            case DatasetDto ignored -> "research data";
            case PublicationDto ignored -> "research literature";
            case null, default -> "";
        };
    }

    private List<Contribution> generateContributions(ItemDto item) {
        List<Contribution> result = new ArrayList<>();
        item.getContributors().forEach(contributor -> {
            Contribution contribution = new Contribution();

            contribution.setBy("api/actors/" + contributor.getActor().getId());
            contribution.setRole(contributor.getRole().getLabel());
            contribution.setDeclaredAffiliations(List.of(contributor.getActor().getName()));
            result.add(contribution);
        });

        return result;
    }

    private List<Identifier> generateIdentifiers(ItemDto item) {
        List<Identifier> result = new ArrayList<>();
        item.getAccessibleAt().forEach(accessibleAt -> {
            Identifier identifier = new Identifier();
            identifier.setScheme("url");
            identifier.setValue(accessibleAt);
            result.add(identifier);
        });

        item.getExternalIds().forEach(externalIdDto -> {
            Identifier identifier = new Identifier();
            identifier.setScheme("url");
            identifier.setValue(externalIdDto.getIdentifier());
            result.add(identifier);
        });

        return result;
    }

    private List<RelatedProduct> generateRelatedProducts(ItemDto item) {
        List<RelatedProduct>  result = new ArrayList<>();
        item.getRelatedItems().forEach(relatedItem -> {
            if (relatedItem.getRelation().getCode().equals("is-documented-by")) {
                RelatedProduct relatedProduct = new RelatedProduct();
                relatedProduct.setIsDocumentedBy(List.of(relatedItem.getPersistentId()));
                result.add(relatedProduct);
            }
            if(relatedItem.getRelation().getCode().equals("mentions")) {
                RelatedProduct relatedProduct = new RelatedProduct();
                relatedProduct.setCites(List.of(relatedItem.getPersistentId()));
                result.add(relatedProduct);
            }
        });
        return result;
    }
}
