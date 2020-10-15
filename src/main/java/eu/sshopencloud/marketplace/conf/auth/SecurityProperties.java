package eu.sshopencloud.marketplace.conf.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "marketplace.security")
public class SecurityProperties {

    private final Auth auth = new Auth();
    private final OAuth oauth = new OAuth();

    public static class Auth {
        private String jwtTokenSecret;
        private long jwtTokenExpirationMsec;

        public String getJwtTokenSecret() {
            return StringUtils.isNotBlank(jwtTokenSecret) ? jwtTokenSecret : "default-token-secret";
        }

        public void setJwtTokenSecret(String jwtTokenSecret) {
            this.jwtTokenSecret = jwtTokenSecret;
        }

        public long getJwtTokenExpirationMsec() {
            return jwtTokenExpirationMsec;
        }

        public void setJwtTokenExpirationMsec(long jwtTokenExpirationMsec) {
            this.jwtTokenExpirationMsec = jwtTokenExpirationMsec;
        }
    }

    public static class OAuth {
        private long implicitGrantTokenExpirationMsec;

        public long getImplicitGrantTokenExpirationMsec() {
            return implicitGrantTokenExpirationMsec;
        }

        public void setImplicitGrantTokenExpirationMsec(long implicitGrantTokenExpirationMsec) {
            this.implicitGrantTokenExpirationMsec = implicitGrantTokenExpirationMsec;
        }
    }

    public Auth getAuth() {
        return auth;
    }


    public OAuth getOAuth() {
        return oauth;
    }

}