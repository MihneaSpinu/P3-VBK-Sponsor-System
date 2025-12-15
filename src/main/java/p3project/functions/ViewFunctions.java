package p3project.functions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Contract;
import p3project.classes.Eventlog;
import p3project.classes.Service;
import p3project.classes.Sponsor;
import p3project.repositories.ContractRepository;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;
import p3project.repositories.SponsorRepository;
import p3project.repositories.UserRepository;

@Component
public class ViewFunctions {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private SponsorFunctions sponsorFunctions;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private UserFunctions userFunctions;

    private void updateActiveFields(){
        sponsorFunctions.updateActiveFields();
    }

    private boolean userIsAdmin(HttpServletRequest request) {
        return userFunctions.userIsAdmin(request);
    }

    public String showAdminPanelPage(Model model, HttpServletRequest request) {
        model.addAttribute("users", userRepository.findAll());
        return "AdminPanel";
    }

    
     public String showArchivePage(Model model, HttpServletRequest request) {
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



    public String changelogPage(Model model, HttpServletRequest request) {
        List<Eventlog> logs = logRepository.findAll();
        Collections.reverse(logs);
        model.addAttribute("changelogs", logs);
        return "changelog";
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


    public String returnSponsorPage(Model model) {
        model.addAttribute("sponsors", sponsorRepository.findAll());
        model.addAttribute("contracts", contractRepository.findAll());
        model.addAttribute("services", serviceRepository.findAll());
        updateActiveFields();
        return "sponsors";
    }


}
