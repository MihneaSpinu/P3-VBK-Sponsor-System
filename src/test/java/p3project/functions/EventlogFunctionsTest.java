package p3project.functions;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Contract;
import p3project.classes.Service;
import p3project.classes.Sponsor;
import p3project.classes.User;
import p3project.repositories.ContractRepository;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;
import p3project.repositories.SponsorRepository;

@ExtendWith(MockitoExtension.class)
class EventlogFunctionsTest {

    @Mock
    private LogRepository logRepository;

    @Mock
    private SponsorRepository sponsorRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private UserFunctions userFunctions;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private EventlogFunctions eventlogFunctions;

    @Test
    void compareFields_logsChangesAndSavesSponsor() throws Exception {
        Sponsor stored = new Sponsor();
        stored.setName("Old");

        Sponsor incoming = new Sponsor();
        incoming.setName("New");

        when(userFunctions.getUserFromToken(request)).thenReturn(new User());


        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(1, changed);
        assertThat(stored.getName()).isEqualTo("New");
        verify(logRepository).save(any());
        verify(sponsorRepository).save(stored);
    }

    @Test
    void compareFields_returnsZeroWhenNoDifferences() throws Exception {
        Sponsor stored = new Sponsor();
        stored.setName("Same");
        Sponsor incoming = new Sponsor();
        incoming.setName("Same");

        when(sponsorRepository.save(any(Sponsor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(0, changed);
        verify(logRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void fieldShouldBeLogged_skipsPdfData() throws Exception {
        Field pdfField = Dummy.class.getDeclaredField("pdfData");
        Field otherField = Dummy.class.getDeclaredField("name");

        assertThat(eventlogFunctions.fieldShouldBeLogged(pdfField)).isFalse();
        assertThat(eventlogFunctions.fieldShouldBeLogged(otherField)).isTrue();
    }

    @Test
    void handleUpdateRequest_returnsSuccessMessageForChanges() throws Exception {
        Sponsor stored = new Sponsor();
        stored.setName("Old");
        stored.setId(1L);
        Sponsor incoming = new Sponsor();
        incoming.setName("New");
        incoming.setId(1L);
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        User user = new User();
        user.setName("TestUser");

        when(userFunctions.getUserFromToken(request)).thenReturn(user);
        when(sponsorRepository.save(any(Sponsor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = eventlogFunctions.handleUpdateRequest(incoming, stored, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        assertThat(attrs.getFlashAttributes().get("responseMessage").toString()).contains("Opdateret 1 felt");
    }

    @Test
    void handleUpdateRequest_returnsNoChangesMessage() throws Exception {
        Sponsor stored = new Sponsor();
        stored.setName("Same");
        Sponsor incoming = new Sponsor();
        incoming.setName("Same");
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(sponsorRepository.save(any(Sponsor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = eventlogFunctions.handleUpdateRequest(incoming, stored, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes().get("responseMessage")).isEqualTo("Ingen felter ændret");
    }

    @Test
    void handleUpdateRequest_returnsMultipleFieldsMessage() throws Exception {
        Sponsor stored = new Sponsor();
        stored.setName("Old");
        stored.setContactPerson("OldContact");
        Sponsor incoming = new Sponsor();
        incoming.setName("New");
        incoming.setContactPerson("NewContact");
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        User user = new User();
        user.setName("TestUser");

        when(userFunctions.getUserFromToken(request)).thenReturn(user);
        when(sponsorRepository.save(any(Sponsor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = eventlogFunctions.handleUpdateRequest(incoming, stored, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes().get("responseMessage").toString()).contains("Opdateret 2 felter");
    }

    @Test
    void compareFields_savesContractCorrectly() throws Exception {
        Contract stored = new Contract();
        stored.setName("Old");
        stored.setPayment("100");
        Contract incoming = new Contract();
        incoming.setName("New");
        incoming.setPayment("100");
        User user = new User();
        user.setName("TestUser");

        when(userFunctions.getUserFromToken(request)).thenReturn(user);
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(1, changed);
        assertThat(stored.getName()).isEqualTo("New");
        verify(contractRepository).save(stored);
    }

    @Test
    void compareFields_savesServiceCorrectly() throws Exception {
        Service stored = new Service(1L, "Service", "Old", true, null, null, 0, 0);
        Service incoming = new Service(1L, "Service", "New", true, null, null, 0, 0);
        User user = new User();
        user.setName("TestUser");

        when(userFunctions.getUserFromToken(request)).thenReturn(user);
        when(serviceRepository.save(any(Service.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(1, changed);
        assertThat(stored.getType()).isEqualTo("New");
        verify(serviceRepository).save(stored);
    }

    @Test
    void compareFields_countsMultipleChanges() throws Exception {
        Sponsor stored = new Sponsor();
        stored.setName("Old1");
        stored.setContactPerson("Old2");
        stored.setEmail("old@test.com");
        Sponsor incoming = new Sponsor();
        incoming.setName("New1");
        incoming.setContactPerson("New2");
        incoming.setEmail("new@test.com");
        User user = new User();
        user.setName("TestUser");

        when(userFunctions.getUserFromToken(request)).thenReturn(user);
        when(sponsorRepository.save(any(Sponsor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(3, changed);
        assertThat(stored.getName()).isEqualTo("New1");
        assertThat(stored.getContactPerson()).isEqualTo("New2");
        assertThat(stored.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void compareFields_ignoresPdfDataField() throws Exception {
        Contract stored = new Contract();
        stored.setName("Same");
        stored.setPdfData("olddata".getBytes());
        Contract incoming = new Contract();
        incoming.setName("Same");
        incoming.setPdfData("newdata".getBytes());

        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(0, changed);
        verify(logRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void compareFields_handlesNullValues() throws Exception {
        Sponsor stored = new Sponsor();
        stored.setName("Old");
        stored.setEmail(null);
        Sponsor incoming = new Sponsor();
        incoming.setName("Old");
        incoming.setEmail("new@test.com");
        User user = new User();
        user.setName("TestUser");

        when(userFunctions.getUserFromToken(request)).thenReturn(user);
        when(sponsorRepository.save(any(Sponsor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(1, changed);
        assertThat(stored.getEmail()).isEqualTo("new@test.com");
    }

    private static class Dummy {
        byte[] pdfData;
        String name;
    }
}
