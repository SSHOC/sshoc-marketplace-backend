package eu.sshopencloud.marketplace.filters.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.dto.auth.LoginData;
import org.apache.commons.io.IOUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UsernamePasswordBodyAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            try {
                String requestBody = IOUtils.toString(request.getReader());
                LoginData loginData = new ObjectMapper().readValue(requestBody, LoginData.class);
                String username = loginData.getUsername() != null ? loginData.getUsername().trim() : "";
                String password = loginData.getPassword() != null ? loginData.getPassword() : "";

                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
                setDetails(request, authRequest);

                return this.getAuthenticationManager().authenticate(authRequest);
            } catch (IOException e) {
                throw new InternalAuthenticationServiceException("Error while reading username and password from request body.", e);
            }
        }
    }

}
