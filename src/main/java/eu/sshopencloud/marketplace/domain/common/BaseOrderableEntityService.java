package eu.sshopencloud.marketplace.domain.common;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public abstract class BaseOrderableEntityService<T extends OrderableEntity<Id>, Id> {

    protected List<T> loadAllEntries() {
        return getEntityRepository().findAll(Sort.by(Sort.Order.asc("ord")));
    }

    protected void placeEntryAtPosition(T entry, Integer ord, boolean insert) {

        if (ord == null)ord = (int) getEntityRepository().count();

        validateEntryPosition(ord, insert);
        entry.setOrd(ord);

        fixEntriesOrder(entry.getId(), ord);
    }

    protected void removeEntryFromPosition(Id entryId) {
        fixEntriesOrder(entryId, null);
    }

    private void fixEntriesOrder(Id modifiedEntryId, Integer modifiedEntryOrd) {
        int ord = 1;

        for (T entry : loadAllEntries()) {
            if (entry.getId().equals(modifiedEntryId))
                continue;

            if (modifiedEntryOrd != null && ord == modifiedEntryOrd)
                ord++;

            if  (entry.getOrd() != ord)
                entry.setOrd(ord);

            ord++;
        }
    }

    private void validateEntryPosition(Integer ord, boolean insert) {
        long entriesCount = getEntityRepository().count();
        int maxPosition = (int) entriesCount;

        if (insert)
            maxPosition += 1;


        if (ord < 1 || ord > maxPosition) {
            throw new IllegalArgumentException(
                    String.format("Invalid position index %d (index range from 1 to %d)", ord, maxPosition)
            );
        }
    }

    protected abstract JpaRepository<T, Id> getEntityRepository();
}
