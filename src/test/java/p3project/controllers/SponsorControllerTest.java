package p3project.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import jakarta.servlet.http.Cookie;
import p3project.classes.Token;
import p3project.classes.User;
import p3project.repositories.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SponsorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User createAdminUser() {
        User user = new User();
        user.setName("admin");
        user.setPassword("pass");
        user.setIsAdmin(true);
        return userRepository.save(user);
    }

    private User createNonAdminUser() {
        User user = new User();
        user.setName("user");
        user.setPassword("pass");
        user.setIsAdmin(false);
        return userRepository.save(user);
    }

    private Cookie buildValidTokenCookie(User user) {
        String idStr = String.valueOf(user.getId());
        String hash = Token.sign(idStr).getHash();
        String encoded = URLEncoder.encode(idStr + "." + hash, StandardCharsets.UTF_8);
        Cookie cookie = new Cookie("token", encoded);
        cookie.setPath("/");
        return cookie;
    }

    @Test
    void addSponsor_redirectsWhenAuthorized() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/add")
            .param("name", "Acme")
            .param("phoneNumber", "")
            .param("cvrNumber", "")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void deleteSponsor_requiresAuth_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/sponsors/delete").param("sponsorId", "1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/login")));
    }

    @Test
    void updateSponsor_redirectsThroughFunction() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/update/sponsor")
            .param("name", "New")
                .param("id", "99999")
            .param("phoneNumber", "")
            .param("cvrNumber", "")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addSponsor_withEmptyName_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/add")
            .param("name", "")
            .param("phoneNumber", "")
            .param("cvrNumber", "")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addSponsor_withValidData_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/add")
                .param("name", "Test Sponsor")
                .param("contactPerson", "John Doe")
                .param("email", "test@example.com")
                .param("phoneNumber", "12345678")
                .param("cvrNumber", "87654321").cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void deleteSponsor_withNonExistentId_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/delete").param("sponsorId", "99999").cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void updateSponsor_withMultipleFields_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/update/sponsor")
            .param("name", "Updated Name")
            .param("contactPerson", "Jane Doe")
            .param("email", "updated@example.com")
            .param("id", "99999")
            .param("phoneNumber", "")
            .param("cvrNumber", "")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addSponsor_withInvalidEmail_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/add")
            .param("name", "Test Sponsor")
            .param("email", "invalid-email")
            .param("phoneNumber", "")
            .param("cvrNumber", "")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addSponsor_withInvalidCVR_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/add")
            .param("name", "Test Sponsor")
            .param("cvrNumber", "123")
            .param("phoneNumber", "")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addSponsor_withInvalidPhoneNumber_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/add")
                .param("name", "Test Sponsor")
                .param("phoneNumber", "123")
                .param("cvrNumber", "")
                .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void updateSponsor_withEmptyName_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/update/sponsor")
            .param("name", "")
            .param("id", "1")
            .param("phoneNumber", "")
            .param("cvrNumber", "")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void deleteSponsor_withZeroId_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/delete").param("sponsorId", "0").cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addSponsor_withAllFieldsPopulated_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/add")
                .param("name", "Complete Sponsor")
                .param("contactPerson", "John Smith")
                .param("email", "john@example.com")
                .param("phoneNumber", "12345678")
                .param("cvrNumber", "12345678")
                .param("comments", "Test comments")
                .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void updateSponsor_withNullId_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/update/sponsor")
            .param("name", "Updated")
            .param("id", "99999")
            .param("phoneNumber", "")
            .param("cvrNumber", "")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addSponsor_withLongName_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/add")
            .param("name", "A".repeat(255))
            .param("phoneNumber", "")
            .param("cvrNumber", "")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void deleteSponsor_withNegativeId_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/delete").param("sponsorId", "-1").cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }
}
