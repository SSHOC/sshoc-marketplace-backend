package eu.sshopencloud.marketplace.services.vocabularies.rdf;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelation;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;

import java.util.*;
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
                String label = statement.getObject().stringValue();
                if (label.endsWith(".")) {
                    vocabulary.setLabel(label.substring(0, label.length() - 1));
                } else {
                    vocabulary.setLabel(label);
                }
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

    public Map<String, Concept> createConcepts(Model rdfModel, Vocabulary vocabulary) {
        Set<Namespace> namespaces = rdfModel.getNamespaces();
        Map<String, Concept> concepts = rdfModel.stream()
                .filter(statement -> statement.getPredicate().stringValue().equals(SKOS_TYPE))
                .filter(statement -> statement.getObject().stringValue().equals(SKOS_CONCEPT))
                .collect(
                        Collectors.toMap(
                                statement -> statement.getSubject().stringValue(),
                                statement -> createConcept(statement, vocabulary, namespaces),
                                (u, v) -> u,
                                LinkedHashMap::new
                        )
                );

        completeConcepts(concepts, rdfModel);
        numberConcepts(concepts.values());

        return concepts;
    }

    private void numberConcepts(Collection<Concept> concepts) {
        int ord = 0;
        for (Concept concept : concepts)
            concept.setOrd(ord++);
    }

    private void completeConcepts(Map<String, Concept> conceptMap, Model rdfModel) {
        rdfModel.forEach(statement -> {
            String subjectUri = statement.getSubject().stringValue();
            if (!conceptMap.containsKey(subjectUri))
                return;

            completeConcept(conceptMap.get(subjectUri), statement);
        });
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

    private ConceptRelatedConcept createConceptRelatedConcept(Concept concept, Statement statement, Map<String, Concept> conceptMap) {
        String predicateUri = statement.getPredicate().stringValue();
        if (predicateUri.equals(SKOS_BROADER) || predicateUri.equals(SKOS_NARROWER)) {
            String relationCode = predicateUri.substring(predicateUri.indexOf("#") + 1);
            String objectUri = statement.getObject().stringValue();
            if (conceptMap.containsKey(objectUri)) {
                ConceptRelatedConcept conceptRelatedConcept = new ConceptRelatedConcept();
                conceptRelatedConcept.setSubject(concept);
                conceptRelatedConcept.setObject(conceptMap.get(objectUri));
                ConceptRelation relation = new ConceptRelation();
                relation.setCode(relationCode);
                conceptRelatedConcept.setRelation(relation);
                return conceptRelatedConcept;
            }
        }
        return null;
    }

    public List<ConceptRelatedConcept> createConceptRelatedConcepts(Map<String, Concept> conceptMap, Model rdfModel) {
        List<ConceptRelatedConcept> result = new ArrayList<ConceptRelatedConcept>();
        for (String subjectUri: conceptMap.keySet()) {
            Concept concept = conceptMap.get(subjectUri);
            List<ConceptRelatedConcept> conceptRelatedConcepts = rdfModel.stream()
                    .filter(statement -> statement.getSubject().stringValue().equals(subjectUri))
                    .map(statement -> createConceptRelatedConcept(concept, statement, conceptMap))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            result.addAll(conceptRelatedConcepts);
        }
        return result;
    }

}
