package p3project.functions;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Eventlog;
import p3project.classes.Service;
import p3project.classes.User;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;

@ExtendWith(MockitoExtension.class)
class ServiceFunctionsTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private LogRepository logRepository;

    @Mock
    private UserFunctions userFunctions;

    @Mock
    private EventlogFunctions eventlogFunctions;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ServiceFunctions serviceFunctions;

    @Test
    void serviceIsActive_deactivatesExpiredBanner() {
        Service service = new Service(1L, "Name", "Banner", true, LocalDate.now().minusDays(2), LocalDate.now().minusDays(1), 0, 0);

        boolean active = serviceFunctions.serviceIsActive(service);

        assertThat(active).isFalse();
        verify(serviceRepository).save(service);
    }

    @Test
    void serviceIsActive_leavesNonExpiringServiceUntouched() {
        Service service = new Service(2L, "Name", "Expo", true, null, null, 0, 0);

        boolean active = serviceFunctions.serviceIsActive(service);

        assertThat(active).isTrue();
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void setServiceArchived_returnsNotFoundWhenMissing() {
        when(serviceRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        ResponseEntity<String> response = serviceFunctions.setServiceArchived(1L, false, request);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void setServiceArchived_updatesStatusAndLogs() {
        Service service = new Service(5L, "Name", "Expo", true, null, null, 0, 0);
        when(serviceRepository.findById(5L)).thenReturn(java.util.Optional.of(service));
        when(userFunctions.getUserFromToken(request)).thenReturn(new User());

        ResponseEntity<String> response = serviceFunctions.setServiceArchived(5L, false, request);

        assertEquals(200, response.getStatusCode().value());
        assertThat(service.getActive()).isFalse();
        verify(serviceRepository).save(service);
        verify(logRepository).save(any(Eventlog.class));
    }

    @Test
    void addServiceForContract_rejectsInvalidService() {
        Service service = new Service(6L, "", "Expo", true, null, null, 0, 0);
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(userFunctions.getUserFromToken(request)).thenReturn(new User());

        String result = serviceFunctions.addServiceForContract(service, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void addServiceForContract_savesValidService() {
        Service service = new Service(7L, "Print", "Expo", true, LocalDate.now(), LocalDate.now().plusDays(1), 10, 0);
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(userFunctions.getUserFromToken(request)).thenReturn(new User());

        String result = serviceFunctions.addServiceForContract(service, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        verify(logRepository).save(any(Eventlog.class));
        verify(serviceRepository).save(service);
    }

    @Test
    void updateServiceFields_returnsRedirectWhenMissingStoredService() {
        Service service = new Service(9L, "Demo", "Expo", true, null, null, 0, 1);
        service.setId(9L);
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(serviceRepository.findById(9L)).thenReturn(java.util.Optional.empty());

        String result = serviceFunctions.updateServiceFields(service, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
    }

    @Test
    void serviceIsActive_deactivatesExpiredLogoTrojer() {
        Service service = new Service(10L, "Logo", "LogoTrojer", true, LocalDate.now().minusDays(5), LocalDate.now().minusDays(1), 0, 5);

        boolean active = serviceFunctions.serviceIsActive(service);

        assertThat(active).isFalse();
        assertThat(service.getActive()).isFalse();
        verify(serviceRepository).save(service);
    }

    @Test
    void serviceIsActive_deactivatesExpiredLogoBukser() {
        Service service = new Service(11L, "Logo", "LogoBukser", true, LocalDate.now().minusDays(3), LocalDate.now().minusDays(1), 0, 5);

        boolean active = serviceFunctions.serviceIsActive(service);

        assertThat(active).isFalse();
        assertThat(service.getActive()).isFalse();
        verify(serviceRepository).save(service);
    }

    @Test
    void serviceIsActive_keepsActiveBannerWithFutureEndDate() {
        Service service = new Service(12L, "Banner", "Banner", true, LocalDate.now(), LocalDate.now().plusDays(10), 0, 0);

        boolean active = serviceFunctions.serviceIsActive(service);

        assertThat(active).isTrue();
        assertThat(service.getActive()).isTrue();
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void serviceIsActive_keepsActiveServiceWithNullEndDate() {
        Service service = new Service(13L, "Logo", "LogoTrojer", true, LocalDate.now(), null, 0, 2);

        boolean active = serviceFunctions.serviceIsActive(service);

        assertThat(active).isTrue();
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void deleteService_returnsErrorWhenServiceNotFound() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        when(serviceRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        String result = serviceFunctions.deleteService(99L, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        verify(serviceRepository, never()).deleteById(any());
    }

    @Test
    void deleteService_deletesServiceAndLogs() {
        Service service = new Service(14L, "TestService", "Expo", true, null, null, 0, 5);
        User user = new User();
        user.setName("Admin");
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(serviceRepository.findById(14L)).thenReturn(java.util.Optional.of(service));
        when(userFunctions.getUserFromToken(request)).thenReturn(user);

        String result = serviceFunctions.deleteService(14L, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        verify(logRepository).save(any(Eventlog.class));
        verify(serviceRepository).deleteById(14L);
    }

    @Test
    void addServiceForContract_validatesServiceName() {
        Service service = new Service(15L, "ValidName", "Expo", true, null, null, 0, 5);
        service.setName("");
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(userFunctions.getUserFromToken(request)).thenReturn(new User());

        String result = serviceFunctions.addServiceForContract(service, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void addServiceForContract_rejectsServiceWithStartAfterEnd() {
        Service service = new Service(16L, "Invalid", "Banner", true, LocalDate.now().plusDays(5), LocalDate.now(), 0, 5);
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(userFunctions.getUserFromToken(request)).thenReturn(new User());

        String result = serviceFunctions.addServiceForContract(service, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void addServiceForContract_rejectsExpoWithNegativeAmount() {
        Service service = new Service(19L, "Expo", "Expo", true, null, null, -10, 0);
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(userFunctions.getUserFromToken(request)).thenReturn(new User());

        String result = serviceFunctions.addServiceForContract(service, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void addServiceForContract_savesValidLogoTrojerService() {
        Service service = new Service(20L, "Logo Jerseys", "LogoTrojer", true, LocalDate.now(), LocalDate.now().plusMonths(1), 0, 2);
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(userFunctions.getUserFromToken(request)).thenReturn(new User());

        String result = serviceFunctions.addServiceForContract(service, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        verify(logRepository).save(any(Eventlog.class));
        verify(serviceRepository).save(service);
    }

    @Test
    void updateServiceFields_rejectsInvalidService() {
        Service service = new Service(21L, "", "Expo", true, null, null, 0, 1);
        service.setId(21L);
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        String result = serviceFunctions.updateServiceFields(service, request, attrs);

        assertThat(result).isEqualTo("redirect:/sponsors");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
        verify(serviceRepository, never()).findById(any());
    }

    @Test
    void updateServiceFields_updatesValidService() {
        Service storedService = new Service(22L, "Old Name", "Expo", true, null, null, 5, 0);
        storedService.setId(22L);
        Service updatedService = new Service(22L, "New Name", "Expo", true, null, null, 10, 0);
        updatedService.setId(22L);
        RedirectAttributes attrs = new RedirectAttributesModelMap();

        when(serviceRepository.findById(22L)).thenReturn(java.util.Optional.of(storedService));

        serviceFunctions.updateServiceFields(updatedService, request, attrs);

        verify(eventlogFunctions).handleUpdateRequest(updatedService, storedService, request, attrs);
    }

    @Test
    void setServiceArchived_activatesInactiveService() {
        Service service = new Service(23L, "Inactive", "Expo", false, null, null, 0, 0);
        when(serviceRepository.findById(23L)).thenReturn(java.util.Optional.of(service));
        when(userFunctions.getUserFromToken(request)).thenReturn(new User());

        ResponseEntity<String> response = serviceFunctions.setServiceArchived(23L, true, request);

        assertEquals(200, response.getStatusCode().value());
        assertThat(service.getActive()).isTrue();
        verify(serviceRepository).save(service);
        verify(logRepository).save(any(Eventlog.class));
    }
}
