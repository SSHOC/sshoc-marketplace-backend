package eu.sshopencloud.marketplace.services.auth;

import eu.sshopencloud.marketplace.conf.auth.ImplicitGrantTokenProvider;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.auth.PaginatedUsers;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.auth.OAuthRegistrationDto;
import eu.sshopencloud.marketplace.mappers.auth.UserMapper;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final ImplicitGrantTokenProvider implicitGrantTokenProvider;


    public PaginatedUsers getUsers(String q, PageCoords pageCoords) {
        ExampleMatcher queryUserMatcher = ExampleMatcher.matchingAny()
                .withMatcher("username", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        User queryUser = new User();
        queryUser.setUsername(q);

        Page<User> usersPage = userRepository.findAll(Example.of(queryUser, queryUserMatcher),
                PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("username"))));

        List<UserDto> users = usersPage.stream().map(UserMapper.INSTANCE::toDto).collect(Collectors.toList());

        return PaginatedUsers.builder().users(users)
                .count(usersPage.getContent().size()).hits(usersPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(usersPage.getTotalPages())
                .build();
    }

    public UserDto getUser(Long id) {
        return UserMapper.INSTANCE.toDto(userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + User.class.getName() + " with id " + id)));
    }

    public OAuthRegistrationDto getOAuthRegistration(String token) throws InvalidTokenException {
        Long userId = implicitGrantTokenProvider.getUserIdFromToken(token);
        return UserMapper.INSTANCE.toOAuthRegistrationDto(userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + User.class.getName() + " with id " + userId)));
    }

}
