package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.domain.media.MediaCategory;
import eu.sshopencloud.marketplace.dto.auth.UserOrder;
import eu.sshopencloud.marketplace.dto.items.ItemOrder;
import eu.sshopencloud.marketplace.dto.search.ActorSearchOrder;
import eu.sshopencloud.marketplace.dto.search.ConceptSearchOrder;
import eu.sshopencloud.marketplace.dto.search.ItemSearchOrder;
import eu.sshopencloud.marketplace.dto.sources.SourceOrder;
import eu.sshopencloud.marketplace.model.auth.UserRole;
import eu.sshopencloud.marketplace.model.auth.UserStatus;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;


@Configuration
public class WebConvertersConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldType(UserRole.class, new UserRoleFormatter());
        registry.addFormatterForFieldType(UserStatus.class, new UserStatusFormatter());
        registry.addFormatterForFieldType(ItemCategory.class, new ItemCategoryFormatter());
        registry.addFormatterForFieldType(ItemStatus.class, new ItemStatusFormatter());
        registry.addFormatterForFieldType(ItemOrder.class, new ItemOrderFormatter());
        registry.addFormatterForFieldType(ItemSearchOrder.class, new ItemSearchOrderFormatter());
        registry.addFormatterForFieldType(PropertyTypeClass.class, new PropertyTypeClassFormatter());
        registry.addFormatterForFieldType(MediaCategory.class, new MediaCategoryFormatter());
        registry.addFormatterForFieldType(SourceOrder.class, new SourceOrderFormatter());
        registry.addFormatterForFieldType(UserOrder.class, new UserOrderFormatter());
        registry.addFormatterForFieldType(ActorSearchOrder.class, new ActorSearchOrderFormatter());
        registry.addFormatterForFieldType(ConceptSearchOrder.class, new ConceptSearchOrderFormatter());
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setUrlDecode(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }

}
