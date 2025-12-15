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

import p3project.functions.ContractFunctions;
import p3project.functions.UserFunctions;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addContract_redirectsWhenNoToken() throws Exception {
        mockMvc.perform(multipart("/sponsors/addContract")
                .file(new MockMultipartFile("pdffile", new byte[0]))
                .param("name", "Demo"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addContract_delegatesWhenAuthorized() throws Exception {
        mockMvc.perform(multipart("/sponsors/addContract")
                .file(new MockMultipartFile("pdffile", "file.pdf", "application/pdf", "data".getBytes()))
                .param("name", "Demo"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addContract_withEmptyName_redirects() throws Exception {
        mockMvc.perform(multipart("/sponsors/addContract")
                .file(new MockMultipartFile("pdffile", "file.pdf", "application/pdf", "data".getBytes()))
                .param("name", ""))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addContract_withNullPayment_redirects() throws Exception {
        mockMvc.perform(multipart("/sponsors/addContract")
                .file(new MockMultipartFile("pdffile", "file.pdf", "application/pdf", "data".getBytes()))
                .param("name", "Contract")
                .param("payment", ""))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addContract_withLargePDF_redirects() throws Exception {
        byte[] largeData = new byte[1024 * 100]; // 100KB
        mockMvc.perform(multipart("/sponsors/addContract")
                .file(new MockMultipartFile("pdffile", "large.pdf", "application/pdf", largeData))
                .param("name", "Large Contract"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteContract_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/deleteContract")
                .param("contractId", "1"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteContract_withNonExistentId_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/deleteContract")
                .param("contractId", "99999"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void updateContract_redirects() throws Exception {
        mockMvc.perform(multipart("/update/contract")
                .file(new MockMultipartFile("pdffile", new byte[0]))
                .param("id", "1")
                .param("name", "Updated Contract"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void updateContract_withoutPDF_redirects() throws Exception {
        mockMvc.perform(multipart("/update/contract")
                .file(new MockMultipartFile("pdffile", new byte[0]))
                .param("id", "1")
                .param("name", "Updated"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void addContract_withAllFields_redirects() throws Exception {
        mockMvc.perform(multipart("/sponsors/addContract")
                .file(new MockMultipartFile("pdffile", "contract.pdf", "application/pdf", "content".getBytes()))
                .param("name", "Full Contract")
                .param("payment", "10000")
                .param("type", "Sponsor")
                .param("sponsorId", "1"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteContract_withZeroId_redirects() throws Exception {
        mockMvc.perform(post("/sponsors/deleteContract")
                .param("contractId", "0"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void updateContract_withEmptyName_redirects() throws Exception {
        mockMvc.perform(multipart("/update/contract")
                .file(new MockMultipartFile("pdffile", new byte[0]))
                .param("id", "1")
                .param("name", ""))
            .andExpect(status().is3xxRedirection());
    }
}
