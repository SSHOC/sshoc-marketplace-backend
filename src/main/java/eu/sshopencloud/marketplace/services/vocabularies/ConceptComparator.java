package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;

import java.util.Comparator;

public class ConceptComparator implements Comparator<Concept> {

    @Override
    public int compare(Concept concept1, Concept concept2) {
        return concept1.getOrd().compareTo(concept2.getOrd());
    }
}
