package p3project.functions;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Eventlog;
import p3project.classes.Service;
import p3project.classes.User;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;


@Component
public class ServiceFunctions{

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private UserFunctions userFunctions;

    @Autowired
    private EventlogFunctions eventlogFunctions;
    
    public User getUserFromToken(HttpServletRequest request) throws RuntimeException {
        return userFunctions.getUserFromToken(request);
    }

    private <T> String handleUpdateRequest(T requestObject, T storedObject, HttpServletRequest request, RedirectAttributes redirectAttributes){
        return eventlogFunctions.handleUpdateRequest(requestObject, storedObject, request, redirectAttributes);
    }

    public boolean serviceIsActive(Service service) {
        if((service.getType().equals("Banner")      || 
            service.getType().equals("LogoTrojer")  || 
            service.getType().equals("LogoBukser")) &&
            LocalDate.now().isAfter(service.getEndDate())) {

            service.setActive(false);
            serviceRepository.save(service);
        }
        return service.getActive();
    }

    public ResponseEntity<String> setServiceArchived(Long serviceId, boolean active, HttpServletRequest request) {

        Service service = serviceRepository.findById(serviceId).orElse(null);
        if (service == null) return ResponseEntity.status(404).body("not found");

        service.setActive(active);
        serviceRepository.save(service);

        try {
            User user = getUserFromToken(request);
            Eventlog log = new Eventlog(user, service, "Opdattede");
            logRepository.save(log);
        } catch (Exception ex) {
        }

        return ResponseEntity.ok("ok");
    }

    
    private boolean serviceIsValid(Service service) {
        if(service.getName() == null || service.getName().isEmpty()) return false;

        LocalDate start = service.getStartDate();
        LocalDate end = service.getEndDate();
        if(start != null && end != null && start.isAfter(end)) return false;

        if(service.getAmountOrDivision() < 0) return false;

        return true;
    }

    public String deleteService(Long serviceId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Service service = serviceRepository.findById(serviceId).orElse(null);
        if(service == null) {
            redirectAttributes.addFlashAttribute("responseMessage", "Intern serverfejl, prøv igen");
            return "redirect:/sponsors";
        }

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, service, "Slettede");
        logRepository.save(log);

        serviceRepository.deleteById(serviceId);
        return "redirect:/sponsors";

    }
    public String addServiceForContract(Service service, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, service, "Oprettede");
        logRepository.save(log);
        service.setActive(true);
        serviceIsActive(service);

        if(!serviceIsValid(service)){
            redirectAttributes.addFlashAttribute("responseMessage", "Tjenesten er ikke valid");
            return "redirect:/sponsors";
        }
        try {
            serviceRepository.save(service);
            redirectAttributes.addFlashAttribute("responseMessage", "Tilføjet service: " + service.getName());
            return "redirect:/sponsors";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("responseMessage", "Intern serverfejl, prøv igen");
            return "redirect:/sponsors";
        }
    }

    public String updateServiceFields(Service service, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        if(!serviceIsValid(service)){
            redirectAttributes.addFlashAttribute("responseMessage", "Tjenesten er ikke valid");
            return "redirect:/sponsors";
        }

        Service storedService = serviceRepository.findById(service.getId()).orElse(null);
        
        service.setActive(true);
        service.setActive(serviceIsActive(service));
        if(storedService == null) {
            redirectAttributes.addAttribute("responseMessage", "Intern serverfejl, prøv igen");
            return "redirect:/sponsors";
        }
        return handleUpdateRequest(service, storedService, request, redirectAttributes);
    }
}