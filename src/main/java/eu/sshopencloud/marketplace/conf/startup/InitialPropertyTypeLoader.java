package eu.sshopencloud.marketplace.conf.startup;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialPropertyTypeLoader {

    private final PropertyTypeRepository propertyTypeRepository;

    public void loadPropertyTypeData() {
        log.debug("Loading property type data");
        Map<String, List<Object>> data = YamlLoader.loadYamlData("initial-data/property-type-data.yml");

        long propertyTypesCount = propertyTypeRepository.count();
        if (propertyTypesCount == 0) {
            List<PropertyType> propertyTypes = YamlLoader.getObjects(data, "PropertyType");
            propertyTypeRepository.saveAll(propertyTypes);
            log.debug("Loaded " + propertyTypes.size() + " PropertyType objects");
        } else {
            log.debug("Skipped loading property types. {} already present.", propertyTypesCount);
        }
    }

}
