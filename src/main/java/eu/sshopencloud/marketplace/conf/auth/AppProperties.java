package eu.sshopencloud.marketplace.conf.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "marketplace.security")
public class AppProperties {
    private final Auth auth = new Auth();
    private final OAuth2 oauth2 = new OAuth2();

    public static class Auth {
        private String tokenSecret;
        private long tokenExpirationMsec;

        public String getTokenSecret() {
            return tokenSecret;
        }

        public void setTokenSecret(String tokenSecret) {
            this.tokenSecret = tokenSecret;
        }

        public long getTokenExpirationMsec() {
            return tokenExpirationMsec;
        }

        public void setTokenExpirationMsec(long tokenExpirationMsec) {
            this.tokenExpirationMsec = tokenExpirationMsec;
        }
    }

    public static class OAuth2 {
        private String redirectAfterLogin;
        private String redirectAfterLogout;

        public String getRedirectAfterLogin() {
            return redirectAfterLogin;
        }

        public void setRedirectAfterLogin(String redirectAfterLogin) {
            this.redirectAfterLogin = redirectAfterLogin;
        }

        public String getRedirectAfterLogout() {
            return redirectAfterLogout;
        }

        public void setRedirectAfterLogout(String redirectAfterLogout) {
            this.redirectAfterLogout = redirectAfterLogout;
        }
    }

    public Auth getAuth() {
        return auth;
    }

    public OAuth2 getOauth2() {
        return oauth2;
    }
}