package eu.sshopencloud.marketplace.conf.auth;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private static final String TOKEN_PREFIX = "Bearer ";

    private SecurityProperties securityProperties;

    public JwtTokenProvider(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String createToken(Authentication authentication) {
        return createToken(authentication.getName());
    }

    public String createToken(String username) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + securityProperties.getAuth().getJwtTokenExpirationMsec());

        return TOKEN_PREFIX + Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, securityProperties.getAuth().getJwtTokenSecret())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        String jwtToken = token.substring(TOKEN_PREFIX.length());

        Claims claims = Jwts.parser()
                .setSigningKey(securityProperties.getAuth().getJwtTokenSecret())
                .parseClaimsJws(jwtToken)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        String jwtToken = authToken.substring(TOKEN_PREFIX.length());
        try {
            Jwts.parser().setSigningKey(securityProperties.getAuth().getJwtTokenSecret()).parseClaimsJws(jwtToken);
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