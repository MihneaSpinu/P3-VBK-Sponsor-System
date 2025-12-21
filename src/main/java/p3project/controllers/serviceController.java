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
public class ServiceController {
    
    @Autowired
    private ServiceFunctions seF;

    @Autowired
    private UserFunctions uF;

    @PostMapping("/api/service/update/active")
    public ResponseEntity<String> setServiceArchivedMapping(@RequestParam Long serviceId, @RequestParam boolean active, HttpServletRequest request) {
        if(!uF.userHasValidToken(request)) return ResponseEntity.status(403).body("forbidden");
        if(!uF.userIsAdmin(request))       return ResponseEntity.status(403).body("forbidden");

        return seF.setServiceArchived(serviceId, active, request);
    }


    @PostMapping("/api/service/delete")
    public String deleteServiceMapping(@RequestParam Long serviceId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        if(!uF.userIsAdmin(request))       return "redirect:/homepage";

        return seF.deleteService(serviceId, request, redirectAttributes);
    }


    @PostMapping("/api/service/add")
    public String addServiceForContractMapping(@ModelAttribute Service service, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        if(!uF.userIsAdmin(request))       return "redirect:/homepage";

        return seF.addServiceForContract(service, request, redirectAttributes);
    }

    @PostMapping("/api/service/update")
    public String updateServiceFieldsMapping(@ModelAttribute Service service, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        if(!uF.userIsAdmin(request))       return "redirect:/homepage";

        return seF.updateServiceFields(service, request, redirectAttributes);

    }
}