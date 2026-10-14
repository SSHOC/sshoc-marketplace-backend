package eu.sshopencloud.marketplace.mappers.skg;

import eu.sshopencloud.marketplace.conf.SkgConfiguration;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.items.ItemDto;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.model.skg.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ItemToSkgMapper {

    private final SkgConfiguration skgConfiguration;

    public ItemToSkgMapper(SkgConfiguration skgConfiguration) {
        this.skgConfiguration = skgConfiguration;
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
                        .entityType("product")
                        .titles(Map.of("none", List.of(item.getLabel())))
                        .abstracts(Map.of("none", List.of(item.getDescription())))
                        .contributions(generateContributions(item))
                        .relatedProducts(generateRelatedProducts(item))
                        .manifestations(List.of(Map.of("dates", Map.of("modified", item.getLastInfoUpdate().toString()))))
                        .build()
        );

        return doc;
    }

    public JsonLdDocument toSkg(List<ToolDto> items) {
        JsonLdDocument doc = new JsonLdDocument(skgConfiguration.getBaseUri());

        for (ItemDto item : items) {
            doc.addEntity(
                    ResearchProduct.builder()
                            .localIdentifier(generateIdentifier(item))
                            .productType(generateProductType(item))
                            .entityType("product")
                            .titles(Map.of("none", List.of(item.getLabel())))
                            .abstracts(Map.of("none", List.of(item.getDescription())))
                            .contributions(generateContributions(item))
                            .relatedProducts(generateRelatedProducts(item))
                            .identifiers(generateIdentifiers(item))
                            .manifestations(List.of(Map.of("dates", Map.of("modified", item.getLastInfoUpdate().toString()))))
                            .build()
            );
        }
        return doc;
    }

    private String generateIdentifier(ItemDto item) {
        return switch (item) {
            case ToolDto ignored -> skgConfiguration.getBaseUri() + "api/tools-services/" + item.getPersistentId();
            case DatasetDto ignored -> skgConfiguration.getBaseUri() + "api/datasets/" + item.getPersistentId();
            case PublicationDto ignored -> skgConfiguration.getBaseUri() + "api/publications/" + item.getPersistentId();
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
            identifier.setScheme("<scheme>");
            identifier.setValue(accessibleAt);
            result.add(identifier);
        });

        item.getExternalIds().forEach(externalIdDto -> {
            Identifier identifier = new Identifier();
            identifier.setScheme("<scheme>");
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
