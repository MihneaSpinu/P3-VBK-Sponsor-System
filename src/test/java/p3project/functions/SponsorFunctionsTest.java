package p3project.functions;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
class SponsorFunctionsTest {

    @Mock
    private SponsorRepository sponsorRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ContractFunctions contractFunctions;

    @Mock
    private UserFunctions userFunctions;

    @Mock
    private EventlogFunctions eventlogFunctions;

    @Mock
    private LogRepository logRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SponsorFunctions sponsorFunctions;

    @Test
    void sponsorIsValid_acceptsCorrectData() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName("ACME");
        sponsor.setPhoneNumber("+4512345678");
        sponsor.setCvrNumber("12345678");

        assertThat(sponsorFunctions.sponsorIsValid(sponsor)).isTrue();
    }

    @Test
    void sponsorIsValid_rejectsInvalidPhoneOrCvr() {
        Sponsor missingName = new Sponsor();
        missingName.setPhoneNumber("");
        missingName.setCvrNumber("");

        Sponsor invalidPhone = new Sponsor();
        invalidPhone.setName("ACME");
        invalidPhone.setCvrNumber("");
        invalidPhone.setPhoneNumber("abc");

        Sponsor invalidCvr = new Sponsor();
        invalidCvr.setName("ACME");
        invalidCvr.setPhoneNumber("");
        invalidCvr.setCvrNumber("123");

        assertThat(sponsorFunctions.sponsorIsValid(missingName)).isFalse();
        assertThat(sponsorFunctions.sponsorIsValid(invalidPhone)).isFalse();
        assertThat(sponsorFunctions.sponsorIsValid(invalidCvr)).isFalse();
    }

    @Test
    void updateActiveFields_marksSponsorActiveWhenContractActive() {
        Sponsor sponsor = new Sponsor();
        sponsor.setId(1L);

        Contract contract = new Contract();
        contract.setId(10L);
        contract.setSponsorId(1L);

        when(contractRepository.findAll()).thenReturn(List.of(contract));
        when(sponsorRepository.findAll()).thenReturn(List.of(sponsor));
        when(contractFunctions.contractIsActive(contract)).thenReturn(true);

        sponsorFunctions.updateActiveFields();

        verify(sponsorRepository).save(sponsor);
        assertThat(sponsor.getActive()).isTrue();
    }

    @Test
    void sponsorIsValid_acceptsEmptyCvrWithValidPhone() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName("Test Sponsor");
        sponsor.setPhoneNumber("12345678");
        sponsor.setCvrNumber("");

        assertThat(sponsorFunctions.sponsorIsValid(sponsor)).isTrue();
    }

    @Test
    void sponsorIsValid_acceptsEmptyPhoneWithValidCvr() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName("Test Sponsor");
        sponsor.setPhoneNumber("");
        sponsor.setCvrNumber("12345678");

        assertThat(sponsorFunctions.sponsorIsValid(sponsor)).isTrue();
    }

    @Test
    void sponsorIsValid_rejectsNullName() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName(null);
        sponsor.setPhoneNumber("12345678");
        sponsor.setCvrNumber("");

        assertThat(sponsorFunctions.sponsorIsValid(sponsor)).isFalse();
    }

    @Test
    void contractIsActive_returnsTrueForActiveContract() {
        Contract contract = new Contract();
        contract.setActive(true);
        contract.setEndDate(LocalDate.now().plusDays(1));

        when(contractFunctions.contractIsActive(contract)).thenReturn(true);

        assertThat(contractFunctions.contractIsActive(contract)).isTrue();
    }

    @Test
    void contractIsActive_returnsFalseForInactiveContract() {
        Contract contract = new Contract();
        contract.setActive(false);

        when(contractFunctions.contractIsActive(contract)).thenReturn(false);

        assertThat(contractFunctions.contractIsActive(contract)).isFalse();
    }

    @Test
    void updateActiveFields_marksSponsorInactiveWhenNoActiveContracts() {
        Sponsor sponsor = new Sponsor();
        sponsor.setId(1L);
        sponsor.setActive(true);

        Contract contract = new Contract();
        contract.setId(10L);
        contract.setSponsorId(1L);

        when(contractRepository.findAll()).thenReturn(List.of(contract));
        when(sponsorRepository.findAll()).thenReturn(List.of(sponsor));
        when(contractFunctions.contractIsActive(contract)).thenReturn(false);

        sponsorFunctions.updateActiveFields();

        verify(sponsorRepository).save(sponsor);
        assertThat(sponsor.getActive()).isFalse();
    }

    @Test
    void updateActiveFields_handlesSponsorsWithNoContracts() {
        Sponsor sponsor = new Sponsor();
        sponsor.setId(1L);
        sponsor.setActive(true);

        when(contractRepository.findAll()).thenReturn(List.of());
        when(sponsorRepository.findAll()).thenReturn(List.of(sponsor));

        sponsorFunctions.updateActiveFields();

        verify(sponsorRepository).save(sponsor);
        assertThat(sponsor.getActive()).isFalse();
    }

    @Test
    void addSponsor_savesValidSponsor() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName("New Sponsor");
        sponsor.setPhoneNumber("12345678");
        sponsor.setCvrNumber("");
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        User user = new User();
        user.setName("TestUser");

        when(userFunctions.getUserFromToken(request)).thenReturn(user);

        String result = sponsorFunctions.addSponsor(sponsor, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        verify(sponsorRepository).save(sponsor);
        verify(logRepository).save(any());
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
    }

    @Test
    void addSponsor_rejectsInvalidSponsor() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName("");
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        String result = sponsorFunctions.addSponsor(sponsor, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        verify(sponsorRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void updateSponsor_updatesValidSponsor() {
        Sponsor sponsor = new Sponsor();
        sponsor.setId(1L);
        sponsor.setName("Updated");
        sponsor.setPhoneNumber("87654321");
        sponsor.setCvrNumber("");
        
        Sponsor storedSponsor = new Sponsor();
        storedSponsor.setId(1L);
        storedSponsor.setName("Old");
        
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(sponsorRepository.findById(1L)).thenReturn(Optional.of(storedSponsor));
        when(eventlogFunctions.handleUpdateRequest(sponsor, storedSponsor, request, attrs))
            .thenReturn("redirect:/sponsors");

        String result = sponsorFunctions.updateSponsor(sponsor, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        verify(eventlogFunctions).handleUpdateRequest(sponsor, storedSponsor, request, attrs);
    }

    @Test
    void updateSponsor_rejectsInvalidSponsor() {
        Sponsor sponsor = new Sponsor();
        sponsor.setId(1L);
        sponsor.setName("");
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        String result = sponsorFunctions.updateSponsor(sponsor, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        verify(sponsorRepository, org.mockito.Mockito.never()).findById(any());
    }

    @Test
    void updateSponsor_handlesMissingSponsor() {
        Sponsor sponsor = new Sponsor();
        sponsor.setId(1L);
        sponsor.setName("Valid");
        sponsor.setPhoneNumber("");
        sponsor.setCvrNumber("");
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(sponsorRepository.findById(1L)).thenReturn(Optional.empty());

        String result = sponsorFunctions.updateSponsor(sponsor, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
    }

    @Test
    void deleteSponsor_deletesExistingSponsor() {
        Long sponsorId = 1L;
        Sponsor sponsor = new Sponsor();
        sponsor.setId(sponsorId);
        sponsor.setName("To Delete");
        User user = new User();
        user.setName("TestUser");
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(sponsorRepository.findById(sponsorId)).thenReturn(Optional.of(sponsor));
        when(userFunctions.getUserFromToken(request)).thenReturn(user);
        when(contractRepository.findAll()).thenReturn(List.of());
        when(serviceRepository.findAll()).thenReturn(List.of());

        String result = sponsorFunctions.deleteSponsor(sponsorId, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        verify(sponsorRepository).deleteById(sponsorId);
        verify(logRepository).save(any());
    }

    @Test
    void deleteSponsor_deletesCascadeContractsAndServices() {
        Long sponsorId = 1L;
        Sponsor sponsor = new Sponsor();
        sponsor.setId(sponsorId);
        sponsor.setName("Sponsor");
        
        Contract contract = new Contract();
        contract.setId(10L);
        contract.setSponsorId(sponsorId);
        
        Service service = new Service(100L, "Service", "Expo", true, 0, null, null);
        service.setId(100L); // Explicitly set the ID after construction
        service.setContractId(10L);
        
        User user = new User();
        user.setName("TestUser");
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(sponsorRepository.findById(sponsorId)).thenReturn(Optional.of(sponsor));
        when(userFunctions.getUserFromToken(request)).thenReturn(user);
        when(contractRepository.findAll()).thenReturn(List.of(contract));
        when(serviceRepository.findAll()).thenReturn(List.of(service));

        String result = sponsorFunctions.deleteSponsor(sponsorId, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        verify(serviceRepository).deleteById(100L);
        verify(contractRepository).deleteById(10L);
        verify(sponsorRepository).deleteById(sponsorId);
    }

    @Test
    void deleteSponsor_handlesMissingSponsor() {
        Long sponsorId = 1L;
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(sponsorRepository.findById(sponsorId)).thenReturn(Optional.empty());

        String result = sponsorFunctions.deleteSponsor(sponsorId, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        verify(sponsorRepository, org.mockito.Mockito.never()).deleteById(any());
    }

    @Test
    void sponsorIsValid_rejectsEmptyName() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName("");
        sponsor.setPhoneNumber("12345678");
        sponsor.setCvrNumber("");

        assertThat(sponsorFunctions.sponsorIsValid(sponsor)).isFalse();
    }

    @Test
    void sponsorIsValid_rejectsCvrWithLetters() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName("Valid");
        sponsor.setPhoneNumber("");
        sponsor.setCvrNumber("1234567A");

        assertThat(sponsorFunctions.sponsorIsValid(sponsor)).isFalse();
    }

    @Test
    void sponsorIsValid_acceptsPhoneWithPlusAndDash() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName("Valid");
        sponsor.setPhoneNumber("+45-12345678");
        sponsor.setCvrNumber("");

        assertThat(sponsorFunctions.sponsorIsValid(sponsor)).isTrue();
    }

    @Test
    void updateActiveFields_handlesMultipleSponsorsAndContracts() {
        Sponsor sponsor1 = new Sponsor();
        sponsor1.setId(1L);
        Sponsor sponsor2 = new Sponsor();
        sponsor2.setId(2L);

        Contract contract1 = new Contract();
        contract1.setId(10L);
        contract1.setSponsorId(1L);
        Contract contract2 = new Contract();
        contract2.setId(20L);
        contract2.setSponsorId(2L);

        when(contractRepository.findAll()).thenReturn(List.of(contract1, contract2));
        when(sponsorRepository.findAll()).thenReturn(List.of(sponsor1, sponsor2));
        when(contractFunctions.contractIsActive(contract1)).thenReturn(true);
        when(contractFunctions.contractIsActive(contract2)).thenReturn(false);

        sponsorFunctions.updateActiveFields();

        verify(sponsorRepository).save(sponsor1);
        verify(sponsorRepository).save(sponsor2);
        assertThat(sponsor1.getActive()).isTrue();
        assertThat(sponsor2.getActive()).isFalse();
    }
}
