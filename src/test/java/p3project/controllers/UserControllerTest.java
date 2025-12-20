package p3project.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void addUser_delegatesToFunctions() throws Exception {
        mockMvc.perform(post("/api/user/add")
                .param("name", "alice")
                .param("password", "pw")
                .param("isAdmin", "true"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteUser_callsFunction() throws Exception {
        mockMvc.perform(post("/api/user/delete/5"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_withMissingPassword_redirects() throws Exception {
        mockMvc.perform(post("/api/user/add")
                .param("name", "testuser")
                .param("password", "")
                .param("isAdmin", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_withShortPassword_redirects() throws Exception {
        mockMvc.perform(post("/api/user/add")
                .param("name", "testuser")
                .param("password", "123")
                .param("isAdmin", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteUser_withNonExistentId_redirects() throws Exception {
        mockMvc.perform(post("/api/user/delete/99999"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_withNullName_returns400() throws Exception {
        mockMvc.perform(post("/api/user/add")
                .param("password", "validpass123")
                .param("isAdmin", "false"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_withEmptyName_redirects() throws Exception {
        mockMvc.perform(post("/api/user/add")
                .param("name", "")
                .param("password", "validpass123")
                .param("isAdmin", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_asAdmin_redirects() throws Exception {
        mockMvc.perform(post("/api/user/add")
                .param("name", "adminuser")
                .param("password", "adminpass123")
                .param("isAdmin", "true"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_withValidLongPassword_redirects() throws Exception {
        mockMvc.perform(post("/api/user/add")
                .param("name", "testuser")
                .param("password", "verylongpassword12345")
                .param("isAdmin", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteUser_withZeroId_redirects() throws Exception {
        mockMvc.perform(post("/api/user/delete/0"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteUser_withNegativeId_redirects() throws Exception {
        mockMvc.perform(post("/api/user/delete/-1"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_withSpecialCharactersInName_redirects() throws Exception {
        mockMvc.perform(post("/api/user/add")
                .param("name", "user@#$%")
                .param("password", "password123")
                .param("isAdmin", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_withWhitespaceOnlyPassword_redirects() throws Exception {
        mockMvc.perform(post("/api/user/add")
                .param("name", "testuser")
                .param("password", "   ")
                .param("isAdmin", "false"))
            .andExpect(status().is3xxRedirection());
    }
}
