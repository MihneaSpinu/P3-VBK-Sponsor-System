package p3project.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LoginControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void confirmLogin_redirectsPerFunction() throws Exception {
        mockMvc.perform(post("/login/confirm")
                .param("username", "alice")
                .param("password", "pw")
                .param("rememberMe", "true"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void logout_redirectsPerFunction() throws Exception {
        mockMvc.perform(get("/logout"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void confirmLogin_withEmptyUsername_redirects() throws Exception {
        mockMvc.perform(post("/login/confirm")
                .param("username", "")
                .param("password", "password")
                .param("rememberMe", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void confirmLogin_withEmptyPassword_redirects() throws Exception {
        mockMvc.perform(post("/login/confirm")
                .param("username", "testuser")
                .param("password", "")
                .param("rememberMe", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void confirmLogin_withoutRememberMe_returns400() throws Exception {
        mockMvc.perform(post("/login/confirm")
                .param("username", "testuser")
                .param("password", "password"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void confirmLogin_withRememberMeFalse_redirects() throws Exception {
        mockMvc.perform(post("/login/confirm")
                .param("username", "testuser")
                .param("password", "password")
                .param("rememberMe", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void confirmLogin_withSpecialCharacters_redirects() throws Exception {
        mockMvc.perform(post("/login/confirm")
                .param("username", "user@#$")
                .param("password", "pass@#$")
                .param("rememberMe", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void confirmLogin_withLongUsername_redirects() throws Exception {
        mockMvc.perform(post("/login/confirm")
                .param("username", "verylongusernametestingboundaries")
                .param("password", "password")
                .param("rememberMe", "false"))
            .andExpect(status().is3xxRedirection());
    }
}
