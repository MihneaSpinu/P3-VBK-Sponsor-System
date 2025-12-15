package p3project.functions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Contract;
import p3project.classes.Service;
import p3project.classes.Sponsor;
import p3project.repositories.ContractRepository;
import p3project.repositories.ServiceRepository;
import p3project.repositories.SponsorRepository;


@Component
public class ArchiveFunctions {
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private SponsorFunctions sponsorFunctions;

    @Autowired
    private UserFunctions userFunctions;

    //Wrappers
    private boolean userHasValidToken(HttpServletRequest request) {
        return userFunctions.userHasValidToken(request);
    }

    private void updateActiveFields(){
        sponsorFunctions.updateActiveFields();
    }

    private boolean userIsAdmin(HttpServletRequest request) {
        return userFunctions.userIsAdmin(request);
    }

     public String showArchivePage(Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        boolean userIsAdmin = userIsAdmin(request);
        model.addAttribute("userIsAdmin", userIsAdmin);

        List<Sponsor> sponsors = sponsorRepository.findAll();
        List<Sponsor> archivedSponsors = new ArrayList<>();
        updateActiveFields();
        for(Sponsor sponsor : sponsors) {
            if(!sponsor.getActive()) {
                archivedSponsors.add(sponsor);
            }
        }

        Iterable<Contract> contracts = contractRepository.findAll();
        Iterable<Service> services = serviceRepository.findAll();

        model.addAttribute("sponsors", archivedSponsors);
        model.addAttribute("contracts", contracts);
        model.addAttribute("services", services);
        
        return "archive";
    }   
}
