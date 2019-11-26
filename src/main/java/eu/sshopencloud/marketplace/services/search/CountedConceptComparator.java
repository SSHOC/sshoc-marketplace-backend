package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.dto.search.CountedConcept;

import java.util.Comparator;

public class CountedConceptComparator implements Comparator<CountedConcept> {

    @Override
    public int compare(CountedConcept concept1, CountedConcept concept2) {
        return concept1.getOrd().compareTo(concept2.getOrd());
    }

}
