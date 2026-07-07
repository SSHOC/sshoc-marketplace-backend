package eu.sshopencloud.marketplace.mappers.skg;

import eu.sshopencloud.marketplace.conf.SkgConfiguration;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.model.skg.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DatasetToSkgMapper {

    private final SkgConfiguration skgConfiguration;

    public DatasetToSkgMapper(SkgConfiguration skgConfiguration) {
        this.skgConfiguration = skgConfiguration;
    }

    public JsonLdDocument toSkg(DatasetDto dataset) {
        JsonLdDocument doc = new JsonLdDocument(skgConfiguration.getBaseUri());

        doc.addEntity(
                ResearchProduct.builder()
                        .localIdentifier("api/dataset/" + dataset.getPersistentId())
                        .productType("research data")
                        .entityType("product")
                        .titles(Map.of("none", dataset.getLabel()))
                        .abstracts(Map.of("none", List.of(dataset.getDescription())))
                        .contributions(generatedDatasetContributions(dataset))
                        .relatedProducts(generateRelatedProducts(dataset))
                        .manifestations(List.of(Map.of("dates", Map.of("modified", dataset.getLastInfoUpdate().toString()))))
                        .build()
        );

        return doc;
    }

    private List<Contribution> generatedDatasetContributions(DatasetDto dataset) {
        List<Contribution> result = new ArrayList<>();
        dataset.getContributors().forEach(contributor -> {
            Contribution contribution = new Contribution();

            contribution.setBy("api/actors/" + contributor.getActor().getId());
            contribution.setRole(contributor.getRole().getLabel());
            contribution.setDeclaredAffiliations(List.of(contributor.getActor().getName()));
            result.add(contribution);
        });

        return result;
    }

    private List<RelatedProduct> generateRelatedProducts(DatasetDto dataset) {
        List<RelatedProduct>  result = new ArrayList<>();
        dataset.getRelatedItems().forEach(relatedItem -> {
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
