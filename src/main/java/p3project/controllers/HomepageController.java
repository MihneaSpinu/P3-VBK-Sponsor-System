package p3project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import p3project.functions.ContractFunctions;
import p3project.functions.HomepageFunctions;
import p3project.functions.UserFunctions;
import p3project.repositories.ContractRepository;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;
import p3project.repositories.SponsorRepository;



@Controller
public class HomepageController {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private UserFunctions userFunctions;

    @Autowired
    private ContractFunctions contractFunctions;

    @Autowired
    private HomepageFunctions homepageFunctions;


    //Wrappers
    
    public String showhomepage(Model model, HttpServletRequest request) {
        return homepageFunctions.showhomepage(model, request);
    }
    
    
    @GetMapping("/homepage")
    public String showhomepageMapping(Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        
        return showhomepage(model, request);
    }
    
    private boolean userHasValidToken(HttpServletRequest request) {
        return userFunctions.userHasValidToken(request);
    }
}
