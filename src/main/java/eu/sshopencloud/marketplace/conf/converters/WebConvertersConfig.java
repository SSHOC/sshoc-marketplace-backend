package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.domain.media.dto.MediaCategory;
import eu.sshopencloud.marketplace.dto.search.SearchOrder;
import eu.sshopencloud.marketplace.model.auth.UserRole;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConvertersConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldType(UserRole.class, new UserRoleFormatter());
        registry.addFormatterForFieldType(ItemCategory.class, new ItemCategoryFormatter());
        registry.addFormatterForFieldType(ItemStatus.class, new ItemStatusFormatter());
        registry.addFormatterForFieldType(SearchOrder.class, new SearchOrderFormatter());
        registry.addFormatterForFieldType(PropertyTypeClass.class, new PropertyTypeClassFormatter());
    }
}
