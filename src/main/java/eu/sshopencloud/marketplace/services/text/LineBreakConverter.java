package eu.sshopencloud.marketplace.services.text;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class LineBreakConverter {

    public String removeLineBreaks(String text) {

        String textWithoutBreaks = text.replaceAll("\n", "");
        textWithoutBreaks = textWithoutBreaks.replaceAll("\r", "");
        return textWithoutBreaks;
    }
}
