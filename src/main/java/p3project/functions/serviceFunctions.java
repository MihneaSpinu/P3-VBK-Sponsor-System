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
public class ServiceFunctions {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private UserFunctions uF;

    @Autowired
    private EventlogFunctions eF;
    
    public boolean serviceIsActive(Service service) {
        if((service.getType().equals("Banner")      || 
            service.getType().equals("LogoTrojer")  || 
            service.getType().equals("LogoBukser")) &&
            service.getEndDate() != null                      &&
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
            User user = uF.getUserFromToken(request);
            Eventlog log = new Eventlog(user, service, "Opdaterede");
            logRepository.save(log);
        } catch (RuntimeException ex) {
            return ResponseEntity.internalServerError().body("Internal server error");
        }

        return ResponseEntity.ok("ok");
    }

    
    private boolean serviceIsValid(Service service) {
        if(service.getName() == null || service.getName().isEmpty()) return false;

        LocalDate start = service.getStartDate();
        LocalDate end = service.getEndDate();
        if(start != null && end != null && start.isAfter(end)) return false;

        String type = service.getType() == null ? "" : service.getType();
        if (type.equals("LogoTrojer") || type.equals("LogoBukser")) {
            if (service.getDivision() < 0) return false;
        } else {
            if (service.getAmount() < 0) return false;
        }

        return true;
    }

    public String deleteService(Long serviceId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Service service = serviceRepository.findById(serviceId).orElse(null);
        if(service == null) {
            redirectAttributes.addFlashAttribute("responseMessage", "Intern serverfejl, prøv igen");
            return "redirect:/sponsors";
        }

        User user = uF.getUserFromToken(request);
        Eventlog log = new Eventlog(user, service, "Slettede");
        logRepository.save(log);

        serviceRepository.deleteById(serviceId);
        return "redirect:/sponsors";
    }


    public String addServiceForContract(Service service, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = uF.getUserFromToken(request);
        Eventlog log = new Eventlog(user, service, "Oprettede");
        logRepository.save(log);

        if(!serviceIsValid(service)){
            redirectAttributes.addFlashAttribute("responseMessage", "Ugyldig information indtastet");
            return "redirect:/sponsors";
        }
        try {
            serviceRepository.save(service);
            redirectAttributes.addFlashAttribute("responseMessage", "Tilføjet tjeneste: " + service.getName());
            return "redirect:/sponsors";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("responseMessage", "Intern serverfejl, prøv igen");
            return "redirect:/sponsors";
        }
    }

    public String updateServiceFields(Service service, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        if(!serviceIsValid(service)){
            redirectAttributes.addFlashAttribute("responseMessage", "Ugyldig information indtastet");
            return "redirect:/sponsors";
        }

        Service storedService = serviceRepository.findById(service.getId()).orElse(null);
        
        if(storedService == null) {
            redirectAttributes.addAttribute("responseMessage", "Intern serverfejl, prøv igen");
            return "redirect:/sponsors";
        }
        return eF.handleUpdateRequest(service, storedService, request, redirectAttributes);
    }
}