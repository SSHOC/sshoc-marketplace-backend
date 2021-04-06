package eu.sshopencloud.marketplace.repositories.auth;

import eu.sshopencloud.marketplace.model.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    @Query("select u from User u" +
            " where lower(u.username) like lower(concat('%', :q,'%'))" +
            " or lower(u.displayName) like lower(concat('%', :q,'%'))" +
            " or lower(u.email) like lower(concat('%', :q,'%'))")
    Page<User> findLikeUsernameOrDisplayNameOrEmail(String q, Pageable pageable);

}
