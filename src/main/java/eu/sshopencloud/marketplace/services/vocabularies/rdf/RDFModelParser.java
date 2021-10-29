package eu.sshopencloud.marketplace.services.vocabularies.rdf;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelation;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RIOT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
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
        String scheme = schemeStatement.map(value -> value.getSubject().stringValue()).orElse(null);
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
        vocabulary.setNamespace(extractNamespaceUri(rdfModel));
        return vocabulary;
    }

    private String extractNamespaceUri(Model rdfModel) {
        String namespaceUri = "";
        Optional<Statement> conceptStatement = rdfModel.stream()
                .filter(statement -> statement.getPredicate().stringValue().equals(SKOS_TYPE))
                .filter(statement -> statement.getObject().stringValue().equals(SKOS_CONCEPT))
                .findFirst();
        String conceptUri = conceptStatement.map(value -> value.getSubject().stringValue()).orElse(null);
        if (conceptUri != null) {
            for (Namespace namespace : rdfModel.getNamespaces()) {
                if (conceptUri.startsWith(namespace.getName())) {
                    if (namespace.getName().startsWith(namespaceUri)) {
                        namespaceUri = namespace.getName();
                    }
                }
            }
        }
        if (StringUtils.isBlank(namespaceUri)) {
            Optional<Namespace> mainNamespace = rdfModel.getNamespace("");
            if (mainNamespace.isPresent()) {
                namespaceUri = mainNamespace.get().getName();
            }
        }
        return namespaceUri;
    }

    private Concept createConcept(Statement statement, Vocabulary vocabulary) {
        String conceptUri = statement.getSubject().stringValue();
        String namespaceUri = vocabulary.getNamespace();
        String conceptCode = conceptUri.substring(namespaceUri.length());
        Concept result = new Concept();
        result.setCode(conceptCode);
        result.setVocabulary(vocabulary);
        result.setLabel("");
        result.setLabels(new LinkedHashMap<>());
        result.setNotation("");
        result.setDefinitions(new LinkedHashMap<>());
        result.setUri(conceptUri);
        result.setCandidate(false);
        return result;
    }

    public Map<String, Concept> createConcepts(Model rdfModel, Vocabulary vocabulary) {
        Map<String, Concept> concepts = rdfModel.stream()
                .filter(statement -> statement.getPredicate().stringValue().equals(SKOS_TYPE))
                .filter(statement -> statement.getObject().stringValue().equals(SKOS_CONCEPT))
                .collect(
                        Collectors.toMap(
                                statement -> statement.getSubject().stringValue(),
                                statement -> createConcept(statement, vocabulary),
                                (u, v) -> u,
                                LinkedHashMap::new
                        )
                );

        completeConcepts(concepts, rdfModel);
        numberConcepts(concepts.values());

        return concepts;
    }

    private void numberConcepts(Collection<Concept> concepts) {
        int ord = 1;
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
        for (String subjectUri : conceptMap.keySet()) {
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


    //ELiza
    /*
    <https://vocabs.dariah.eu/sshoc-audience/schemaAudience> csw:hierarchyRoot true;
      a skos:ConceptScheme;
      skos:prefLabel "Intended audience"@en; V
      dcterms:title "Intended audience"@en;V
      skos:definition "Intended audience of an item."@en;V
      skos:note "Used in the SSHOC projects SSH Open Marketplace and SSH Training Discovery Toolkit."@en;
      skos:hasTopConcept <https://vocabs.dariah.eu/sshoc-audience/dataCreator> .
    * */
    public org.apache.jena.rdf.model.Model createRDFModel(Vocabulary vocabulary) {

        RDFResources resources = new RDFResources(vocabulary.getNamespace());

        org.apache.jena.rdf.model.Model model = ModelFactory.createDefaultModel();
        resources.generateNamespacePrefixes(vocabulary.getNamespace());
        model.setNsPrefixes(resources.getNamespacePrefixes());

        Resource vocabularySchema = model.createResource(vocabulary.getNamespace() + "Schema");

        Property propertyCSWHierarchyRoot = model.createProperty(resources.PREFIX_CSW + "hierarchyRoot");
        Property propertyCSWHierarchyRootType = model.createProperty(resources.PREFIX_CSW + "hierarchyRootType");

        vocabularySchema.addLiteral(propertyCSWHierarchyRoot, true)
                .addProperty(propertyCSWHierarchyRootType, SKOS.ConceptScheme)
                .addProperty(SKOS.prefLabel, vocabulary.getLabel(), "en");


        if (!vocabulary.getDescription().isEmpty() || !Objects.isNull(vocabulary.getDescription()))
            vocabularySchema.addProperty(DCTerms.description, vocabulary.getDescription());

        if (!vocabulary.getTitles().isEmpty()) {
            vocabulary.getTitles().forEach(
                    (key, value) -> {
                        vocabularySchema.addProperty(DCTerms.title, value, key);
                    }
            );
        }

        if (!vocabulary.getComments().isEmpty()) {
            vocabulary.getComments().forEach(
                    (key, value) -> {
                        vocabularySchema.addProperty(RDFS.comment, value, key);
                    }
            );
        }

        if (!vocabulary.getComments().isEmpty()) {
            vocabulary.getComments().forEach(
                    (key, value) -> {
                        vocabularySchema.addProperty(RDFS.label, value, key);
                    }
            );
        }

        //  model.add(vocabularySchema);
        /*
        vocabulary.getDescriptions().forEach(description ->
        {
            resources.getModel().getResource(vocabulary.getNamespace()+"Schema")
                   // .addProperty(DCTerms.description)
        });

*/
        org.apache.jena.riot.RDFWriter.create()
                .set(RIOT.symTurtleDirectiveStyle, "at")
                // .set(RIOT.symTurtleOmitBase, true)
                .lang(org.apache.jena.riot.Lang.TTL)
                .source(model)
                .output(System.out);


        return model;

    }


}
