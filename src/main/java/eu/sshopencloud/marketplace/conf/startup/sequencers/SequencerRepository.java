package eu.sshopencloud.marketplace.conf.startup.sequencers;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class SequencerRepository {

    @PersistenceContext
    private EntityManager entityManager;

    long getTableMaxId(String tableName) {
        var resultList = entityManager.createNativeQuery("SELECT max(id) FROM " + tableName).getResultList();
        if (resultList.isEmpty() || resultList.get(0) == null) {
            return 0;
        } else {
            return Long.parseLong(resultList.get(0).toString());
        }
    }

    void setSequencerLastValue(String sequencerName, long value) {
        entityManager.createNativeQuery("ALTER SEQUENCE " + sequencerName + " RESTART WITH " + value).executeUpdate();
    }

}
