package eu.sshopencloud.marketplace.services.items;


import eu.sshopencloud.marketplace.model.items.ItemComment;

public class OtherUserCommentException extends Exception {

    public OtherUserCommentException(ItemComment itemComment) {
        super("Item comment " + itemComment.getId() + " has been added by other user " + itemComment.getCreator().getUsername() + "!");
    }

}
