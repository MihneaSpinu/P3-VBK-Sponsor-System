package p3project.functions;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Contract;
import p3project.classes.Eventlog;
import p3project.classes.Service;
import p3project.classes.Sponsor;
import p3project.classes.User;
import p3project.repositories.ContractRepository;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;
import p3project.repositories.SponsorRepository;


@Component
public class SponsorFunctions {
    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private EventlogFunctions eventlogFunctions;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ContractFunctions contractFunctions;

    @Autowired
    private UserFunctions userFunctions;

    //Wrappers
    public boolean contractIsActive(Contract contract) {  
        return contractFunctions.contractIsActive(contract);
    }

    public User getUserFromToken(HttpServletRequest request) throws RuntimeException {
        return userFunctions.getUserFromToken(request);
    }
    

    private <T> String handleUpdateRequest(T requestObject, T storedObject, HttpServletRequest request, RedirectAttributes redirectAttributes){
        return eventlogFunctions.handleUpdateRequest(requestObject, storedObject, request, redirectAttributes);
    }

    public String updateSponsor(Sponsor sponsor, HttpServletRequest request, RedirectAttributes redirectAttributes){

        if (!sponsorIsValid(sponsor)){
            redirectAttributes.addFlashAttribute("responseMessage", "Sponsor is invalid");
            return "redirect:/sponsors";
        }

        Sponsor storedSponsor = sponsorRepository.findById(sponsor.getId()).orElse(null);

        if(storedSponsor == null) {
            redirectAttributes.addFlashAttribute("responseMessage", "Intern serverfejl, prøv igen");
            return "redirect:/sponsors";
        }

        return handleUpdateRequest(sponsor, storedSponsor, request, redirectAttributes);
    }
    
    public boolean sponsorIsValid(Sponsor sponsor) {
        if(sponsor.getName() == null || sponsor.getName().isEmpty()) return false;

        if(!sponsor.getPhoneNumber().equals("") && !sponsor.getPhoneNumber().matches("[\\+\\-0-9]*")) return false;

        if(!sponsor.getCvrNumber().equals("") && sponsor.getCvrNumber().length() != 8) return false;
    
        if(!sponsor.getCvrNumber().equals("") && !sponsor.getCvrNumber().matches("[0-9]*")) return false;
        return true;
    }

    
    public void updateActiveFields() {
        List<Contract> contracts = contractRepository.findAll();
        List<Sponsor> sponsors = sponsorRepository.findAll();

        for (Sponsor sponsor : sponsors) {

            boolean sponsorActive = false; // default

            for (Contract contract : contracts) {
                if (sponsor.getId().equals(contract.getSponsorId()) && contractIsActive(contract)) {
                    sponsorActive = true; // found an active one
                    break;                // stop checking further contracts
                }
            }

            sponsor.setActive(sponsorActive);
            sponsorRepository.save(sponsor);
        }
    }

    public String addSponsor(@ModelAttribute Sponsor sponsor, HttpServletRequest request, RedirectAttributes redirectAttributes){
        if (!sponsorIsValid(sponsor)){
            redirectAttributes.addFlashAttribute("responseMessage", "Sponsor is invalid");
            return "redirect:/sponsors";
        }
        
        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, sponsor, "Oprettede");
        logRepository.save(log);
        
        sponsorRepository.save(sponsor);
        
        redirectAttributes.addFlashAttribute("responseMessage", "tilføjet sponsor: " + sponsor.getName());
        return "redirect:/sponsors";
    }


    public String deleteSponsor(@RequestParam Long sponsorId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Sponsor sponsor = sponsorRepository.findById(sponsorId).orElse(null);
        if(sponsor == null) {
            redirectAttributes.addFlashAttribute("responseMessage", "Intern serverfejl, prøv igen");
            return "redirect:/sponsors";
        }

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, sponsor, "Slettede");
        logRepository.save(log);

        sponsorRepository.deleteById(sponsorId);
        
        Iterable<Contract> contracts = contractRepository.findAll();
        Iterable<Service> services = serviceRepository.findAll();
        for (Contract contract : contracts) {
            if (sponsorId.equals(contract.getSponsorId())) {    
                for (Service service : services) {
                    if (contract.getId().equals(service.getContractId())) {
                        serviceRepository.deleteById(service.getId());
                    }
                }
                contractRepository.deleteById(contract.getId());
            }
        }
        return "redirect:/sponsors";
    }
}

