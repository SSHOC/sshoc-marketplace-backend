package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.controllers.items.dto.ItemCommentCore;
import eu.sshopencloud.marketplace.model.items.ItemComment;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemCommentConverter {

    public ItemComment convert(ItemCommentCore itemComment) {
        ItemComment result = new ItemComment();
        result.setBody(itemComment.getBody());
        return result;
    }

}
