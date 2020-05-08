package eu.sshopencloud.marketplace.conf.auth;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.dto.auth.LoginData;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class LogInTestClient {

    public static String getJwt(MockMvc mvc, String username, String password)
            throws Exception {
        LoginData loginData = new LoginData(username, password);

        return mvc.perform(post("/api/auth/sign-in")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(loginData))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getHeader("Authorization");
    }

}
