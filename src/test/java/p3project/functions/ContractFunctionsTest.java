package p3project.functions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Contract;
import p3project.classes.Service;
import p3project.classes.User;
import p3project.repositories.ContractRepository;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;

@ExtendWith(MockitoExtension.class)
class ContractFunctionsTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private LogRepository logRepository;

    @Mock
    private ServiceFunctions serviceFunctions;

    @Mock
    private UserFunctions userFunctions;

    @Mock
    private EventlogFunctions eventlogFunctions;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ContractFunctions contractFunctions;

    @Test
    void contractIsValid_acceptsWellFormedContract() {
        Contract contract = new Contract();
        contract.setName("Deal A");
        contract.setPayment("5000");
        contract.setStartDate(LocalDate.of(2024, 1, 1));
        contract.setEndDate(LocalDate.of(2024, 12, 31));

        assertThat(contractFunctions.contractIsValid(contract)).isTrue();
    }

    @Test
    void contractIsValid_rejectsInvalidInputs() {
        Contract missingName = new Contract();
        missingName.setPayment("100");
        missingName.setStartDate(LocalDate.now());
        missingName.setEndDate(LocalDate.now());

        Contract negativePayment = new Contract();
        negativePayment.setName("X");
        negativePayment.setPayment("-1");
        negativePayment.setStartDate(LocalDate.now());
        negativePayment.setEndDate(LocalDate.now());

        assertThat(contractFunctions.contractIsValid(missingName)).isFalse();
        assertThat(contractFunctions.contractIsValid(negativePayment)).isFalse();
    }

    @Test
    void parseContract_setsPdfBytesAndFilename() {
        Contract contract = new Contract();
        MultipartFile pdf = new MockMultipartFile("pdffile", "contract.pdf", "application/pdf", new byte[]{1,2,3});

        contractFunctions.parseContract(contract, pdf);

        assertArrayEquals(new byte[]{1,2,3}, contract.getPdfData());
        assertEquals("contract.pdf", contract.getFileName());
    }

    @Test
    void parseContract_throwsRuntimeOnIOException() throws Exception {
        Contract contract = new Contract();
        MultipartFile brokenFile = org.mockito.Mockito.mock(MultipartFile.class);
        when(brokenFile.isEmpty()).thenReturn(false);
        when(brokenFile.getBytes()).thenThrow(new java.io.IOException("boom"));

        assertThrows(RuntimeException.class, () -> contractFunctions.parseContract(contract, brokenFile));
    }

    @Test
    void contractIsActive_setsActiveWhenAnyServiceActive() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setEndDate(LocalDate.now().plusDays(1));
        Service service = new Service(1L, "Name", "Banner", true, 0, LocalDate.now(), LocalDate.now().plusDays(1));

        when(serviceRepository.findAll()).thenReturn(List.of(service));
        when(serviceFunctions.serviceIsActive(service)).thenReturn(true);
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean active = contractFunctions.contractIsActive(contract);

        assertThat(active).isTrue();
        assertThat(contract.getActive()).isTrue();
        verify(contractRepository).save(contract);
    }

    @Test
    void contractIsActive_setsInactiveWhenNoActiveServices() {
        Contract contract = new Contract();
        contract.setId(2L);
        contract.setEndDate(LocalDate.now().plusDays(1));
        Service service = new Service(2L, "Name", "Banner", true, 0, LocalDate.now(), LocalDate.now().plusDays(1));

        when(serviceRepository.findAll()).thenReturn(List.of(service));
        when(serviceFunctions.serviceIsActive(service)).thenReturn(false);

        boolean active = contractFunctions.contractIsActive(contract);

        assertThat(active).isFalse();
        assertThat(contract.getActive()).isFalse();
        verify(contractRepository).save(contract);
    }

    @Test
    void addContractForSponsor_savesWhenValid() {
        Contract contract = new Contract();
        contract.setName("Valid");
        contract.setPayment("100");
        contract.setStartDate(LocalDate.now());
        contract.setEndDate(LocalDate.now().plusDays(1));
        MultipartFile pdf = new MockMultipartFile("pdffile", "c.pdf", "application/pdf", "data".getBytes());
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(userFunctions.getUserFromToken(request)).thenReturn(new User());
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        String result = contractFunctions.addContractForSponsor(contract, pdf, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        verify(logRepository).save(any());
        verify(contractRepository).save(contract);
    }

    @Test
    void addContractForSponsor_rejectsInvalidContract() {
        Contract contract = new Contract();
        MultipartFile pdf = new MockMultipartFile("pdffile", "c.pdf", "application/pdf", "data".getBytes());
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        String result = contractFunctions.addContractForSponsor(contract, pdf, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        verify(contractRepository, never()).save(any());
    }

    @Test
    void getFile_returnsPdfResponse() {
        Contract contract = new Contract();
        contract.setId(3L);
        contract.setFileName("file.pdf");
        contract.setPdfData(new byte[]{5,6});
        when(contractRepository.findById(3L)).thenReturn(java.util.Optional.of(contract));

        ResponseEntity<byte[]> response = contractFunctions.getFile(3L);

        assertThat(response.getHeaders().getFirst("Content-Disposition")).contains("file.pdf");
        assertArrayEquals(new byte[]{5,6}, response.getBody());
    }

    @Test
    void getFile_throwsWhenMissing() {
        when(contractRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> contractFunctions.getFile(99L));
    }

    @Test
    void contractIsValid_acceptsZeroPayment() {
        Contract contract = new Contract();
        contract.setName("Free Deal");
        contract.setPayment("0");
        contract.setStartDate(LocalDate.of(2024, 1, 1));
        contract.setEndDate(LocalDate.of(2024, 12, 31));

        assertThat(contractFunctions.contractIsValid(contract)).isTrue();
    }

    @Test
    void contractIsValid_rejectsNullDates() {
        Contract contract = new Contract();
        contract.setName("Deal");
        contract.setPayment("100");
        contract.setStartDate(null);
        contract.setEndDate(LocalDate.of(2024, 12, 31));

        assertThat(contractFunctions.contractIsValid(contract)).isFalse();
    }

    @Test
    void parseContract_handlesEmptyFile() {
        Contract contract = new Contract();
        MultipartFile emptyFile = new MockMultipartFile("pdffile", "empty.pdf", "application/pdf", new byte[0]);

        contractFunctions.parseContract(contract, emptyFile);

        // Empty files don't set pdfData
        assertEquals(null, contract.getPdfData());
    }

    @Test
    void contractIsActive_handlesExpiredContract() {
        Contract contract = new Contract();
        contract.setId(3L);
        contract.setEndDate(LocalDate.now().minusDays(1));

        boolean active = contractFunctions.contractIsActive(contract);

        assertThat(active).isFalse();
    }

    @Test
    void deleteContract_deletesExistingContract() {
        Long contractId = 1L;
        Contract contract = new Contract();
        contract.setId(contractId);
        contract.setName("To Delete");
        User user = new User();
        user.setName("TestUser");
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(contractRepository.findById(contractId)).thenReturn(java.util.Optional.of(contract));
        when(userFunctions.getUserFromToken(request)).thenReturn(user);
        when(serviceRepository.findAll()).thenReturn(java.util.List.of());

        String result = contractFunctions.deleteContract(contractId, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        verify(contractRepository).deleteById(contractId);
        verify(logRepository).save(any());
    }

    @Test
    void deleteContract_deletesCascadeServices() {
        Long contractId = 1L;
        Contract contract = new Contract();
        contract.setId(contractId);
        contract.setName("Contract");
        
        Service service = new Service(100L, "Service", "Expo", true, 0, null, null);
        service.setId(100L); // Explicitly set the ID after construction
        service.setContractId(contractId);
        
        User user = new User();
        user.setName("TestUser");
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(contractRepository.findById(contractId)).thenReturn(java.util.Optional.of(contract));
        when(userFunctions.getUserFromToken(request)).thenReturn(user);
        when(serviceRepository.findAll()).thenReturn(java.util.List.of(service));

        String result = contractFunctions.deleteContract(contractId, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        verify(serviceRepository).deleteById(100L);
        verify(contractRepository).deleteById(contractId);
    }

    @Test
    void deleteContract_handlesMissingContract() {
        Long contractId = 1L;
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(contractRepository.findById(contractId)).thenReturn(java.util.Optional.empty());

        String result = contractFunctions.deleteContract(contractId, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        verify(contractRepository, never()).deleteById(any());
    }

    @Test
    void updateContractFields_updatesWithNewPdf() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setName("Updated");
        contract.setPayment("500");
        contract.setStartDate(LocalDate.now());
        contract.setEndDate(LocalDate.now().plusDays(1));
        
        Contract storedContract = new Contract();
        storedContract.setId(1L);
        storedContract.setName("Old");
        storedContract.setPdfData("olddata".getBytes());
        storedContract.setFileName("old.pdf");
        
        MultipartFile pdf = new MockMultipartFile("pdffile", "new.pdf", "application/pdf", "newdata".getBytes());
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(contractRepository.findById(1L)).thenReturn(java.util.Optional.of(storedContract));
        when(eventlogFunctions.handleUpdateRequest(any(), any(), any(), any())).thenReturn("redirect:/sponsors");

        String result = contractFunctions.updateContractFields(contract, pdf, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(contract.getFileName()).isEqualTo("new.pdf");
        verify(eventlogFunctions).handleUpdateRequest(any(), any(), any(), any());
    }

    @Test
    void updateContractFields_keepsOldPdfWhenNoneProvided() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setName("Updated");
        contract.setPayment("500");
        contract.setStartDate(LocalDate.now());
        contract.setEndDate(LocalDate.now().plusDays(1));
        
        Contract storedContract = new Contract();
        storedContract.setId(1L);
        storedContract.setName("Old");
        storedContract.setPdfData("olddata".getBytes());
        storedContract.setFileName("old.pdf");
        
        MultipartFile emptyPdf = new MockMultipartFile("pdffile", "", "application/pdf", new byte[0]);
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(contractRepository.findById(1L)).thenReturn(java.util.Optional.of(storedContract));
        when(eventlogFunctions.handleUpdateRequest(any(), any(), any(), any())).thenReturn("redirect:/sponsors");

        String result = contractFunctions.updateContractFields(contract, emptyPdf, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(contract.getFileName()).isEqualTo("old.pdf");
        assertArrayEquals("olddata".getBytes(), contract.getPdfData());
    }

    @Test
    void updateContractFields_rejectsInvalidContract() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setName("");
        MultipartFile pdf = new MockMultipartFile("pdffile", "new.pdf", "application/pdf", "data".getBytes());
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        String result = contractFunctions.updateContractFields(contract, pdf, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        verify(contractRepository, never()).findById(any());
    }

    @Test
    void updateContractFields_handlesMissingContract() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setName("Valid");
        contract.setPayment("100");
        contract.setStartDate(LocalDate.now());
        contract.setEndDate(LocalDate.now().plusDays(1));
        MultipartFile pdf = new MockMultipartFile("pdffile", "new.pdf", "application/pdf", "data".getBytes());
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        contract.setName("Valid");
        contract.setPayment("100");
        when(contractRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        String result = contractFunctions.updateContractFields(contract, pdf, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.asMap()).containsKey("responseMessage");
    }

    @Test
    void parseContract_setsFilename() {
        Contract contract = new Contract();
        MultipartFile pdf = new MockMultipartFile("pdffile", "path/to/contract.pdf", "application/pdf", new byte[]{1,2,3});

        contractFunctions.parseContract(contract, pdf);

        assertEquals("contract.pdf", contract.getFileName());
    }

    @Test
    void contractIsActive_marksServiceInactiveWhenExpired() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setEndDate(LocalDate.now().minusDays(1));
        Service service = new Service(100L, "Service", "Expo", true, 0, null, null);
        service.setContractId(1L);

        when(serviceRepository.findAll()).thenReturn(List.of(service));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean active = contractFunctions.contractIsActive(contract);

        assertThat(active).isFalse();
        assertThat(service.getActive()).isFalse();
    }
}
