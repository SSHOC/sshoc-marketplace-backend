package eu.sshopencloud.marketplace;

import eu.sshopencloud.marketplace.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class Examples {
    @Test
    public void Examples() throws MalformedURLException {
        Tool gephi = new Tool("Gephi");
        gephi.setAccessibleAt(new URL("https://gephi.org/"));
        gephi.setDescription("Visualization and exploration software for all kinds of graphs and networks");
        gephi.setLastInfoUpdate(new Date());
        gephi.setLicense(new URL("http://www.opensource.org/licenses/CDDL-1.0"));
        MarketplaceEntity source = new DigitalObject("sourceOfGephi");
        source.setDescription("This is a source as InformationObject, but it could be another Tool if needed - just " +
                "needs to be a subclass of Item...");
        Relation relation = new Relation();
        relation.setType("introduction");
        relation.setMarketplaceEntity(source);
        gephi.setRelatedItems(Collections.singletonList(relation));
        PropertyType propertyType = new PropertyType();
        Vocabulary tadirah = new Vocabulary("TaDiRAH");
        tadirah.setDescription("Taxonomy of Digital Research Activities in the Humanities");
        propertyType.setAllowedValues(Collections.singletonList(tadirah));
        propertyType.setLabel("annotation-layer");
        Property property = new Property();
        property.setKey(propertyType);
        Concept concept = new Concept();
        concept.setPreLabel("Annotating");
        concept.setDefinition("Annotating refers to the activity of making information about a digital object explicit by adding, e.g., comments, metadata or keywords to a digitized representation or to an annotation file associated with it. This can be in the form of annotations that comment on or contextualize a passage (explanatory annotations) in order to make structural or linguistic information explicit (structural/linguistic annotation), as linked open data making the relationships between objects machine-readable, or, in the case of general metadata, adding information about the object as a whole. Encoding is a technique associated with annotating, as are POS-Tagging, Tree-Tagging, and Georeferencing.");
        concept.setUrl(new URL("http://tadirah.dariah.eu/vocab/index.php?tema=22&/annotating"));
        property.setConcept(concept);
        gephi.setOutputProperties(Collections.singletonList(property));
        property = new Property();
        property.setKey(propertyType);
        property.setValue("Another annotation property that does not exist in a vocabulary");
        gephi.setInputProperties(Collections.singletonList(property));

        Assert.notNull(gephi, "The Tool instance should not be null");
    }
}
