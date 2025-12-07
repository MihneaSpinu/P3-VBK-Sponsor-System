package p3project.functions;

import static p3project.functions.userFunction.*;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

import p3project.repositories.ContractRepository;
import p3project.repositories.ServiceRepository;
import p3project.repositories.SponsorRepository;

public class sponsorFunction {
    @Autowired
    private SponsorRepository sponsorRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    public static String sponsorPageLogic(HttpServletRequest request, Model model) {
        if (!userHasValidToken(request))
            return "redirect:/login";
        if (!userIsAdmin(request))
            return "redirect:/homepage";

        model.addAttribute("sponsors", sponsorRepository.findAll());
        model.addAttribute("contracts", contractRepository.findAll());
        model.addAttribute("services", serviceRepository.findAll());
        return "sponsors";
    }
}