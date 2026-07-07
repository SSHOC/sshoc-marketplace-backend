package eu.sshopencloud.marketplace.model.skg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class JsonLdDocument {

    @JsonProperty("@context")
    private final List<Object> context;

    @JsonProperty("@graph")
    private final List<Object> graph;

    public JsonLdDocument(String baseUri) {
        this.context = buildContext(baseUri);
        this.graph = new ArrayList<>();
    }

    private List<Object> buildContext(String baseUri) {
        List<Object> ctx = new ArrayList<>();
        ctx.add("https://w3id.org/skg-if/context/skg-if.json");

        Map<String, Object> base = new LinkedHashMap<>();
        base.put("@base", baseUri);
        ctx.add(base);

        return ctx;
    }

    public JsonLdDocument addEntity(Object entity) {
        this.graph.add(entity);
        return this;
    }
}
