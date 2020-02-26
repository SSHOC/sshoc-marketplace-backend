package eu.sshopencloud.marketplace.conf.startup.trainings;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingMaterialLoader {

    private final TrainingMaterialRepository trainingMaterialRepository;

    private final ConceptRepository conceptRepository;

    private  final IndexService indexService;

    public void createTrainingMaterials(List<TrainingMaterial> newTrainingMaterials) {
        for (TrainingMaterial newTrainingMaterial: newTrainingMaterials) {
            for (Property property: newTrainingMaterial.getProperties()) {
                if (property.getConcept() != null) {
                    property.setConcept(conceptRepository.findById(ConceptId.builder().code(property.getConcept().getCode()).vocabulary(property.getConcept().getVocabulary().getCode()).build()).get());
                }
            }
            TrainingMaterial trainingMaterial = trainingMaterialRepository.save(newTrainingMaterial);
            indexService.indexItem(trainingMaterial);
        }
    }

}
