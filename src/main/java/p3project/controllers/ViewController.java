package p3project.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import p3project.functions.UserFunctions;
import p3project.functions.ViewFunctions;;

@Controller
public class ViewController {
    @Autowired
    private UserFunctions userFunctions;

    @Autowired ViewFunctions viewFunctions;

  
    private boolean userHasValidToken(HttpServletRequest request) {
        return userFunctions.userHasValidToken(request);
    }

    private boolean userIsAdmin(HttpServletRequest request) {
        return userFunctions.userIsAdmin(request);
    }

    private String showAdminPanelPage(Model model, HttpServletRequest request) {
        return viewFunctions.showAdminPanelPage(model, request);
    }

    public String showArchivePage(Model model, HttpServletRequest request) {
        return viewFunctions.showArchivePage(model, request);
    }

    public String changelogPage(Model model, HttpServletRequest request) {
        return viewFunctions.changelogPage(model, request);
    }

    public String showhomepage(Model model, HttpServletRequest request) {
        return viewFunctions.showhomepage(model, request);
    }

    private String returnSponsorPage(Model model) {
        return viewFunctions.returnSponsorPage(model);
    }
    
    @GetMapping("/AdminPanel")
    public String showAdminPanelPageMapping(Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";
        return showAdminPanelPage(model, request);
    }


    @GetMapping("/archive")
    public String showArchivePageMapping(Model model, HttpServletRequest request) {
        return showArchivePage(model, request);
    }


    @GetMapping("/changelog")
    public String changelogPageMapping(Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        return changelogPage(model, request);
    }

    
    @GetMapping("/homepage")
    public String showhomepageMapping(Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        
        return showhomepage(model, request);
    }

    @GetMapping("/sponsors")
    public String showSponsors(Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";
        return returnSponsorPage(model);
    }

    @GetMapping("/") 
    public String defaultMapping(HttpServletRequest request) {
        if(userHasValidToken(request)) {
            return "redirect:/homepage";
        }
        return "redirect:/login";
    }
}
