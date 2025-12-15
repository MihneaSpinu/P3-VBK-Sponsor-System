package p3project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Sponsor;
import p3project.functions.SponsorFunctions;
import p3project.functions.UserFunctions;

@Controller
public class SponsorController {

    @Autowired
    private SponsorFunctions sponsorFunctions;

    @Autowired
    private UserFunctions userFunctions;

    //Wrappers
    private boolean userHasValidToken(HttpServletRequest request) {
        return userFunctions.userHasValidToken(request);
    }

    private boolean userIsAdmin(HttpServletRequest request) {
        return userFunctions.userIsAdmin(request);
    }

    public String addSponsor(@ModelAttribute Sponsor sponsor, HttpServletRequest request, RedirectAttributes redirectAttributes){
        return sponsorFunctions.addSponsor(sponsor, request, redirectAttributes);
    }

    public String updateSponsor(Sponsor sponsor, HttpServletRequest request, RedirectAttributes redirectAttributes){
        return sponsorFunctions.updateSponsor(sponsor, request, redirectAttributes);
    }


    public String deleteSponsor(@RequestParam Long sponsorId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return sponsorFunctions.deleteSponsor(sponsorId, request, redirectAttributes);
    }
    


    @PostMapping("/update/sponsor")
    public String updateSponsorFields(@ModelAttribute Sponsor sponsor, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        return updateSponsor(sponsor, request, redirectAttributes);
    }

    @PostMapping("/sponsors/add")
    public String addSponsorFromWeb(@ModelAttribute Sponsor sponsor, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        return addSponsor(sponsor, request, redirectAttributes);
    }

        // Deletes a sponsor and all contracts linked to that sponsor
    @PostMapping("/sponsors/delete")
    public String deleteSponsorMapping(@RequestParam Long sponsorId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";
        return deleteSponsor(sponsorId, request, redirectAttributes);
    }
}
