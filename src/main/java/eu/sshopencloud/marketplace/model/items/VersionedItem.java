package eu.sshopencloud.marketplace.model.items;

import lombok.*;

import javax.persistence.*;
import java.util.*;


@Entity
@Table(name = "versioned_items")
@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(exclude = "comments")
@EqualsAndHashCode(exclude = "comments")
public class VersionedItem {

    @Id
    @Column(name = "id")
    private String persistentId;

    @Version
    @Column(name = "optlock")
    private Long entityVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VersionedItemStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curr_ver_id", foreignKey = @ForeignKey(name = "versioned_item_curr_version_fk"))
    private Item currentVersion;

    @Column(name = "active", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateCreated DESC")
    private List<ItemComment> comments;

    //Eliza
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id", foreignKey = @ForeignKey(name="merged_with_fk"))
    List<VersionedItem> mergedWith;


    public VersionedItem(String persistentId) {
        this.persistentId = persistentId;
        this.active = true;
        this.comments = new ArrayList<>();
    }

    public boolean hasAnyVersions() {
        return (currentVersion != null);
    }

    public ItemComment getLatestComment() {
        return comments.get(0);
    }

    public void addComment(ItemComment comment) {
        comments.add(0, comment);
        comment.setItem(this);
    }

    public Optional<ItemComment> findComment(long commentId) {
        return comments.stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst();
    }

    public boolean removeComment(long commentId) {
        Optional<ItemComment> comment = findComment(commentId);

        if (comment.isEmpty())
            return false;

        comments.remove(comment.get());
        return true;
    }

    public List<ItemComment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public boolean areCommentsAllowed() {
        return (isActive() && status != VersionedItemStatus.DRAFT);
    }
}
