package eu.sshopencloud.marketplace.conf.auth;

import eu.sshopencloud.marketplace.filters.auth.JwtGenerateAuthenticationSuccessHandler;
import eu.sshopencloud.marketplace.filters.auth.JwtHeaderAuthenticationFilter;
import eu.sshopencloud.marketplace.filters.auth.JwtProvider;
import eu.sshopencloud.marketplace.filters.auth.UsernamePasswordBodyAuthenticationFilter;
import eu.sshopencloud.marketplace.model.auth.UserRole;
import eu.sshopencloud.marketplace.services.auth.UserService;
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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
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

    @Value("${marketplace.cors.max-age-sec}")
    private Long corsMaxAgeInSec;

    private final UserService userService;

    private final JwtProvider jwtProvider;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .csrf().disable();
        http
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers(HttpMethod.PUT, "/api/item-reindex").hasAuthority(UserRole.ADMINISTRATOR.getAuthority());
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/**").permitAll()
                .antMatchers(HttpMethod.PUT, "/api/**").permitAll()
                .antMatchers(HttpMethod.DELETE, "/api/**").permitAll();

        http
                .addFilterBefore(jwtHeaderAuthenticationFilter(), UsernamePasswordBodyAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userService;
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
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new JwtGenerateAuthenticationSuccessHandler(jwtProvider);
    }

    @Bean
    public UsernamePasswordBodyAuthenticationFilter usernamePasswordAuthenticationFilter() throws Exception {
        UsernamePasswordBodyAuthenticationFilter authenticationFilter
                = new UsernamePasswordBodyAuthenticationFilter();
        authenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/auth/sign-in", "POST"));
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        authenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        //authenticationFilter.setAuthenticationFailureHandler(this::loginFailureHandler);
        return authenticationFilter;
    }

    @Bean
    public JwtHeaderAuthenticationFilter jwtHeaderAuthenticationFilter() throws Exception {
        return new JwtHeaderAuthenticationFilter(userDetailsService(), jwtProvider);
    }

}