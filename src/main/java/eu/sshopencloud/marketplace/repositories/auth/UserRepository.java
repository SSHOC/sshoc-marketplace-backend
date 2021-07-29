package eu.sshopencloud.marketplace.repositories.auth;

import eu.sshopencloud.marketplace.model.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByEmail(String email);

    @Query("select u from User u" +
            " where lower(u.username) like lower(concat('%', :q,'%'))" +
            " or lower(u.displayName) like lower(concat('%', :q,'%'))" +
            " or lower(u.email) like lower(concat('%', :q,'%'))" )
    Page<User> findLikeUsernameOrDisplayNameOrEmail(String q, Pageable pageable);

    @Query(value =
            "WITH RECURSIVE sub_item AS ( " +
                    "   WITH RECURSIVE merge_item AS (" +
                    "       SELECT v.id, v.merged_with_id" +
                    "       FROM versioned_items v" +
                    "       INNER JOIN items i ON i.persistent_id = v.id" +
                    "       WHERE v.merged_with_id = :persistentId OR i.persistent_id = :persistentId " +
                    "       UNION" +
                    "       SELECT v.id, v.merged_with_id " +
                    "       FROM versioned_items v, merge_item m" +
                    "       WHERE m.id = v.merged_with_id)" +
                    "   SELECT i.persistent_id , i.id,  i.prev_version_id" +
                    "   FROM merge_item m" +
                    "   INNER JOIN items i " +
                    "   ON i.persistent_id  = m.id " +
                    "   UNION" +
                    "   SELECT i.persistent_id, i.id, i.prev_version_id" +
                    "   FROM items i, sub_item si" +
                    "   WHERE i.id = si.prev_version_id)" +

                    "SELECT DISTINCT(u.id), u.username, u.display_name, u.password,  u.status,u.registration_date, u.role, u.provider, u.token_key, u.email, u.config, u.preferences " +
                    "FROM Users u " +
                    "INNER JOIN items i \n" +
                    "ON u.id = i.info_contributor_id\n" +
                    "INNER JOIN sub_item si\n" +
                    "ON i.persistent_id = si.persistent_id AND si.id = i.id" , nativeQuery = true
    )
    List<User> findInformationContributors(@Param("persistentId" ) String persistentId);



    @Query(value =
            "WITH RECURSIVE sub_item AS ( " +
                    "   WITH RECURSIVE merge_item AS (" +
                    "       SELECT v.id, v.merged_with_id" +
                    "       FROM versioned_items v" +
                    "       INNER JOIN items i ON i.persistent_id = v.id" +
                    "       WHERE v.merged_with_id = :persistentId OR (i.persistent_id = :persistentId and i.id = :versionId) " +
                    "       UNION" +
                    "       SELECT v.id, v.merged_with_id " +
                    "       FROM versioned_items v, merge_item m" +
                    "       WHERE m.id = v.merged_with_id)" +
                    "   SELECT i.persistent_id , i.id,  i.prev_version_id" +
                    "   FROM merge_item m" +
                    "   INNER JOIN items i " +
                    "   ON i.persistent_id  = m.id " +
                    "   UNION" +
                    "   SELECT i.persistent_id, i.id, i.prev_version_id" +
                    "   FROM items i, sub_item si" +
                    "   WHERE i.id = si.prev_version_id)" +

                    "SELECT DISTINCT(u.id), u.username, u.display_name, u.password,  u.status,u.registration_date, u.role, u.provider, u.token_key, u.email, u.config, u.preferences " +
                    "FROM Users u " +
                    "INNER JOIN items i \n" +
                    "ON u.id = i.info_contributor_id\n" +
                    "INNER JOIN sub_item si\n" +
                    "ON i.persistent_id = si.persistent_id AND si.id = i.id", nativeQuery = true
    )
    List<User> findInformationContributors(@Param("persistentId" ) String persistentId, @Param("versionId" ) Long versionId);


}
