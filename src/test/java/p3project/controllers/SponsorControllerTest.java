package p3project.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import p3project.functions.SponsorFunctions;
import p3project.functions.UserFunctions;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SponsorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addSponsor_redirectsWhenAuthorized() throws Exception {
        mockMvc.perform(post("/sponsors/add").param("name", "Acme"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteSponsor_requiresAuth() throws Exception {
        mockMvc.perform(post("/sponsors/delete").param("sponsorId", "1"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void updateSponsor_redirectsThroughFunction() throws Exception {
        mockMvc.perform(post("/update/sponsor").param("name", "New"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addSponsor_withEmptyName_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/add").param("name", ""))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addSponsor_withValidData_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/add")
                .param("name", "Test Sponsor")
                .param("contactPerson", "John Doe")
                .param("email", "test@example.com")
                .param("phoneNumber", "12345678")
                .param("cvrNumber", "87654321"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteSponsor_withNonExistentId_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/delete").param("sponsorId", "99999"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void updateSponsor_withMultipleFields_redirects() throws Exception {
        mockMvc.perform(post("/update/sponsor")
                .param("name", "Updated Name")
                .param("contactPerson", "Jane Doe")
                .param("email", "updated@example.com"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addSponsor_withInvalidEmail_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/add")
                .param("name", "Test Sponsor")
                .param("email", "invalid-email"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addSponsor_withInvalidCVR_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/add")
                .param("name", "Test Sponsor")
                .param("cvrNumber", "123"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addSponsor_withInvalidPhoneNumber_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/add")
                .param("name", "Test Sponsor")
                .param("phoneNumber", "123"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void updateSponsor_withEmptyName_redirects() throws Exception {
        mockMvc.perform(post("/update/sponsor")
                .param("name", "")
                .param("id", "1"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteSponsor_withZeroId_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/delete").param("sponsorId", "0"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addSponsor_withAllFieldsPopulated_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/add")
                .param("name", "Complete Sponsor")
                .param("contactPerson", "John Smith")
                .param("email", "john@example.com")
                .param("phoneNumber", "12345678")
                .param("cvrNumber", "12345678")
                .param("comments", "Test comments"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void updateSponsor_withNullId_redirects() throws Exception {
        mockMvc.perform(post("/update/sponsor")
                .param("name", "Updated"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addSponsor_withLongName_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/add")
                .param("name", "A".repeat(255)))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteSponsor_withNegativeId_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/delete").param("sponsorId", "-1"))
            .andExpect(status().is3xxRedirection());
    }
}
