package p3project.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import p3project.functions.UserFunctions;
import p3project.functions.ViewFunctions;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminPanel_requiresAdmin() throws Exception {
        mockMvc.perform(get("/AdminPanel"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void sponsors_redirectsWhenNoToken() throws Exception {
        mockMvc.perform(get("/sponsors"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void homepage_returnsViewWhenAuthorized() throws Exception {
        mockMvc.perform(get("/homepage"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void archive_redirectsWhenNoToken() throws Exception {
        mockMvc.perform(get("/archive"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void changelog_redirectsWhenNoToken() throws Exception {
        mockMvc.perform(get("/changelog"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void root_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void adminPanel_withInvalidToken_redirects() throws Exception {
        mockMvc.perform(get("/AdminPanel"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void sponsors_withoutAuthentication_redirects() throws Exception {
        mockMvc.perform(get("/sponsors"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void homepage_withoutAuthentication_redirects() throws Exception {
        mockMvc.perform(get("/homepage"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void archive_withoutAuthentication_redirects() throws Exception {
        mockMvc.perform(get("/archive"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void changelog_withoutAuthentication_redirects() throws Exception {
        mockMvc.perform(get("/changelog"))
            .andExpect(status().is3xxRedirection());
    }
}
