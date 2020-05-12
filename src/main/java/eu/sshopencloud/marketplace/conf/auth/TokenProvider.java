package eu.sshopencloud.marketplace.conf.auth;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TokenProvider {
    private static final String TOKEN_PREFIX = "Bearer ";
    private AppProperties appProperties;
//    private UserRepository userRepository;

//    public TokenProvider(AppProperties appProperties, UserRepository userRepository) {
    public TokenProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
//        this.userRepository = userRepository;
    }

    public String createToken(Authentication authentication) {
//        DefaultOidcUser defaultOidcUser = (DefaultOidcUser) authentication.getPrincipal();
//        Map<String, Object> attributes = defaultOidcUser.getAttributes();
//        String email = (String) attributes.get("email");
//        log.info(defaultOidcUser.toString());
//
//        Optional<User> userOpt = userRepository.findByEmail(email);
//        if(userOpt.isPresent()) {
//            User user = userOpt.get();
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());

            return TOKEN_PREFIX + Jwts.builder()
                    .setSubject(authentication.getName())
                    .setIssuedAt(new Date())
                    .setExpiration(expiryDate)
                    .signWith(SignatureAlgorithm.HS512, appProperties.getAuth().getTokenSecret())
                    .compact();
//        }
//        throw new UsernameNotFoundException("User not found with username : " + email);
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(appProperties.getAuth().getTokenSecret())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        authToken = authToken.substring(TOKEN_PREFIX.length());
        try {
            Jwts.parser().setSigningKey(appProperties.getAuth().getTokenSecret()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

}