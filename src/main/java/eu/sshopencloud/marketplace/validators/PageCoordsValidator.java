package eu.sshopencloud.marketplace.validators;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.PageCoords;

public class PageCoordsValidator {

    private Integer defaultPerpage;

    private Integer maximalPerpage;

    public PageCoordsValidator(Integer defaultPerpage, Integer maximalPerpage) {
        this.defaultPerpage = defaultPerpage;
        this.maximalPerpage = maximalPerpage;
    }

    public PageCoords validate(Integer page, Integer perpage) throws PageTooLargeException {
        if (perpage != null && perpage > maximalPerpage) {
            throw new PageTooLargeException(maximalPerpage);
        }
        return PageCoords.builder()
                .perpage(perpage == null ? defaultPerpage : perpage)
                .page(page == null ? 1 : page)
                .build();
    }

}
