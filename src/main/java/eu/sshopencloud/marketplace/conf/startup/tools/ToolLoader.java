package eu.sshopencloud.marketplace.conf.startup.tools;

import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.repositories.tools.ToolRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolLoader {

    private final ToolRepository toolRepository;

    private final ConceptRepository conceptRepository;

    private  final IndexService indexService;

    public void createTools(List<Tool> newTools) {
        for (Tool newTool: newTools) {
            for (Property property: newTool.getProperties()) {
                if (property.getConcept() != null) {
                    property.setConcept(conceptRepository.findById(ConceptId.builder().code(property.getConcept().getCode()).vocabulary(property.getConcept().getVocabulary().getCode()).build()).get());
                }
            }
            Tool tool = toolRepository.save(newTool);
            indexService.indexItem(tool);
        }
    }

}
