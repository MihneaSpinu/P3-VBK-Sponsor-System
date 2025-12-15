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
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;
import p3project.repositories.SponsorRepository;

@Component
public class HomepageFunctions {
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
    private SponsorFunctions sponsorFunctions;

    //Wrappers
    private boolean userIsAdmin(HttpServletRequest request) {
        return userFunctions.userIsAdmin(request);
    }

        
    public void updateActiveFields() {
        sponsorFunctions.updateActiveFields();
    }




    public String showhomepage(Model model, HttpServletRequest request) {

        Iterable<Sponsor> sponsors = sponsorRepository.findAll();
        Iterable<Contract> contracts = contractRepository.findAll();
        Iterable<Service> services = serviceRepository.findAll();
        boolean userIsAdmin = userIsAdmin(request);
        List<Sponsor> activeSponsors = new ArrayList<>();
        updateActiveFields();

        for(Sponsor sponsor : sponsors) {
            if(sponsor.getActive()) {
                activeSponsors.add(sponsor);
            }
        }
        

        model.addAttribute("sponsors", activeSponsors);
        model.addAttribute("contracts", contracts);
        model.addAttribute("services", services);
        model.addAttribute("userIsAdmin", userIsAdmin);
        return "homepage";
    }
}
