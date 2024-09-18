package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.controllers.vocabularies.VocabularyControllerITCase;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.services.vocabularies.exception.VocabularyAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@TestMethodOrder(MethodOrderer.MethodName.class)
@Slf4j
@Transactional
public class VocabularyServiceITCase {


    @Autowired
    private VocabularyService vocabularyService;


    @Test
    public void shouldUpdateExportedVocabulary() throws IOException, VocabularyAlreadyExistsException {
        String vocabularyCode = "discipline";
        InputStream vocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/discipline.ttl");

        Vocabulary vocabulary = vocabularyService.createVocabulary(vocabularyCode, vocabularyStream, false);

        checkDisciplineVocabulary(vocabulary);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        vocabularyService.exportVocabulary(vocabularyCode, out);
        String vocabularySkosTtl = out.toString(StandardCharsets.UTF_8);

        checkDisciplineSkosTtl(vocabularySkosTtl);

        vocabularyService.removeVocabulary(vocabularyCode, false);

        InputStream exportedVocabularyStream = new ByteArrayInputStream(vocabularySkosTtl.getBytes(StandardCharsets.UTF_8));

        Vocabulary recreatedVocabulary = vocabularyService.createVocabulary(vocabularyCode, exportedVocabularyStream, false);

        checkDisciplineVocabulary(recreatedVocabulary);

        // TODO strange exception that cannot create a vocabulary with existing code :/
        //out = new ByteArrayOutputStream();
        //vocabularyService.exportVocabulary(vocabularyCode, out);
        //vocabularySkosTtl = out.toString(StandardCharsets.UTF_8);

        //checkDisciplineSkosTtl(vocabularySkosTtl);
    }


    private void checkDisciplineVocabulary(Vocabulary vocabulary) {
        Concept concept1 = new Concept();
        concept1.setCode("1");
        concept1.setLabel("NATURAL SCIENCES");
        concept1.setLabels(Map.of("de", "NATURWISSENSCHAFTEN", "en", "NATURAL SCIENCES"));
        concept1.setNotation("1");
        concept1.setOrd(1);
        concept1.setDefinition(null);
        concept1.setDefinitions(Collections.emptyMap());
        concept1.setUri("https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/1");
        concept1.setVocabulary(vocabulary);

        Concept concept107 = new Concept();
        concept107.setCode("107");
        concept107.setLabel("Other Natural Sciences");
        concept107.setLabels(Map.of("de", "Andere Naturwissenschaften", "en", "Other Natural Sciences"));
        concept107.setNotation("107");
        concept107.setOrd(2);
        concept107.setDefinition(null);
        concept107.setDefinitions(Collections.emptyMap());
        concept107.setUri("https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/107");
        concept107.setVocabulary(vocabulary);

        assertThat(vocabulary.getCode(), is("discipline"));
        assertThat(vocabulary.getScheme(), is("https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/Schema"));
        assertThat(vocabulary.getNamespace(), is("https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/"));
        assertThat(vocabulary.getLabel(), is("ÖFOS 2012. Austrian Fields of Science and Technology Classification 2012"));
        assertThat(vocabulary.getConcepts().get(0), is(concept1));
        assertThat(vocabulary.getConcepts().get(1), is(concept107));
    }

    private void checkDisciplineSkosTtl(String vocabularySkosTtl) {
        assertThat(vocabularySkosTtl, startsWith("@prefix : <https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/> ."));
        assertThat(vocabularySkosTtl, containsString("<https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/Schema> a skos:ConceptScheme;"));
        assertThat(vocabularySkosTtl, containsString("skos:hasTopConcept \"https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/1\""));
        assertThat(vocabularySkosTtl, containsString("skos:hasTopConcept \"https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/2\""));
        assertThat(vocabularySkosTtl, containsString("skos:hasTopConcept \"https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/3\""));
        assertThat(vocabularySkosTtl, containsString("skos:hasTopConcept \"https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/4\""));
        assertThat(vocabularySkosTtl, containsString("skos:hasTopConcept \"https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/5\""));
        assertThat(vocabularySkosTtl, containsString("skos:hasTopConcept \"https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/6\""));
        assertThat(vocabularySkosTtl, not(containsString("skos:hasTopConcept \"https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/107\"")));
        assertThat(vocabularySkosTtl, containsString("<https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/1> a skos:Concept;"));
        assertThat(vocabularySkosTtl, containsString("<https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/107> a skos:Concept;"));
        assertThat(vocabularySkosTtl, containsString("<https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/107> skos:broader <https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/1> ."));
    }

}
