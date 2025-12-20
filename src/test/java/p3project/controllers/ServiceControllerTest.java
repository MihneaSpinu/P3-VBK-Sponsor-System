package p3project.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.hamcrest.Matchers;

import p3project.repositories.UserRepository;
import p3project.classes.Token;
import p3project.classes.User;

import jakarta.servlet.http.Cookie;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private Cookie buildValidTokenCookie(User user) {
        String idStr = String.valueOf(user.getId());
        String hash = Token.sign(idStr).getHash();
        String value = URLEncoder.encode(idStr + "." + hash, StandardCharsets.UTF_8);
        return new Cookie("token", value);
    }

    private User createAdminUser() {
        User u = new User();
        u.setName("admin");
        u.setPassword("pass");
        u.setIsAdmin(true);
        return userRepository.save(u);
    }

    private User createNonAdminUser() {
        User u = new User();
        u.setName("user");
        u.setPassword("pass");
        u.setIsAdmin(false);
        return userRepository.save(u);
    }

    @Test
    void setServiceArchived_forbiddenWhenNoToken() throws Exception {
        mockMvc.perform(post("/sponsors/setServiceArchived")
                .param("serviceId", "1")
                .param("active", "false"))
            .andExpect(status().isForbidden());
    }

    @Test
    void setServiceArchived_returnsBodyWhenAuthorized() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/setServiceArchived")
            .param("serviceId", "2")
            .param("active", "true")
            .cookie(token))
            .andExpect(status().isOk());
    }

    @Test
    void addService_redirectsWhenAuthorized() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/addService")
                .param("name", "Print")
            .param("type", "Expo")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addService_withEmptyName_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/addService")
                .param("name", "")
            .param("type", "Expo")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addService_withBannerType_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/addService")
                .param("name", "Banner Service")
            .param("type", "Banner")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addService_withLogoTrojerType_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/addService")
                .param("name", "Logo Jersey")
                .param("type", "LogoTrojer")
            .param("division", "2")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addService_withLogoBukserType_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/addService")
                .param("name", "Logo Pants")
                .param("type", "LogoBukser")
            .param("division", "3")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void deleteService_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/deleteService")
            .param("serviceId", "1")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void deleteService_withNonExistentId_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/deleteService")
            .param("serviceId", "99999")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void updateService_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/update/service")
            .param("id", "1")
            .param("name", "Updated Service")
            .param("type", "Expo")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void updateService_withEmptyName_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/update/service")
            .param("id", "1")
            .param("name", "")
            .param("type", "Expo")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addService_withNegativeAmount_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/addService")
            .param("name", "Service")
            .param("type", "Expo")
            .param("amount", "-100")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void setServiceArchived_withInvalidServiceId_forbidden() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/setServiceArchived")
            .param("serviceId", "99999")
            .param("active", "false")
            .cookie(token))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateService_withAllFields_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/update/service")
            .param("id", "1")
            .param("name", "Complete Service")
            .param("type", "Banner")
            .param("amount", "500")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void deleteService_withZeroId_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/deleteService")
            .param("serviceId", "0")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }
}
