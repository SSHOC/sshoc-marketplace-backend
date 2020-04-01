package eu.sshopencloud.marketplace.services.auth;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        // TODO credentials and others
        return new org.springframework.security.core.userdetails.User(username, null, null);
    }

    public List<User> getUsers(String q, int perpage) {
        ExampleMatcher queryUserMatcher = ExampleMatcher.matchingAny()
                .withMatcher("username", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        User queryUser = new User();
        queryUser.setUsername(q);

        Page<User> users = userRepository.findAll(Example.of(queryUser, queryUserMatcher), PageRequest.of(0, perpage, Sort.by(Sort.Order.asc("username"))));
        return users.getContent();
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + User.class.getName() + " with id " + id));
    }

}
