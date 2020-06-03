package eu.sshopencloud.marketplace.services.auth;

import eu.sshopencloud.marketplace.conf.auth.ImplicitGrantTokenProvider;
import eu.sshopencloud.marketplace.conf.auth.JwtTokenProvider;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final ImplicitGrantTokenProvider implicitGrantTokenProvider;

    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;


    public String validateImplicitGrantToken(String token) throws InvalidTokenException {
        Long userId = implicitGrantTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new InvalidTokenException("Invalid token for user " + userId));
        if (user.getTokenKey() == null) {
            throw new InvalidTokenException("No active token for user " + userId);
        }
        if (implicitGrantTokenProvider.validateToken(token, user.getTokenKey())) {
            log.info("Valid implicit grant token found for username: '" + user.getUsername() + "'");
            return jwtTokenProvider.createToken(user.getUsername());
        } else {
            throw new InvalidTokenException("No active token for user " + userId);
        }
    }

}
