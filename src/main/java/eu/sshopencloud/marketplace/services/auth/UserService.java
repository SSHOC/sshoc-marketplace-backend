package eu.sshopencloud.marketplace.services.auth;

import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.mappers.auth.UserMapper;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public List<UserDto> getUsers(String q, int perpage) {
        ExampleMatcher queryUserMatcher = ExampleMatcher.matchingAny()
                .withMatcher("username", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        User queryUser = new User();
        queryUser.setUsername(q);

        Page<User> usersPage = userRepository.findAll(Example.of(queryUser, queryUserMatcher), PageRequest.of(0, perpage, Sort.by(Sort.Order.asc("username"))));
        return UserMapper.INSTANCE.toDto(usersPage.getContent());
    }

    public UserDto getUser(Long id) {
        return UserMapper.INSTANCE.toDto(userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + User.class.getName() + " with id " + id)));
    }

}
