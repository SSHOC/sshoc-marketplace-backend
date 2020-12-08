package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.dto.items.ItemCommentCore;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.ItemComment;
import eu.sshopencloud.marketplace.services.text.MarkdownConverter;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import java.time.ZonedDateTime;


@Service
@Transactional
@RequiredArgsConstructor
public class ItemCommentFactory {

    public ItemComment create(ItemCommentCore itemCommentCore, User creator) throws ValidationException {
        ItemComment itemComment = new ItemComment();
        setCommentData(itemCommentCore, itemComment);

        ZonedDateTime now = ZonedDateTime.now();
        itemComment.setDateCreated(now);
        itemComment.setDateLastUpdated(now);

        itemComment.setCreator(creator);

        return itemComment;
    }

    public ItemComment update(ItemCommentCore itemCommentCore, ItemComment itemComment) throws ValidationException {
        setCommentData(itemCommentCore, itemComment);

        ZonedDateTime now = ZonedDateTime.now();
        itemComment.setDateLastUpdated(now);

        return itemComment;
    }

    private void setCommentData(ItemCommentCore itemCommentCore, ItemComment itemComment) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(itemCommentCore, "ItemComment");

        if (StringUtils.isBlank(itemCommentCore.getBody()))
            errors.rejectValue("body", "field.required", "Body is required.");
        else
            itemComment.setBody(MarkdownConverter.convertHtmlToMarkdown(itemCommentCore.getBody()));

        if (errors.hasErrors())
            throw new ValidationException(errors);
    }
}
