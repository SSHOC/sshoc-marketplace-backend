package eu.sshopencloud.marketplace.services.vocabularies.rdf;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelation;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.*;
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

    public static final String PREFIX_CSW = "http://semantic-web.at/ontologies/csw.owl#";

    public static final String PREFIX_CYCANNOT = "http://sw.cyc.com/CycAnnotations_v1#";

    public static final String PREFIX_TAGS = "http://www.holygoat.co.uk/owl/redwood/0.1/tags/";

    public static final String PREFIX_DBPEDIA = "http://dbpedia.org/resource/";

    public static final String PREFIX_FREEBASE = "http://rdf.freebase.com/ns/";

    public static final String PREFIX_OPENCYC = "http://sw.opencyc.org/concept/";

    public static final String PREFIX_CYC = "http://sw.cyc.com/concept/";

    public static final String PREFIX_CTAG = "http://commontag.org/ns#";

    public Model modelGlobal;

    public List<Statement> inverseStatements;

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
            vocabulary.setScheme(scheme);
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
        vocabulary.setNamespaces(getNamespaces(rdfModel));
        vocabulary.setNamespace(extractNamespaceUri(rdfModel));
        return vocabulary;
    }

    private Map<String, String> getNamespaces(Model rdfModel) {
        Map<String, String> namespaces = new LinkedHashMap<>();
        for (Namespace namespace : rdfModel.getNamespaces()) {
            namespaces.put(namespace.getPrefix(), namespace.getName());
        }
        return namespaces;
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
        List<ConceptRelatedConcept> result = new ArrayList<>();
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


    public void addConceptToModel(String mainResource, Concept concept,
                                  List<ConceptRelatedConcept> conceptRelatedConcepts) {

        ModelBuilder builder = new ModelBuilder(modelGlobal);
        ValueFactory factory = SimpleValueFactory.getInstance();
        boolean isTopConcept = isConceptTopObject(conceptRelatedConcepts, concept);

        if (isTopConcept) builder.subject(mainResource).add(SKOS.HAS_TOP_CONCEPT, concept.getUri());

        builder.defaultGraph()
                .subject(concept.getUri())
                .add(RDF.TYPE, SKOS.CONCEPT);

        if (!concept.getNotation().isBlank()) {
            builder.subject(concept.getUri()).add(SKOS.NOTATION,
                    factory.createLiteral(concept.getNotation()));
        }

        if (!concept.getDefinitions().isEmpty()) {
            concept.getDefinitions().forEach(
                    (key, value) ->
                            builder.subject(concept.getUri())
                                    .add(SKOS.DEFINITION,
                                            factory.createLiteral(value, key))
            );
        }

        if (isTopConcept) builder.subject(concept.getUri()).add(SKOS.TOP_CONCEPT_OF, mainResource);

        builder.subject(concept.getUri())
                .add(SKOS.IN_SCHEME, mainResource);

        if (!concept.getLabels().isEmpty()) {
            concept.getLabels().forEach(
                    (key, value) ->
                            builder.subject(concept.getUri())
                                    .add(SKOS.PREF_LABEL,
                                            factory.createLiteral(value, key))
            );
        }

        conceptRelatedConcepts.forEach(
                conceptRelated -> {
                    builder.subject(concept.getUri())
                            .add(conceptRelated.getRelation().getUri(), conceptRelated.getObject().getUri());

                    inverseStatements.add(
                            factory.createStatement(
                                    factory.createIRI(conceptRelated.getObject().getUri()),
                                    factory.createIRI(conceptRelated.getRelation().getInverseOf().getUri()),
                                    factory.createIRI(concept.getUri())));
                }
        );

        modelGlobal = builder.build();
    }

    public Model generateInverseStatements() {
        modelGlobal.addAll(inverseStatements);

        return modelGlobal;
    }

    public Model createModel(Vocabulary vocabulary, String mainResource) {

        ModelBuilder builder = new ModelBuilder();
        ValueFactory factory = SimpleValueFactory.getInstance();

        builder.setNamespace(RDF.NS)
                .setNamespace(RDFS.NS)
                .setNamespace(SKOS.NS)
                .setNamespace(SKOSXL.NS)
                .setNamespace(OWL.NS)
                .setNamespace(DC.NS)
                .setNamespace(DCTERMS.NS)
                .setNamespace(XSD.NS)
                .setNamespace(FOAF.NS)
                .setNamespace("tags", PREFIX_TAGS)
                .setNamespace("cycAnnot", PREFIX_CYCANNOT)
                .setNamespace("csw", PREFIX_CSW)
                .setNamespace("dbpedia", PREFIX_DBPEDIA)
                .setNamespace("freebase", PREFIX_FREEBASE)
                .setNamespace("opencyc", PREFIX_OPENCYC)
                .setNamespace("cyc", PREFIX_CYC)
                .setNamespace("ctag", PREFIX_CTAG);


        builder.defaultGraph()
                .subject(mainResource)
                .add(factory.createIRI(PREFIX_CSW + "hierarchyRoot"), true)
                .add(factory.createIRI(PREFIX_CSW + "hierarchyRootType"), SKOS.CONCEPT_SCHEME);

        builder
                .subject(mainResource)
                .add(RDF.TYPE, SKOS.CONCEPT_SCHEME);


        for (Map.Entry<String, String> entry : vocabulary.getLabels().entrySet()) {
            if (entry.getValue().equals(vocabulary.getLabel())) {
                builder
                        .subject(mainResource)
                        .add(SKOS.PREF_LABEL, factory.createLiteral(vocabulary.getLabel(), entry.getKey()));
            }
        }

        if (!StringUtils.isBlank(vocabulary.getDescription())) {
            for (Map.Entry<String, String> entry : vocabulary.getDescriptions().entrySet()) {
                if (entry.getValue().equals(vocabulary.getDescription())) {
                    builder
                            .subject(mainResource)
                            .add(DC_DESCRIPTION, factory.createLiteral(vocabulary.getDescription(), entry.getKey()));
                }
            }
        }

        if (!vocabulary.getTitles().isEmpty()) {
            vocabulary.getTitles().forEach(
                    (key, value) ->
                            builder.subject(mainResource)
                                    .add(DCTERMS.TITLE, factory.createLiteral(value, key))
            );
        }

        if (!vocabulary.getComments().isEmpty()) {
            vocabulary.getComments().forEach(
                    (key, value) ->
                            builder.subject(mainResource)
                                    .add(RDFS.COMMENT, factory.createLiteral(value, key))
            );
        }

        if (!vocabulary.getLabels().isEmpty()) {
            vocabulary.getLabels().forEach(
                    (key, value) ->
                            builder.subject(mainResource)
                                    .add(RDFS.LABEL, factory.createLiteral(value, key))
            );
        }

        if (!vocabulary.getDescriptions().isEmpty()) {
            vocabulary.getDescriptions().forEach(
                    (key, value) ->
                            builder.subject(mainResource)
                                    .add(DC.DESCRIPTION, factory.createLiteral(value, key))
            );
        }

        Model model = builder.build();

        modelGlobal = model;
        inverseStatements = new LinkedList<>();

        return model;
    }

    public boolean isConceptTopObject(List<ConceptRelatedConcept> conceptRelatedConcepts, Concept concept) {

        List<ConceptRelatedConcept> tmp = conceptRelatedConcepts.stream().filter(
                        c -> (c.getSubject().equals(concept) && !c.getObject().equals(concept) && c.getRelation().getUri().equals(SKOS_NARROWER)))
                .collect(Collectors.toList());

        List<ConceptRelatedConcept> broader = conceptRelatedConcepts.stream().filter(
                        c -> (!c.getSubject().equals(concept) && c.getObject().equals(concept) && c.getRelation().getUri().equals(SKOS_BROADER)))
                .collect(Collectors.toList());

        tmp.addAll(broader);

        return tmp.size() == conceptRelatedConcepts.size();
    }

    public Vocabulary recreateVocabulary(String vocabularyCode, Model rdfModel, Vocabulary vocabulary) {
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
}
