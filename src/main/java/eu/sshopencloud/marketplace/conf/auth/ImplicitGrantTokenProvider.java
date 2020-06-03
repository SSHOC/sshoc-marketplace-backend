package eu.sshopencloud.marketplace.conf.auth;

import eu.sshopencloud.marketplace.services.auth.InvalidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class ImplicitGrantTokenProvider {

    private SecurityProperties securityProperties;

    public ImplicitGrantTokenProvider(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String createToken(UserPrincipal userPrincipal) {
        //base64(userId + ":" + expirationTime + ":" + md5Hex(userId + ":" + expirationTime + ":" + key))

        Long expirationTime = new Date().getTime() + securityProperties.getOAuth().getImplicitGrantTokenExpirationMsec();
        StringBuilder builder = new StringBuilder();
        String data = builder
                .append(userPrincipal.getId())
                .append(":")
                .append(expirationTime)
                .append(":")
                .append(generateSignature(userPrincipal.getId(), expirationTime.toString(), userPrincipal.getTokenKey()))
                .toString();
        return new String(Base64.getEncoder().encode(data.getBytes()));
    }

    private String generateSignature(Long userId, String expirationTime, String tokenKey) {
        String data = userId + ":" +  expirationTime + ":" + tokenKey;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }
        return new String(Hex.encode(digest.digest(data.getBytes(StandardCharsets.UTF_8))));
    }

    private String[] decodeAndSplit(String token) {
        String decodedToken = new String(Base64.getDecoder().decode(token));
        return decodedToken.split(":");
    }

    public Long getUserIdFromToken(String token) throws InvalidTokenException {
        try {
            return Long.parseLong(decodeAndSplit(token)[0]);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid token!", e);
        }
    }

    public boolean validateToken(String token, String tokenKey) {
        String[] parts = decodeAndSplit(token);
        if (parts.length == 3) {
            try {
                if (new Date().getTime() < Long.parseLong(parts[1])) {
                    if (parts[2].equals(generateSignature(Long.parseLong(parts[0]), parts[1], tokenKey))) {
                        return true;
                    } else {
                        log.error("Invalid implicit grant token signature");
                    }
                } else {
                    log.error("Expired implicit grant token");
                }
            } catch (NumberFormatException e) {
                log.error("Invalid implicit grant token");
            }
        } else {
            log.error("Unsupported implicit grant token");
        }
        return false;
    }

}
