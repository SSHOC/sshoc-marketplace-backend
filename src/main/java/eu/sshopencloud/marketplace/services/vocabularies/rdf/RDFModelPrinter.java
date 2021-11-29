package eu.sshopencloud.marketplace.services.vocabularies.rdf;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RDFModelPrinter {

    private static final String DC_DESCRIPTION = "http://purl.org/dc/elements/1.1/description";

    private static final String SKOS_BROADER = "http://www.w3.org/2004/02/skos/core#broader";

    private static final String SKOS_NARROWER = "http://www.w3.org/2004/02/skos/core#narrower";

    public static final String PREFIX_CSW = "http://semantic-web.at/ontologies/csw.owl#";

    Model model;

    List<Statement> inverseStatements;

    public Model getModel() {
        return model;
    }

    @Bean
    @RequestScope
    public RDFModelPrinter requestScopedBean() {
        return new RDFModelPrinter();
    }

    public List<Statement> getInverseStatements() {
        return inverseStatements;
    }

    public void setInverseStatements(List<Statement> inverseStatements) {
        this.inverseStatements = inverseStatements;
    }

    public void createModel(Vocabulary vocabulary) {

        ModelBuilder builder = new ModelBuilder();
        ValueFactory factory = SimpleValueFactory.getInstance();
        setInverseStatements(new LinkedList<>());

        vocabulary.getNamespaces()
                .forEach((key, value) -> {
                            if (!value.equals(vocabulary.getNamespace()) && !value.equals(vocabulary.getScheme()) && !key.isBlank()) {
                                builder.setNamespace(key, value);
                            }
                        }
                );

        builder.defaultGraph()
                .subject(vocabulary.getScheme())
                .add(factory.createIRI(PREFIX_CSW + "hierarchyRoot"), true)
                .add(factory.createIRI(PREFIX_CSW + "hierarchyRootType"), SKOS.CONCEPT_SCHEME);

        builder
                .subject(vocabulary.getScheme())
                .add(RDF.TYPE, SKOS.CONCEPT_SCHEME);


        for (Map.Entry<String, String> entry : vocabulary.getLabels().entrySet()) {
            if (entry.getValue().equals(vocabulary.getLabel())) {
                builder
                        .subject(vocabulary.getScheme())
                        .add(SKOS.PREF_LABEL, factory.createLiteral(vocabulary.getLabel(), entry.getKey()));
            }
        }

        if (!StringUtils.isBlank(vocabulary.getDescription())) {
            for (Map.Entry<String, String> entry : vocabulary.getDescriptions().entrySet()) {
                if (entry.getValue().equals(vocabulary.getDescription())) {
                    builder
                            .subject(vocabulary.getScheme())
                            .add(DC_DESCRIPTION, factory.createLiteral(vocabulary.getDescription(), entry.getKey()));
                }
            }
        }

        if (!vocabulary.getTitles().isEmpty()) {
            vocabulary.getTitles().forEach(
                    (key, value) ->
                            builder.subject(vocabulary.getScheme())
                                    .add(DCTERMS.TITLE, factory.createLiteral(value, key))
            );
        }

        if (!vocabulary.getComments().isEmpty()) {
            vocabulary.getComments().forEach(
                    (key, value) ->
                            builder.subject(vocabulary.getScheme())
                                    .add(RDFS.COMMENT, factory.createLiteral(value, key))
            );
        }

        if (!vocabulary.getLabels().isEmpty()) {
            vocabulary.getLabels().forEach(
                    (key, value) ->
                            builder.subject(vocabulary.getScheme())
                                    .add(RDFS.LABEL, factory.createLiteral(value, key))
            );
        }

        if (!vocabulary.getDescriptions().isEmpty()) {
            vocabulary.getDescriptions().forEach(
                    (key, value) ->
                            builder.subject(vocabulary.getScheme())
                                    .add(DC.DESCRIPTION, factory.createLiteral(value, key))
            );
        }

        model = builder.build();

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

    public void addConceptToModel(String scheme, Concept concept,
                                  List<ConceptRelatedConcept> conceptRelatedConcepts) {

        ModelBuilder builder = new ModelBuilder(model);
        ValueFactory factory = SimpleValueFactory.getInstance();
        boolean isTopConcept = isConceptTopObject(conceptRelatedConcepts, concept);

        if (isTopConcept) builder.subject(scheme).add(SKOS.HAS_TOP_CONCEPT, concept.getUri());

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

        if (isTopConcept) builder.subject(concept.getUri()).add(SKOS.TOP_CONCEPT_OF, scheme);

        builder.subject(concept.getUri())
                .add(SKOS.IN_SCHEME, scheme);

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
                    if (conceptRelated.getSubject().equals(concept)) {
                        builder.subject(concept.getUri())
                                .add(conceptRelated.getRelation().getUri(), conceptRelated.getObject().getUri());

                        Statement s = factory.createStatement(
                                factory.createIRI(conceptRelated.getObject().getUri()),
                                factory.createIRI(conceptRelated.getRelation().getInverseOf().getUri()),
                                factory.createIRI(concept.getUri()));
                        if (!inverseStatements.contains(s))
                            inverseStatements.add(s);
                    }

                }
        );


        builder.build();
    }

    public void generateInverseStatements() {
        model.addAll(inverseStatements);
    }

}
