package eu.sshopencloud.marketplace.conf.auth;

import eu.sshopencloud.marketplace.filters.auth.JwtGenerateAuthenticationSuccessHandler;
import eu.sshopencloud.marketplace.filters.auth.JwtHeaderAuthenticationFilter;
import eu.sshopencloud.marketplace.filters.auth.JwtProvider;
import eu.sshopencloud.marketplace.filters.auth.UsernamePasswordBodyAuthenticationFilter;
import eu.sshopencloud.marketplace.model.auth.Authority;
import eu.sshopencloud.marketplace.services.auth.CustomUserDetailsService;
import eu.sshopencloud.marketplace.services.auth.LocalUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import eu.sshopencloud.marketplace.conf.auth.filter.TokenAuthenticationFilter;
import eu.sshopencloud.marketplace.conf.auth.handler.OidcAuthenticationSuccessHandler;
import eu.sshopencloud.marketplace.services.auth.CustomOidcUserService;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

    @Autowired
    private OidcAuthenticationSuccessHandler oidcAuthenticationSuccessHandler;

    @Autowired
    private CustomOidcUserService customOidcUserService;

    @Value("${marketplace.security.oauth2.redirectAfterLogout}")
    private String redirectAfterLogout;

    @Value("${marketplace.cors.max-age-sec}")
    private Long corsMaxAgeInSec;

//    private final LocalUserDetailsService localUserDetailsService;
    private final CustomUserDetailsService customUserDetailsService;

//    private final JwtProvider jwtProvider;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .csrf().disable().logout()
                .logoutSuccessUrl(redirectAfterLogout);
        http
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll();
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.PUT, "/api/item-reindex").hasAuthority(Authority.ADMINISTRATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/concept-reindex").hasAuthority(Authority.ADMINISTRATOR.name())
                .antMatchers("/api/users/**").hasAuthority(Authority.ADMINISTRATOR.name());
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
                .antMatchers(HttpMethod.POST, "/api/datasets/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.PUT, "/api/datasets/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/datasets/**").hasAuthority(Authority.MODERATOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/tools/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.PUT, "/api/tools/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/tools/**").hasAuthority(Authority.MODERATOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/training-materials/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.PUT, "/api/training-materials/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/training-materials/**").hasAuthority(Authority.MODERATOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/workflows/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.PUT, "/api/workflows/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/workflows/**").hasAuthority(Authority.MODERATOR.name());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/items-relations/**").hasAuthority(Authority.CONTRIBUTOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/items-relations/**").hasAuthority(Authority.CONTRIBUTOR.name());
        http
                .authorizeRequests()
                .antMatchers("/api/items/*/comments/**").authenticated();
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/loginFailure")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/login/oauth2/code/eosc/")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/oauth2/authorize/eosc")
                .permitAll()
                .anyRequest()
                .authenticated();
        http
                .oauth2Login()
                    .authorizationEndpoint()
                    .baseUri("/oauth2/authorize")
                .and()
                .userInfoEndpoint()
                    .oidcUserService(customOidcUserService)
                .and()
                .successHandler(oidcAuthenticationSuccessHandler)
                    .failureUrl("/loginFailure");

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
//        http
//                .addFilterBefore(jwtHeaderAuthenticationFilter(), UsernamePasswordBodyAuthenticationFilter.class);
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setMaxAge(corsMaxAgeInSec);
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return oidcAuthenticationSuccessHandler;
    }
    @Bean
    public UsernamePasswordBodyAuthenticationFilter usernamePasswordAuthenticationFilter() throws Exception {
        UsernamePasswordBodyAuthenticationFilter authenticationFilter
                = new UsernamePasswordBodyAuthenticationFilter();
        authenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/auth/sign-in", "POST"));
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        authenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        return authenticationFilter;
    }

//    @Bean
//    public JwtHeaderAuthenticationFilter jwtHeaderAuthenticationFilter() throws Exception {
//        return new JwtHeaderAuthenticationFilter(userDetailsService(), jwtProvider);
//    }

}