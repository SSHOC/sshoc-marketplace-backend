package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.dto.items.ItemCommentCore;
import eu.sshopencloud.marketplace.model.items.ItemComment;
import eu.sshopencloud.marketplace.repositories.items.ItemCommentRepository;
import eu.sshopencloud.marketplace.services.text.MarkdownConverter;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemCommentValidator {

    private final ItemCommentRepository itemCommentRepository;

    public ItemComment validate(ItemCommentCore itemCommentCore, Long id) throws ValidationException {
        ItemComment itemComment = getOrCreateItemComment(id);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(itemCommentCore, "ItemComment");

        if (StringUtils.isBlank(itemCommentCore.getBody())) {
            errors.rejectValue("body", "field.required", "Body is required.");
        } else {
            itemComment.setBody(MarkdownConverter.convertHtmlToMarkdown(itemCommentCore.getBody()));
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return itemComment;
        }
    }

    private ItemComment getOrCreateItemComment(Long id) {
        if (id != null) {
            return itemCommentRepository.getOne(id);
        } else {
            return new ItemComment();
        }
    }

}
