package p3project.functions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Contract;
import p3project.classes.Service;
import p3project.classes.Sponsor;
import p3project.repositories.ContractRepository;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;
import p3project.repositories.SponsorRepository;
import p3project.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class ViewFunctionsTest {

    @Mock private UserRepository userRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private SponsorRepository sponsorRepository;
    @Mock private SponsorFunctions sponsorFunctions;
    @Mock private LogRepository logRepository;
    @Mock private UserFunctions userFunctions;
    @Mock private HttpServletRequest request;

    @InjectMocks
    private ViewFunctions viewFunctions;

    @Test
    void showAdminPanel_addsUsersAndReturnsView() {
        Model model = new ConcurrentModel();
        when(userRepository.findAll()).thenReturn(List.of());

        String view = viewFunctions.showAdminPanelPage(model, request);

        assertThat(view).isEqualTo("AdminPanel");
        assertThat(model.containsAttribute("users")).isTrue();
    }

    @Test
    void showArchivePage_collectsArchivedSponsors() {
        Model model = new ConcurrentModel();
        Sponsor inactive = new Sponsor();
        inactive.setActive(false);
        Sponsor active = new Sponsor();
        active.setActive(true);

        when(userFunctions.userIsAdmin(request)).thenReturn(true);
        doNothing().when(sponsorFunctions).updateActiveFields();
        when(sponsorRepository.findAll()).thenReturn(List.of(inactive, active));
        when(contractRepository.findAll()).thenReturn(List.of());
        when(serviceRepository.findAll()).thenReturn(List.of());

        String view = viewFunctions.showArchivePage(model, request);

        assertThat(view).isEqualTo("homepage");
        @SuppressWarnings("unchecked")
        List<Sponsor> sponsors = (List<Sponsor>) model.getAttribute("sponsors");
        assertThat(sponsors).containsExactly(inactive);
        verify(sponsorFunctions).updateActiveFields();
    }

    @Test
    void showhomepage_addsActiveSponsorsAndFlagsAdmin() {
        Model model = new ConcurrentModel();
        Sponsor active = new Sponsor();
        active.setActive(true);
        Sponsor inactive = new Sponsor();
        inactive.setActive(false);

        when(userFunctions.userIsAdmin(request)).thenReturn(false);
        doNothing().when(sponsorFunctions).updateActiveFields();
        when(sponsorRepository.findAll()).thenReturn(List.of(active, inactive));
        when(contractRepository.findAll()).thenReturn(List.of(new Contract()));
        when(serviceRepository.findAll()).thenReturn(List.of(new Service(1L, "svc", "Banner", true, 0, null, null)));

        String view = viewFunctions.showhomepage(model, request);

        assertThat(view).isEqualTo("homepage");
        @SuppressWarnings("unchecked")
        List<Sponsor> sponsors = (List<Sponsor>) model.getAttribute("sponsors");
        assertThat(sponsors).containsExactly(active);
        assertThat(model.getAttribute("userIsAdmin")).isEqualTo(false);
    }

    @Test
    void returnSponsorPage_addsCollectionsAndReturnsSponsorsView() {
        Model model = new ConcurrentModel();
        doNothing().when(sponsorFunctions).updateActiveFields();
        when(sponsorRepository.findAll()).thenReturn(List.of());
        when(contractRepository.findAll()).thenReturn(List.of());
        when(serviceRepository.findAll()).thenReturn(List.of());

        String view = viewFunctions.returnSponsorPage(model);

        assertThat(view).isEqualTo("sponsors");
        assertThat(model.containsAttribute("sponsors")).isTrue();
        verify(sponsorFunctions).updateActiveFields();
    }
}
