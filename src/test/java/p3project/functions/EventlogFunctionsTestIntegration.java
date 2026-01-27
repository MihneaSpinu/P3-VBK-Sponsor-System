package p3project.functions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import jakarta.servlet.http.Cookie;
import p3project.classes.Contract;
import p3project.classes.Service;
import p3project.classes.Sponsor;
import p3project.classes.User;
import p3project.repositories.ContractRepository;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;
import p3project.repositories.SponsorRepository;
import p3project.repositories.UserRepository;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EventlogFunctionsTestIntegration {

    @Autowired
    private EventlogFunctions eventlogFunctions;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private UserRepository userRepository;

    private MockHttpServletRequest requestWithUser(String username) {
        User user = new User();
        user.setName(username);
        user.setPassword("irrelevant");
        user.setIsAdmin(false);
        user = userRepository.save(user);

        MockHttpServletRequest req = new MockHttpServletRequest();
        // UserFunctions.parseCookie expects "<id>.<hash>" but getUserFromToken does not validate the hash.
        req.setCookies(new Cookie("token", user.getId() + ".dummy"));
        return req;
    }

    @Test
    void compareFields_logsChangesAndSavesSponsor() throws Exception {
        Sponsor stored = new Sponsor();
        stored.setName("Old");

        Sponsor incoming = new Sponsor();
        incoming.setName("New");

        MockHttpServletRequest request = requestWithUser("TestUser");

        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(1, changed);
        assertThat(stored.getName()).isEqualTo("New");
        assertThat(sponsorRepository.count()).isEqualTo(1);
        assertThat(logRepository.count()).isEqualTo(1);
    }

    @Test
    void compareFields_returnsZeroWhenNoDifferences() throws Exception {
        Sponsor stored = new Sponsor();
        stored.setName("Same");

        Sponsor incoming = new Sponsor();
        incoming.setName("Same");

        MockHttpServletRequest request = new MockHttpServletRequest();

        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(0, changed);
        assertThat(logRepository.count()).isEqualTo(0);
        assertThat(sponsorRepository.count()).isEqualTo(1);
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

        Sponsor incoming = new Sponsor();
        incoming.setName("New");

        RedirectAttributes attrs = new RedirectAttributesModelMap();
        MockHttpServletRequest request = requestWithUser("TestUser");

        String result = eventlogFunctions.handleUpdateRequest(incoming, stored, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        assertThat(attrs.getFlashAttributes().get("responseMessage").toString()).contains("Opdateret 1 felt");
        assertThat(logRepository.count()).isEqualTo(1);
    }

    @Test
    void handleUpdateRequest_returnsNoChangesMessage() throws Exception {
        Sponsor stored = new Sponsor();
        stored.setName("Same");

        Sponsor incoming = new Sponsor();
        incoming.setName("Same");

        RedirectAttributes attrs = new RedirectAttributesModelMap();
        MockHttpServletRequest request = requestWithUser("TestUser");

        String result = eventlogFunctions.handleUpdateRequest(incoming, stored, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes().get("responseMessage")).isEqualTo("Ingen felter ændret");
        assertThat(logRepository.count()).isEqualTo(0);
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
        MockHttpServletRequest request = requestWithUser("TestUser");

        String result = eventlogFunctions.handleUpdateRequest(incoming, stored, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes().get("responseMessage").toString()).contains("Opdateret 2 felter");
        assertThat(logRepository.count()).isEqualTo(2);
    }

    @Test
    void compareFields_savesContractCorrectly() throws Exception {
        Contract stored = new Contract();
        stored.setName("Old");
        stored.setPayment("100");

        Contract incoming = new Contract();
        incoming.setName("New");
        incoming.setPayment("100");

        MockHttpServletRequest request = requestWithUser("TestUser");

        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(1, changed);
        assertThat(stored.getName()).isEqualTo("New");
        assertThat(contractRepository.count()).isEqualTo(1);
        assertThat(logRepository.count()).isEqualTo(1);
    }

    @Test
    void compareFields_savesServiceCorrectly() throws Exception {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        Service stored = new Service(1L, "ServiceA", "Tickets", true, 20, startDate, endDate);
        Service incoming = new Service(1L, "ServiceA", "Tickets", false, 20, startDate, endDate);

        MockHttpServletRequest request = requestWithUser("TestUser");

        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(1, changed);
        assertThat(stored.getActive()).isEqualTo(false);
        assertThat(serviceRepository.count()).isEqualTo(1);
        assertThat(logRepository.count()).isEqualTo(1);
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

        MockHttpServletRequest request = requestWithUser("TestUser");

        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(3, changed);
        assertThat(stored.getName()).isEqualTo("New1");
        assertThat(stored.getContactPerson()).isEqualTo("New2");
        assertThat(stored.getEmail()).isEqualTo("new@test.com");
        assertThat(logRepository.count()).isEqualTo(3);
    }

    @Test
    void compareFields_ignoresPdfDataField() throws Exception {
        Contract stored = new Contract();
        stored.setName("Same");
        stored.setPdfData("olddata".getBytes());

        Contract incoming = new Contract();
        incoming.setName("Same");
        incoming.setPdfData("newdata".getBytes());

        MockHttpServletRequest request = new MockHttpServletRequest();

        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(0, changed);
        assertThat(logRepository.count()).isEqualTo(0);
        assertThat(contractRepository.count()).isEqualTo(1);

        // Even though it's not logged, the field is still updated.
        Contract persisted = contractRepository.findAll().get(0);
        assertThat(persisted.getPdfData()).isEqualTo("newdata".getBytes());
    }

    @Test
    void compareFields_handlesNullValues() throws Exception {
        Sponsor stored = new Sponsor();
        stored.setName("Old");
        stored.setEmail(null);

        Sponsor incoming = new Sponsor();
        incoming.setName("Old");
        incoming.setEmail("new@test.com");

        MockHttpServletRequest request = requestWithUser("TestUser");

        Integer changed = eventlogFunctions.compareFields(incoming, stored, request);

        assertEquals(1, changed);
        assertThat(stored.getEmail()).isEqualTo("new@test.com");
        assertThat(logRepository.count()).isEqualTo(1);
    }

    private static class Dummy {
        @SuppressWarnings("unused")
        byte[] pdfData;
        @SuppressWarnings("unused")
        String name;
    }
}
