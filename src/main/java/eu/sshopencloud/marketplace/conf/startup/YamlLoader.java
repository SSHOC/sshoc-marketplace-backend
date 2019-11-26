package eu.sshopencloud.marketplace.conf.startup;

import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@UtilityClass
public class YamlLoader {

    public Map<String, List<Object>> loadYamlData(String filepath) {
        ClassLoader classLoader = YamlLoader.class.getClassLoader();
        InputStream dataStream = classLoader.getResourceAsStream(filepath);
        return (Map<String, List<Object>>) new Yaml(new CustomClassLoaderConstructor(classLoader)).load(dataStream);
    }

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
