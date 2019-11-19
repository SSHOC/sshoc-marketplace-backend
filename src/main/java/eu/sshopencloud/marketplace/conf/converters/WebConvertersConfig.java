package eu.sshopencloud.marketplace.conf.converters;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebConvertersConfig extends WebMvcConfigurationSupport {

    @Override
    public FormattingConversionService mvcConversionService() {
        FormattingConversionService conversionService = super.mvcConversionService();
        conversionService.addConverter(new WebSearchOrderConverter());
        conversionService.addConverter(new WebItemCategoryConverter());
        return conversionService;
    }

}
