package eu.sshopencloud.marketplace.services.vocabularies.rdf;

import java.util.HashMap;
import java.util.Map;

public class RDFResources {
    //prefixes
    public static final String PREFIX_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    public static final String PREFIX_RDFS = "http://www.w3.org/2000/01/rdf-schema#";

    public static final String PREFIX_SKOS = "http://www.w3.org/2004/02/skos/core#";

    public static final String PREFIX_SKOSXL = "http://www.w3.org/2008/05/skos-xl#";

    public static final String PREFIX_OWL = "http://www.w3.org/2002/07/owl#";

    public static final String PREFIX_DC = "http://purl.org/dc/elements/1.1/";

    public static final String PREFIX_DCTERMS = "http://purl.org/dc/terms/";

    public static final String PREFIX_XSD = "http://www.w3.org/2001/XMLSchema#";

    public static final String PREFIX_TAGS = "http://www.holygoat.co.uk/owl/redwood/0.1/tags/";

    public static final String PREFIX_FOAF = "http://xmlns.com/foaf/0.1/";

    public static final String PREFIX_CYCANNOT = "http://sw.cyc.com/CycAnnotations_v1#";

    public static final String PREFIX_CSW = "http://semantic-web.at/ontologies/csw.owl#";

    public static final String PREFIX_DBPEDIA = "http://dbpedia.org/resource/";

    public static final String PREFIX_FREEBASE = "http://rdf.freebase.com/ns/";

    public static final String PREFIX_OPENCYC = "http://sw.opencyc.org/concept/";

    public static final String PREFIX_CYC = "http://sw.cyc.com/concept/";

    public static final String PREFIX_CTAG = "http://commontag.org/ns#";

    Map<String, String> namespacePrefixes;

    public String uri;

    public String getURI() {
        return uri;
    }

    public RDFResources(String uri) {
        namespacePrefixes = new HashMap<>();
        this.uri = uri;

    }

    public void generateNamespacePrefixes(String uri) {
        namespacePrefixes.put("", uri);
        namespacePrefixes.put("rdf", PREFIX_RDF);
        namespacePrefixes.put("rdfs", PREFIX_RDFS);
        namespacePrefixes.put("skos", PREFIX_SKOS);
        namespacePrefixes.put("skosxl", PREFIX_SKOSXL);
        namespacePrefixes.put("owl", PREFIX_OWL);
        namespacePrefixes.put("dc", PREFIX_DC);
        namespacePrefixes.put("dcterms", PREFIX_DCTERMS);
        namespacePrefixes.put("xsd", PREFIX_XSD);
        namespacePrefixes.put("tags", PREFIX_TAGS);
        namespacePrefixes.put("foaf", PREFIX_FOAF);
        namespacePrefixes.put("cycAnnot", PREFIX_CYCANNOT);
        namespacePrefixes.put("csw", PREFIX_CSW);
        namespacePrefixes.put("dbpedia", PREFIX_DBPEDIA);
        namespacePrefixes.put("freebase", PREFIX_FREEBASE);
        namespacePrefixes.put("opencyc", PREFIX_OPENCYC);
        namespacePrefixes.put("cyc", PREFIX_CYC);
        namespacePrefixes.put("ctag", PREFIX_CTAG);
    }


    public void setNamespacePrefix(String prefix) {
        namespacePrefixes.put("", prefix);
    }

    public Map<String, String> getNamespacePrefixes() {
        return namespacePrefixes;
    }

    public void setNamespacePrefixes(Map<String, String> namespacePrefixes) {
        this.namespacePrefixes = namespacePrefixes;
    }

}
