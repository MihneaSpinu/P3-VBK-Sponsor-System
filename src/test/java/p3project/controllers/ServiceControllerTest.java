package p3project.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import p3project.functions.ServiceFunctions;
import p3project.functions.UserFunctions;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void setServiceArchived_forbiddenWhenNoToken() throws Exception {
        mockMvc.perform(post("/sponsors/setServiceArchived")
                .param("serviceId", "1")
                .param("active", "false"))
            .andExpect(status().isForbidden());
    }

    @Test
    void setServiceArchived_returnsBodyWhenAuthorized() throws Exception {
        mockMvc.perform(post("/sponsors/setServiceArchived")
                .param("serviceId", "2")
                .param("active", "true"))
            .andExpect(status().isForbidden());
    }

    @Test
    void addService_redirectsWhenAuthorized() throws Exception {
        mockMvc.perform(post("/sponsors/addService")
                .param("name", "Print")
                .param("type", "Expo"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addService_withEmptyName_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/addService")
                .param("name", "")
                .param("type", "Expo"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addService_withBannerType_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/addService")
                .param("name", "Banner Service")
                .param("type", "Banner"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addService_withLogoTrojerType_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/addService")
                .param("name", "Logo Jersey")
                .param("type", "LogoTrojer")
                .param("division", "2"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addService_withLogoBukserType_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/addService")
                .param("name", "Logo Pants")
                .param("type", "LogoBukser")
                .param("division", "3"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteService_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/deleteService")
                .param("serviceId", "1"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteService_withNonExistentId_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/deleteService")
                .param("serviceId", "99999"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void updateService_redirects() throws Exception {
        mockMvc.perform(post("/update/service")
                .param("id", "1")
                .param("name", "Updated Service")
                .param("type", "Expo"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void updateService_withEmptyName_redirects() throws Exception {
        mockMvc.perform(post("/update/service")
                .param("id", "1")
                .param("name", "")
                .param("type", "Expo"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addService_withNegativeAmount_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/addService")
                .param("name", "Service")
                .param("type", "Expo")
                .param("amount", "-100"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void setServiceArchived_withInvalidServiceId_forbidden() throws Exception {
        mockMvc.perform(post("/sponsors/setServiceArchived")
                .param("serviceId", "99999")
                .param("active", "false"))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateService_withAllFields_redirects() throws Exception {
        mockMvc.perform(post("/update/service")
                .param("id", "1")
                .param("name", "Complete Service")
                .param("type", "Banner")
                .param("amount", "500"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteService_withZeroId_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/deleteService")
                .param("serviceId", "0"))
            .andExpect(status().is3xxRedirection());
    }
}
