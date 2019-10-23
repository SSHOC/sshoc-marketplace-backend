package eu.sshopencloud.marketplace.services.auth;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getUsers(String q) {
        ExampleMatcher queryUserMatcher = ExampleMatcher.matchingAny()
                .withMatcher("username", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        User queryUser = new User();
        queryUser.setUsername(q);

        return userRepository.findAll(Example.of(queryUser, queryUserMatcher), new Sort(Sort.Direction.ASC, "username"));
    }
}
