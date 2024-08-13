package eu.sshopencloud.marketplace.conf.auth;

import eu.sshopencloud.marketplace.filters.auth.JwtTokenAuthenticationFilter;
import eu.sshopencloud.marketplace.filters.auth.OidcAuthenticationFailureHandler;
import eu.sshopencloud.marketplace.filters.auth.OidcAuthenticationSuccessHandler;
import eu.sshopencloud.marketplace.filters.auth.UsernamePasswordBodyAuthenticationFilter;
import eu.sshopencloud.marketplace.model.auth.Authority;
import eu.sshopencloud.marketplace.repositories.auth.HttpCookieOAuth2AuthorizationRequestRepository;
import eu.sshopencloud.marketplace.services.auth.CustomOidcUserService;
import eu.sshopencloud.marketplace.services.auth.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final OidcAuthenticationSuccessHandler oidcAuthenticationSuccessHandler;

    private final OidcAuthenticationFailureHandler oidcAuthenticationFailureHandler;

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final CustomOidcUserService customOidcUserService;

    private final CustomUserDetailsService customUserDetailsService;

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${marketplace.cors.max-age-sec}")
    private Long corsMaxAgeInSec;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(new Http403ForbiddenEntryPoint());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/oauth/sign-up").authenticated();
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/draft-items").authenticated();
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.PUT, "/api/item-reindex").hasAuthority(Authority.ADMINISTRATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/item-autocomplete-rebuild").hasAuthority(Authority.ADMINISTRATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/concept-reindex").hasAuthority(Authority.ADMINISTRATOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/users/*/password").authenticated()
                .antMatchers(HttpMethod.GET, "/api/users/*/display-name").authenticated()
                .antMatchers(HttpMethod.GET, "/api/users").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.GET, "/api/users/*").hasAuthority(Authority.MODERATOR.name())
                .antMatchers("/api/users/**").hasAuthority(Authority.ADMINISTRATOR.name());

        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/property-types/**").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/property-types/**").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/property-types/**").hasAuthority(Authority.MODERATOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/vocabularies/*/concepts/*/merge").hasAnyAuthority(Authority.SYSTEM_CONTRIBUTOR.name(),Authority.MODERATOR.name())
                .antMatchers(HttpMethod.POST, "/api/vocabularies/*/concepts").hasAnyAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.POST, "/api/vocabularies/**").hasAnyAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/vocabularies/**").hasAnyAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/vocabularies/**").hasAuthority(Authority.MODERATOR.name());

        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/actors/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.PUT, "/api/actors/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/actors/**").hasAuthority(Authority.MODERATOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/sources/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.PUT, "/api/sources/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/sources/**").hasAuthority(Authority.MODERATOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/datasets/*/merge").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.POST, "/api/datasets/merge").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/datasets/*/versions/*/revert").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/datasets/*/revert").hasAuthority(Authority.ADMINISTRATOR.name())
                .antMatchers(HttpMethod.POST, "/api/datasets/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.PUT, "/api/datasets/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/datasets/**").hasAuthority(Authority.CONTRIBUTOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/tools-services/*/merge").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.POST, "/api/tools-services/merge").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/tools-services/*/versions/*/revert").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/tools-services/*/revert").hasAuthority(Authority.ADMINISTRATOR.name())
                .antMatchers(HttpMethod.POST, "/api/tools-services/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.PUT, "/api/tools-services/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/tools-services/**").hasAuthority(Authority.CONTRIBUTOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/training-materials/*/merge").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.POST, "/api/training-materials/merge").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/training-materials/*/versions/*/revert").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/training-materials/*/revert").hasAuthority(Authority.ADMINISTRATOR.name())
                .antMatchers(HttpMethod.POST, "/api/training-materials/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.PUT, "/api/training-materials/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/training-materials/**").hasAuthority(Authority.CONTRIBUTOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/publications/*/merge").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.POST, "/api/publications/merge").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/publications/*/versions/*/revert").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/publications/*/revert").hasAuthority(Authority.ADMINISTRATOR.name())
                .antMatchers(HttpMethod.POST, "/api/publications/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.PUT, "/api/publications/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/publications/**").hasAuthority(Authority.MODERATOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/workflows/*/merge").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.POST, "/api/workflows/merge").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/workflows/*/versions/*/revert").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/workflows/*/revert").hasAuthority(Authority.ADMINISTRATOR.name())
                .antMatchers(HttpMethod.GET, "/api/workflows/*/steps/*/merge").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.POST, "/api/workflows/*/steps/merge").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/workflows/*/steps/*/versions/*/revert").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.POST, "/api/workflows/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.PUT, "/api/workflows/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/workflows/**").hasAuthority(Authority.CONTRIBUTOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/items-relations/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/items-relations/**").hasAuthority(Authority.CONTRIBUTOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/actor-roles/**").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/actor-roles/**").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/actor-roles/**").hasAuthority(Authority.MODERATOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/actor-sources/**").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/actor-sources/**").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/actor-sources/**").hasAuthority(Authority.MODERATOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/item-sources/**").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/item-sources/**").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/item-sources/**").hasAuthority(Authority.MODERATOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/media-sources/**").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/media-sources/**").hasAuthority(Authority.MODERATOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/media-sources/**").hasAuthority(Authority.MODERATOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/media/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/media/**").hasAuthority(Authority.CONTRIBUTOR.name());
        http
                .authorizeRequests()
                .antMatchers("/api/items/*/comments/**").authenticated();
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/login/oauth2/code/eosc/")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/oauth2/authorize/eosc")
                .permitAll();

        http
                .oauth2Login().authorizationEndpoint()
                .authorizationRequestResolver(defaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository))
                .baseUri("/oauth2/authorize").authorizationRequestRepository(authorizationRequestRepository()).and()
                .tokenEndpoint()
                .and()
                .userInfoEndpoint().oidcUserService(customOidcUserService)
                .and()
                .successHandler(oidcAuthenticationSuccessHandler)
                .failureHandler(oidcAuthenticationFailureHandler);

        http.addFilterBefore(jwtTokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration apiConfiguration = new CorsConfiguration();
        apiConfiguration.setAllowedOrigins(Arrays.asList("*"));
        apiConfiguration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE"));
        apiConfiguration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        apiConfiguration.setExposedHeaders(Arrays.asList("Authorization"));
        apiConfiguration.setMaxAge(corsMaxAgeInSec);
        apiConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", apiConfiguration);
        return source;
    }

    @Bean
    public JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter() {
        return new JwtTokenAuthenticationFilter(jwtTokenProvider, customUserDetailsService);
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return oidcAuthenticationSuccessHandler;
    }

    @Bean
    public UsernamePasswordBodyAuthenticationFilter usernamePasswordAuthenticationFilter() throws Exception {
        UsernamePasswordBodyAuthenticationFilter authenticationFilter
                = new UsernamePasswordBodyAuthenticationFilter();
        authenticationFilter.setPostOnly(true);
        authenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/auth/sign-in", "POST"));
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        authenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        return authenticationFilter;
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowSemicolon(true);
        firewall.setAllowUrlEncodedDoubleSlash(true);
        firewall.setAllowUrlEncodedPercent(true);
        return firewall;
    }

    @Bean
    public OAuth2AuthorizationRequestResolver defaultOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        OAuth2AuthorizationRequestResolver authorizationRequestResolver = new PkceAuthorizationRequestResolver(
                clientRegistrationRepository);
        return authorizationRequestResolver;
    }
}
