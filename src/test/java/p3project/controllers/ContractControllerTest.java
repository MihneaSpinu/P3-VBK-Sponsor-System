package p3project.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.hamcrest.Matchers;

import p3project.functions.ContractFunctions;
import p3project.functions.UserFunctions;
import p3project.repositories.UserRepository;
import p3project.classes.Token;
import p3project.classes.User;

import jakarta.servlet.http.Cookie;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ContractControllerTest {

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
    void addContract_redirectsWhenNoToken() throws Exception {
        mockMvc.perform(multipart("/sponsors/addContract")
                .file(new MockMultipartFile("pdffile", new byte[0]))
                .param("name", "Demo"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void addContract_delegatesWhenAuthorized() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        MockHttpServletRequestBuilder req = multipart("/sponsors/addContract")
                .file(new MockMultipartFile("pdffile", "file.pdf", "application/pdf", "data".getBytes()))
                .param("name", "Demo")
                .param("payment", "100")
                .param("type", "Sponsor")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(1).toString())
                .cookie(token);
        mockMvc.perform(req)
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addContract_withEmptyName_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(multipart("/sponsors/addContract")
                .file(new MockMultipartFile("pdffile", "file.pdf", "application/pdf", "data".getBytes()))
                .param("name", "")
                .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addContract_withNullPayment_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(multipart("/sponsors/addContract")
                .file(new MockMultipartFile("pdffile", "file.pdf", "application/pdf", "data".getBytes()))
                .param("name", "Contract")
                .param("payment", "")
                .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addContract_withLargePDF_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        byte[] largeData = new byte[1024 * 100]; // 100KB
        mockMvc.perform(multipart("/sponsors/addContract")
                .file(new MockMultipartFile("pdffile", "large.pdf", "application/pdf", largeData))
                .param("name", "Large Contract")
                .param("payment", "100")
                .param("type", "Sponsor")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(1).toString())
                .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void deleteContract_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/deleteContract")
                .param("contractId", "1")
                .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void deleteContract_withNonExistentId_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/deleteContract")
                .param("contractId", "99999")
                .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void updateContract_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(multipart("/update/contract")
                .file(new MockMultipartFile("pdffile", new byte[0]))
                .param("id", "1")
                .param("name", "Updated Contract")
                .param("payment", "100")
                .param("type", "Sponsor")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(1).toString())
                .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void updateContract_withoutPDF_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(multipart("/update/contract")
                .file(new MockMultipartFile("pdffile", new byte[0]))
                .param("id", "1")
                .param("name", "Updated")
                .param("payment", "100")
                .param("type", "Sponsor")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(1).toString())
                .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void addContract_withAllFields_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(multipart("/sponsors/addContract")
                .file(new MockMultipartFile("pdffile", "contract.pdf", "application/pdf", "content".getBytes()))
                .param("name", "Full Contract")
                .param("payment", "10000")
                .param("type", "Sponsor")
                .param("sponsorId", "1")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(1).toString())
                .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.startsWith("/sponsors")));
    }

    @Test
    void deleteContract_withZeroId_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(post("/sponsors/deleteContract")
                .param("contractId", "0")
                .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/sponsors"));
    }

    @Test
    void updateContract_withEmptyName_redirects() throws Exception {
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        mockMvc.perform(multipart("/update/contract")
                .file(new MockMultipartFile("pdffile", new byte[0]))
                .param("id", "1")
                .param("name", "")
                .param("payment", "100")
                .param("type", "Sponsor")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(1).toString())
                .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/sponsors"));
    }

    @Test
    void deleteContract_redirectsToLoginWhenNoToken() throws Exception {
        mockMvc.perform(post("/sponsors/deleteContract").param("contractId", "5"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void deleteContract_redirectsToHomepageWhenNotAdmin() throws Exception {
        User nonAdmin = createNonAdminUser();
        Cookie token = buildValidTokenCookie(nonAdmin);
        mockMvc.perform(post("/sponsors/deleteContract").param("contractId", "5").cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/homepage"));
    }

    @Test
    void updateContract_redirectsToLoginWhenNoToken() throws Exception {
        mockMvc.perform(multipart("/update/contract")
                .file(new MockMultipartFile("pdffile", new byte[0]))
                .param("id", "1")
                .param("name", "Updated"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void updateContract_redirectsToHomepageWhenNotAdmin() throws Exception {
        User nonAdmin = createNonAdminUser();
        Cookie token = buildValidTokenCookie(nonAdmin);
        mockMvc.perform(multipart("/update/contract")
                .file(new MockMultipartFile("pdffile", new byte[0]))
                .param("id", "1")
            .param("name", "Updated")
            .cookie(token))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/homepage"));
    }

    @Test
    void getFile_returnsOkWhenFunctionsReturnBody() throws Exception {
        // Create a contract in DB to download
        User admin = createAdminUser();
        Cookie token = buildValidTokenCookie(admin);
        byte[] data = "content".getBytes();
        mockMvc.perform(multipart("/sponsors/addContract")
                .file(new MockMultipartFile("pdffile", "contract.pdf", "application/pdf", data))
                .param("name", "Full Contract")
                .param("payment", "10000")
                .param("type", "Sponsor")
                .param("sponsorId", "1")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(1).toString())
                .cookie(token))
            .andExpect(status().is3xxRedirection());

        // Assuming last inserted contract has id 1 in H2 for this test context
        mockMvc.perform(get("/getFile/1"))
            .andExpect(status().isOk())
            .andExpect(content().bytes(data));
    }
}
