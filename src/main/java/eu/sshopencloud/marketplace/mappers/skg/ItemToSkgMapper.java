package eu.sshopencloud.marketplace.mappers.skg;

import eu.sshopencloud.marketplace.conf.SkgConfiguration;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.items.ItemDto;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.model.skg.Contribution;
import eu.sshopencloud.marketplace.model.skg.JsonLdDocument;
import eu.sshopencloud.marketplace.model.skg.RelatedProduct;
import eu.sshopencloud.marketplace.model.skg.ResearchProduct;
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

    private String generateIdentifier(ItemDto item) {
        return switch (item) {
            case ToolDto ignored -> "api/tools-services/" + item.getPersistentId();
            case DatasetDto ignored -> "api/datasets/" + item.getPersistentId();
            case PublicationDto ignored -> "api/publications/" + item.getPersistentId();
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
