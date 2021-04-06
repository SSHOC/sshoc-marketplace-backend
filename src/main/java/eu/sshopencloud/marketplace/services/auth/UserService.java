package eu.sshopencloud.marketplace.services.auth;

import eu.sshopencloud.marketplace.conf.auth.ImplicitGrantTokenProvider;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.auth.PaginatedUsers;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.auth.OAuthRegistrationDto;
import eu.sshopencloud.marketplace.mappers.auth.UserMapper;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.auth.UserRole;
import eu.sshopencloud.marketplace.model.auth.UserStatus;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        Page<User> usersPage;
        if (StringUtils.isBlank(q)) {
            usersPage = userRepository.findAll(
                    PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("username"))));
        } else {
            usersPage = userRepository.findLikeUsernameOrDisplayNameOrEmail(q,
                    PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("username"))));
        }

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

    public UserDto getLoggedInUser() {
        return UserMapper.INSTANCE.toDto(userRepository.findById(LoggedInUserHolder.getLoggedInUser().getId()).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + User.class.getName() + " for logged in user id")));
    }

    public OAuthRegistrationDto getOAuthRegistration(String token) throws InvalidTokenException {
        Long userId = implicitGrantTokenProvider.getUserIdFromToken(token);
        return UserMapper.INSTANCE.toOAuthRegistrationDto(userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + User.class.getName() + " with id " + userId)));
    }

    public User loadLoggedInUser() {
        User loggedUser = LoggedInUserHolder.getLoggedInUser();
        return userRepository.findByUsername(loggedUser.getUsername());
    }

    public UserDto updateUserStatus(long id, UserStatus status) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + User.class.getName() + " with id " + id));
        if (status != UserStatus.DURING_REGISTRATION) {
            user.setStatus(status);
            user = userRepository.save(user);
        }
        return UserMapper.INSTANCE.toDto(user);
    }

    public UserDto updateUserRole(long id, UserRole role) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + User.class.getName() + " with id " + id));
        user.setRole(role);
        user = userRepository.save(user);
        return UserMapper.INSTANCE.toDto(user);
    }


}
