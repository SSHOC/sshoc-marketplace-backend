package eu.sshopencloud.marketplace.services.auth;

import eu.sshopencloud.marketplace.conf.auth.ImplicitGrantTokenProvider;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.auth.*;
import eu.sshopencloud.marketplace.mappers.auth.UserMapper;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.auth.UserRole;
import eu.sshopencloud.marketplace.model.auth.UserStatus;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.validators.auth.PasswordValidator;
import eu.sshopencloud.marketplace.validators.auth.UserFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserFactory userFactory;

    private final PasswordValidator passwordValidator;

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

    public UserDto createUser(UserCore userCore) {
        User user = userFactory.create(userCore);
        user.setStatus(UserStatus.ENABLED);
        user.setRegistrationDate(ZonedDateTime.now());
        user.setConfig(true);
        user.setPreferences("{}");
        userRepository.save(user);

        return UserMapper.INSTANCE.toDto(user);
    }

    public UserDto updateUserPassword(long id, NewPasswordData newPasswordData) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + User.class.getName() + " with id " + id));
        checkPrivileges(user);
        String password = passwordValidator.validate(newPasswordData, id);
        user.setPassword(password);
        user = userRepository.save(user);
        return UserMapper.INSTANCE.toDto(user);
    }

    public UserDto updateUserDisplayName(long id, UserDisplayNameCore displayNameCore) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + User.class.getName() + " with id " + id));
        checkPrivileges(user);
        if (StringUtils.isNotBlank(displayNameCore.getDisplayName())) {
            user.setDisplayName(displayNameCore.getDisplayName());
            user = userRepository.save(user);
        }
        return UserMapper.INSTANCE.toDto(user);
    }

    private void checkPrivileges(User user) {
        User loggedInUser = LoggedInUserHolder.getLoggedInUser();
        if (!loggedInUser.getId().equals(user.getId()) && (!loggedInUser.isAdministrator() || !user.isConfig())) {
            throw new AccessDeniedException("No privileges to change the user settings.");
        }
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

    public List<UserDto> getInformationContributors(String itemId) {
        return UserMapper.INSTANCE.toDto(userRepository.findInformationContributors(itemId));
    }
    public List<UserDto> getInformationContributors(String itemId, Long versionId) {
        return UserMapper.INSTANCE.toDto(userRepository.findInformationContributors(itemId,versionId));
    }


}
