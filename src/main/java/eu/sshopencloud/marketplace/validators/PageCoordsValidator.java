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

        if (page != null && page <= 0) {
            // this is probably overkill given the use of @Schema on the controllers
            // but if something does sneak past that then this catches the out of bounds
            // error which would otherwise be caught when calling PageRequest.of()
            // and this ensures that the resulting error message is now correct, rather
            // than it stating it "must not be less than zero" when the caller provided
            // a zero value. In an ideal world the API would use zero based indexes for
            // the page ranges and the UI would add one, rather than us having to take
            // one away every time. Note that we throw an IllegalArgumentException so
            // as not to change the signature of the method and maintain previous behaviour
            throw new IllegalArgumentException("Page index must be 1 or more");
        }

        return PageCoords.builder()
                .perpage(perpage == null ? defaultPerpage : perpage)
                .page(page == null ? 1 : page)
                .build();
    }

}
