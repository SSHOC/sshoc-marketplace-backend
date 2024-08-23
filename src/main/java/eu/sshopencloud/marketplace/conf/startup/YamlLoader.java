package eu.sshopencloud.marketplace.conf.startup;

import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.model.actors.ActorSource;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.model.items.ItemSource;
import eu.sshopencloud.marketplace.model.sources.Source;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelation;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@UtilityClass
public class YamlLoader {
    public static Class[] safeClasses = new Class [] { User.class, ActorRole.class, ItemRelation.class, ConceptRelation.class,
    Source.class, ActorSource.class, ItemSource.class, MediaSourceCore.class, PropertyType.class };

    @SuppressWarnings("unchecked")
    public Map<String, List<Object>> loadYamlData(String filepath) {
        ClassLoader classLoader = YamlLoader.class.getClassLoader();
        InputStream dataStream = classLoader.getResourceAsStream(filepath);
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setTagInspector(tag -> Arrays.stream(safeClasses).anyMatch(c -> c.getName().equals(tag.getClassName())));
        return new Yaml(new CustomClassLoaderConstructor(classLoader, loaderOptions)).load(dataStream);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getObjects(Map<String, List<Object>> data, String label) {
        if (data == null) {
            return Collections.emptyList();
        }
        List<Object> objects = data.get(label);
        if (objects == null || objects.isEmpty()) {
            return Collections.emptyList();
        }
        return (List<T>) objects;
    }

}
