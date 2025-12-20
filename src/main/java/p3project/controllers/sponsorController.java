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
    private SponsorFunctions spF;

    @Autowired
    private UserFunctions uF;


    @PostMapping("/update/sponsor")
    public String updateSponsorFields(@ModelAttribute Sponsor sponsor, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        if(!uF.userIsAdmin(request))       return "redirect:/homepage";

        return spF.updateSponsor(sponsor, request, redirectAttributes);
    }

    @PostMapping("/sponsors/add")
    public String addSponsorFromWeb(@ModelAttribute Sponsor sponsor, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        if(!uF.userIsAdmin(request))       return "redirect:/homepage";

        return spF.addSponsor(sponsor, request, redirectAttributes);
    }

        // Deletes a sponsor and all contracts linked to that sponsor
    @PostMapping("/sponsors/delete")
    public String deleteSponsorMapping(@RequestParam Long sponsorId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        if(!uF.userIsAdmin(request))       return "redirect:/homepage";
        return spF.deleteSponsor(sponsorId, request, redirectAttributes);
    }
}
