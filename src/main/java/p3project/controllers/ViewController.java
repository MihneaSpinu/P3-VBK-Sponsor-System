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
    private UserFunctions uF;

    @Autowired 
    ViewFunctions vF;

    
    @GetMapping("/AdminPanel")
    public String showAdminPanelPageMapping(Model model, HttpServletRequest request) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        if(!uF.userIsAdmin(request))       return "redirect:/homepage";
        return vF.showAdminPanelPage(model, request);
    }


    @GetMapping("/archive")
    public String showArchivePageMapping(Model model, HttpServletRequest request) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        return vF.showArchivePage(model, request);
    }


    @GetMapping("/changelog")
    public String changelogPageMapping(Model model, HttpServletRequest request) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        if(!uF.userIsAdmin(request))       return "redirect:/homepage";

        return vF.changelogPage(model, request);
    }

    
    @GetMapping("/homepage")
    public String showhomepageMapping(Model model, HttpServletRequest request) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        
        return vF.showhomepage(model, request);
    }

    @GetMapping("/sponsors")
    public String showSponsors(Model model, HttpServletRequest request) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        if(!uF.userIsAdmin(request))       return "redirect:/homepage";
        return vF.returnSponsorPage(model);
    }

    @GetMapping("/") 
    public String defaultMapping(HttpServletRequest request) {
        if(uF.userHasValidToken(request)) {
            return "redirect:/homepage";
        }
        return "redirect:/login";
    }
}
