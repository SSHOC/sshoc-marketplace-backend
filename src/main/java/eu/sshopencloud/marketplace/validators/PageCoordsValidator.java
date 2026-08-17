package eu.sshopencloud.marketplace.validators;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.PageCoords;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PageCoordsValidator {

    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;

    @Value("${marketplace.pagination.maximal-perpage}")
    private Integer maximalPerpage;

    public PageCoords validate(Integer page, Integer perpage) throws PageTooLargeException {
        if (perpage != null && perpage > maximalPerpage) {
            throw new PageTooLargeException(maximalPerpage);
        }

        if (page != null && page <= 0) {
            throw new IllegalArgumentException("Page index must be 1 or more");
        }

        return PageCoords.builder()
                .perpage(perpage == null ? defualtPerpage : perpage)
                .page(page == null ? 1 : page)
                .build();
    }

}
