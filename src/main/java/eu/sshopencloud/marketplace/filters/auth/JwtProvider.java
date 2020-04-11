package eu.sshopencloud.marketplace.filters.auth;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JwtProvider {

    static final String TOKEN_PREFIX = "Bearer ";

    @Value("${marketplace.auth.jwt-secret}")
    private String jwtSecret;

    @Value("${marketplace.auth.jwt-max-age-sec}")
    private Long jwtMaxAgeInSec;


    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtMaxAgeInSec * 1000);

        return TOKEN_PREFIX + Jwts.builder().setSubject(username).setIssuedAt(now).setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        String jwtToken = token.substring(TOKEN_PREFIX.length());
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwtToken).getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        String jwtToken = token.substring(TOKEN_PREFIX.length());
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwtToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature", e);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT", e);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty.", e);
        }
        return false;
    }

}
