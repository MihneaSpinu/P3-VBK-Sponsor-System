package p3project.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import p3project.functions.UserFunctions;

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
        mockMvc.perform(post("/users/add")
                .param("name", "alice")
                .param("password", "pw")
                .param("isAdmin", "true"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteUser_callsFunction() throws Exception {
        mockMvc.perform(post("/users/delete/5"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void testUser_mappingRedirects() throws Exception {
        mockMvc.perform(get("/testuser"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_withMissingPassword_redirects() throws Exception {
        mockMvc.perform(post("/users/add")
                .param("name", "testuser")
                .param("password", "")
                .param("isAdmin", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_withShortPassword_redirects() throws Exception {
        mockMvc.perform(post("/users/add")
                .param("name", "testuser")
                .param("password", "123")
                .param("isAdmin", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteUser_withNonExistentId_redirects() throws Exception {
        mockMvc.perform(post("/users/delete/99999"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_withNullName_returns400() throws Exception {
        mockMvc.perform(post("/users/add")
                .param("password", "validpass123")
                .param("isAdmin", "false"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_withEmptyName_redirects() throws Exception {
        mockMvc.perform(post("/users/add")
                .param("name", "")
                .param("password", "validpass123")
                .param("isAdmin", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_asAdmin_redirects() throws Exception {
        mockMvc.perform(post("/users/add")
                .param("name", "adminuser")
                .param("password", "adminpass123")
                .param("isAdmin", "true"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_withValidLongPassword_redirects() throws Exception {
        mockMvc.perform(post("/users/add")
                .param("name", "testuser")
                .param("password", "verylongpassword12345")
                .param("isAdmin", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteUser_withZeroId_redirects() throws Exception {
        mockMvc.perform(post("/users/delete/0"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteUser_withNegativeId_redirects() throws Exception {
        mockMvc.perform(post("/users/delete/-1"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_withSpecialCharactersInName_redirects() throws Exception {
        mockMvc.perform(post("/users/add")
                .param("name", "user@#$%")
                .param("password", "password123")
                .param("isAdmin", "false"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addUser_withWhitespaceOnlyPassword_redirects() throws Exception {
        mockMvc.perform(post("/users/add")
                .param("name", "testuser")
                .param("password", "   ")
                .param("isAdmin", "false"))
            .andExpect(status().is3xxRedirection());
    }
}
