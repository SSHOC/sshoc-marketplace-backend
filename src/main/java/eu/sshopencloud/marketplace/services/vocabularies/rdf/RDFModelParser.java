package eu.sshopencloud.marketplace.services.vocabularies.rdf;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptConverter;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class RDFModelParser {

    private static final String SKOS_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private static final String SKOS_CONCEPT_SCHEME = "http://www.w3.org/2004/02/skos/core#ConceptScheme";

    private static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";

    private static final String RDFS_COMMENT = "http://www.w3.org/2000/01/rdf-schema#comment";

    private static final String SKOS_CONCEPT = "http://www.w3.org/2004/02/skos/core#Concept";

    private static final String SKOS_LABEL = "http://www.w3.org/2004/02/skos/core#prefLabel";

    private static final String SKOS_NOTATION = "http://www.w3.org/2004/02/skos/core#notation";

    private static final String SKOS_DEFINITION = "http://www.w3.org/2004/02/skos/core#definition";


    private static final String SKOS_BROADER = "http://www.w3.org/2004/02/skos/core#broader";

    private static final String SKOS_NARROWER = "http://www.w3.org/2004/02/skos/core#narrower";



    private void completeVocabulary(Vocabulary vocabulary, Statement statement) {
        if (statement.getPredicate().stringValue().equals(RDFS_LABEL)) {
            if (StringUtils.isBlank(vocabulary.getLabel())) {
                vocabulary.setLabel(statement.getObject().stringValue());
            }
        }
        if (statement.getPredicate().stringValue().equals(RDFS_COMMENT)) {
            if (StringUtils.isBlank(vocabulary.getDescription())) {
                vocabulary.setDescription(statement.getObject().stringValue());
            }
        }
        // TODO accessibleAt
    }

    public Vocabulary createVocabulary(String vocabularyCode, Model rdfModel) {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setCode(vocabularyCode);
        vocabulary.setLabel("");
        vocabulary.setDescription("");
        Optional<Statement> schemeStatement = rdfModel.stream()
                .filter(statement -> statement.getPredicate().stringValue().equals(SKOS_TYPE))
                .filter(statement -> statement.getObject().stringValue().equals(SKOS_CONCEPT_SCHEME))
                .findFirst();
        String scheme = schemeStatement.isPresent() ? schemeStatement.get().getSubject().stringValue() : null;
        if (scheme != null) {
            rdfModel.stream()
                    .filter(statement -> statement.getSubject().stringValue().equals(scheme))
                    .forEach(statement -> completeVocabulary(vocabulary, statement));
        }
        return vocabulary;
    }

    private Concept createConcept(Statement statement, Vocabulary vocabulary, Set<Namespace> namespaces) {
        String conceptUri = statement.getSubject().stringValue();
        String namespaceUri = "";
        for (Namespace namespace: namespaces) {
            if (conceptUri.startsWith(namespace.getName())) {
                if (namespace.getName().startsWith(namespaceUri)) {
                    namespaceUri = namespace.getName();
                }
            }
        }
        String conceptCode = conceptUri.substring(namespaceUri.length());
        Concept result = new Concept();
        result.setCode(conceptCode);
        result.setVocabulary(vocabulary);
        result.setLabel("");
        result.setDefinition("");
        result.setNotation("");
        result.setUri(conceptUri);
        return result;
    }

    public Map<Resource, Concept> createConcepts(Model rdfModel, Vocabulary vocabulary) {
        Set<Namespace> namespaces = rdfModel.getNamespaces();
        return rdfModel.stream()
                .filter(statement -> statement.getPredicate().stringValue().equals(SKOS_TYPE))
                .filter(statement -> statement.getObject().stringValue().equals(SKOS_CONCEPT))
                .collect(Collectors.toMap(Statement::getSubject, statement -> createConcept(statement, vocabulary, namespaces)));
    }

    private void completeConcept(Concept concept, Statement statement) {
        if (statement.getPredicate().stringValue().equals(SKOS_LABEL)) {
            if (StringUtils.isBlank(concept.getLabel())) {
                concept.setLabel(statement.getObject().stringValue());
            }
        }
        if (statement.getPredicate().stringValue().equals(SKOS_NOTATION)) {
            if (StringUtils.isBlank(concept.getNotation())) {
                concept.setNotation(statement.getObject().stringValue());
            }
        }
        if (statement.getPredicate().stringValue().equals(SKOS_DEFINITION)) {
            if (StringUtils.isBlank(concept.getDefinition())) {
                concept.setDefinition(statement.getObject().stringValue());
            }
        }
    }

    private void completeRelatedConcept(Concept concept, Statement statement, Map<Resource, Concept> concepts) {
        if (statement.getPredicate().stringValue().equals(SKOS_BROADER)) {
            ConceptRelatedConcept conceptRelatedConcept = new ConceptRelatedConcept();
            conceptRelatedConcept.setSubject(concept);
            // TODO
        }
    }

    public static void completeConcepts(Map<Resource, Concept> concepts, Model rdfModel) {
        for (Resource subject: concepts.keySet()) {
            Concept concept = concepts.get(subject);
            rdfModel.stream()
                    .filter(statement -> statement.getSubject().equals(subject))
                    .forEach(statement -> completeConcept(concept, statement));
        }
    }

}
