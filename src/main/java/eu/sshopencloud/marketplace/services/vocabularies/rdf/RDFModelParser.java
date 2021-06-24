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
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class RDFModelParser {

    private static final String SKOS_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private static final String SKOS_CONCEPT_SCHEME = "http://www.w3.org/2004/02/skos/core#ConceptScheme";

    private static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";

    private static final String DC_TITLE = "http://purl.org/dc/elements/1.1/title";

    private static final String DCT_TITLE = "http://purl.org/dc/terms/title";

    private static final String RDFS_COMMENT = "http://www.w3.org/2000/01/rdf-schema#comment";

    private static final String DC_DESCRIPTION = "http://purl.org/dc/elements/1.1/description";

    private static final String SKOS_CONCEPT = "http://www.w3.org/2004/02/skos/core#Concept";

    private static final String SKOS_LABEL = "http://www.w3.org/2004/02/skos/core#prefLabel";

    private static final String SKOS_NOTATION = "http://www.w3.org/2004/02/skos/core#notation";

    private static final String SKOS_DEFINITION = "http://www.w3.org/2004/02/skos/core#definition";

    private static final String SKOS_BROADER = "http://www.w3.org/2004/02/skos/core#broader";

    private static final String SKOS_NARROWER = "http://www.w3.org/2004/02/skos/core#narrower";


    private void completeWithStatement(Statement statement, Map<String, String> values,
                                       Function<Void, String> checkFunction, Function<String, Void> setFunction) {
        if (statement.getObject() instanceof SimpleLiteral) {
            SimpleLiteral object = (SimpleLiteral) statement.getObject();
            if (object.getLanguage().isPresent()) {
                if (!values.containsKey(object.getLanguage().get())) {
                    values.put(object.getLanguage().get(), object.getLabel());
                }
            } else {
                if (StringUtils.isBlank(checkFunction.apply(null))) {
                    setFunction.apply(object.getLabel());
                }
            }
        } else {
            if (StringUtils.isBlank(checkFunction.apply(null))) {
                setFunction.apply(statement.getObject().stringValue());
            }
        }
    }


    private void completeVocabulary(Vocabulary vocabulary, Statement statement) {
        switch (statement.getPredicate().stringValue()) {
            case RDFS_LABEL:
                completeWithStatement(statement, vocabulary.getLabels(),
                        v -> vocabulary.getLabel(),
                        s -> {
                            vocabulary.setLabel(s);
                            return null;
                        });
                break;
            case DC_TITLE:
            case DCT_TITLE:
                completeWithStatement(statement, vocabulary.getTitles(),
                        v -> vocabulary.getLabel(),
                        s -> {
                            vocabulary.setLabel(s);
                            return null;
                        });
                break;
            case RDFS_COMMENT:
                completeWithStatement(statement, vocabulary.getComments(),
                        v -> vocabulary.getDescription(),
                        s -> {
                            vocabulary.setDescription(s);
                            return null;
                        });
                break;
            case DC_DESCRIPTION:
                completeWithStatement(statement, vocabulary.getDescriptions(),
                        v -> vocabulary.getDescription(),
                        s -> {
                            vocabulary.setDescription(s);
                            return null;
                        });
                break;
        }
        // TODO accessibleAt
    }


    public Vocabulary createVocabulary(String vocabularyCode, Model rdfModel) {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setCode(vocabularyCode);
        vocabulary.setLabel("");
        vocabulary.setLabels(new HashMap<>());
        vocabulary.setTitles(new HashMap<>());
        vocabulary.setComments(new HashMap<>());
        vocabulary.setDescriptions(new HashMap<>());
        Optional<Statement> schemeStatement = rdfModel.stream()
                .filter(statement -> statement.getPredicate().stringValue().equals(SKOS_TYPE))
                .filter(statement -> statement.getObject().stringValue().equals(SKOS_CONCEPT_SCHEME))
                .findFirst();
        String scheme = schemeStatement.isPresent() ? schemeStatement.get().getSubject().stringValue() : null;
        if (scheme != null) {
            rdfModel.stream()
                    .filter(statement -> statement.getSubject().stringValue().equals(scheme))
                    .forEach(statement -> completeVocabulary(vocabulary, statement));
            if (StringUtils.isBlank(vocabulary.getLabel()) && vocabulary.getLabels().containsKey("en")) {
                vocabulary.setLabel(vocabulary.getLabels().get("en"));
            }
            if (StringUtils.isBlank(vocabulary.getLabel()) && vocabulary.getTitles().containsKey("en")) {
                vocabulary.setLabel(vocabulary.getTitles().get("en"));
            }
            if (StringUtils.isBlank(vocabulary.getLabel()) && !vocabulary.getLabels().isEmpty()) {
                vocabulary.setLabel(vocabulary.getLabels().values().iterator().next());
            }
            if (StringUtils.isBlank(vocabulary.getLabel()) && !vocabulary.getTitles().isEmpty()) {
                vocabulary.setLabel(vocabulary.getTitles().values().iterator().next());
            }
            if (StringUtils.isBlank(vocabulary.getDescription()) && vocabulary.getComments().containsKey("en")) {
                vocabulary.setDescription(vocabulary.getComments().get("en"));
            }
            if (StringUtils.isBlank(vocabulary.getDescription()) && vocabulary.getDescriptions().containsKey("en")) {
                vocabulary.setDescription(vocabulary.getDescriptions().get("en"));
            }
            if (StringUtils.isBlank(vocabulary.getDescription()) && !vocabulary.getComments().isEmpty()) {
                vocabulary.setDescription(vocabulary.getComments().values().iterator().next());
            }
            if (StringUtils.isBlank(vocabulary.getDescription()) && !vocabulary.getDescriptions().isEmpty()) {
                vocabulary.setDescription(vocabulary.getDescriptions().values().iterator().next());
            }
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
        result.setLabels(new LinkedHashMap<>());
        result.setNotation("");
        result.setDefinitions(new LinkedHashMap<>());
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
        for (Concept concept : conceptMap.values()) {
            if (StringUtils.isBlank(concept.getLabel()) && concept.getLabels().containsKey("en")) {
                concept.setLabel(concept.getLabels().get("en"));
            }
            if (StringUtils.isBlank(concept.getLabel()) && !concept.getLabels().isEmpty()) {
                concept.setLabel(concept.getLabels().values().iterator().next());
            }
            if (StringUtils.isBlank(concept.getDefinition()) && concept.getDefinitions().containsKey("en")) {
                concept.setDefinition(concept.getDefinitions().get("en"));
            }
            if (StringUtils.isBlank(concept.getDefinition()) && !concept.getDefinitions().isEmpty()) {
                concept.setDefinition(concept.getDefinitions().values().iterator().next());
            }
        }
    }

    private void completeConcept(Concept concept, @NotNull Statement statement) {
        switch (statement.getPredicate().stringValue()) {
            case SKOS_LABEL:
                completeWithStatement(statement, concept.getLabels(),
                        v -> concept.getLabel(),
                        s -> {
                            concept.setLabel(s);
                            return null;
                        });
                break;
            case SKOS_NOTATION:
                if (StringUtils.isBlank(concept.getNotation())) {
                    concept.setNotation(statement.getObject().stringValue());
                }
                break;
            case SKOS_DEFINITION:
                completeWithStatement(statement, concept.getDefinitions(),
                        v -> concept.getDefinition(),
                        s -> {
                            concept.setDefinition(s);
                            return null;
                        });
                break;
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
