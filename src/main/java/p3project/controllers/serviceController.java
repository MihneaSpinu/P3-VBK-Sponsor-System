package p3project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Service;
import p3project.functions.ServiceFunctions;
import p3project.functions.UserFunctions;

@Controller
public class ServiceController{
    
    @Autowired
    private ServiceFunctions serviceFunctions;

    @Autowired
    private UserFunctions userFunctions;

    //Wrappers
    private boolean userHasValidToken(HttpServletRequest request) {
        return userFunctions.userHasValidToken(request);
    }

    private boolean userIsAdmin(HttpServletRequest request) {
        return userFunctions.userIsAdmin(request);
    }

    private  ResponseEntity<String> setServiceArchived(Long serviceId, boolean active, HttpServletRequest request) {
        return serviceFunctions.setServiceArchived(serviceId, active, request);
    }
    private String deleteService(Long serviceId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return serviceFunctions.deleteService(serviceId, request, redirectAttributes);
    }

    private String addServiceForContract(Service service, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return serviceFunctions.addServiceForContract(service, request, redirectAttributes);
    }

    private String updateServiceFields(Service service, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return serviceFunctions.updateServiceFields(service, request, redirectAttributes);
    }

    @PostMapping("/sponsors/setServiceArchived")
    public ResponseEntity<String> setServiceArchivedMapping(@RequestParam Long serviceId, @RequestParam boolean active, HttpServletRequest request) {
        if(!userHasValidToken(request)) return ResponseEntity.status(403).body("forbidden");
        if(!userIsAdmin(request))       return ResponseEntity.status(403).body("forbidden");

        return setServiceArchived(serviceId, active, request);
    }

        // Deletes a service by ID
    @PostMapping("/sponsors/deleteService")
    public String deleteServiceMapping(@RequestParam Long serviceId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        return deleteService(serviceId, request, redirectAttributes);
    }

     // Handles creating a new service for a contract
    @PostMapping("/sponsors/addService")
    public String addServiceForContractMapping(@ModelAttribute Service service, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        return addServiceForContract(service, request, redirectAttributes);
    }

    @PostMapping("/update/service")
    public String updateServiceFieldsMapping(@ModelAttribute Service service, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        return updateServiceFields(service, request, redirectAttributes);

    }
}