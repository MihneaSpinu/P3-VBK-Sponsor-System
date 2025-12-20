package p3project.functions;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Contract;
import p3project.classes.Eventlog;
import p3project.classes.Service;
import p3project.classes.User;
import p3project.repositories.ContractRepository;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;

@Component
public class ContractFunctions {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private ServiceFunctions seF;

    @Autowired
    private UserFunctions uF;

    @Autowired
    private EventlogFunctions eF;



    public boolean contractIsActive(Contract contract) {  
        List<Service> services = serviceRepository.findAll();
        for(Service service : services) {
            if(contract.getId().equals(service.getContractId())) {
                
                if(LocalDate.now().isAfter(contract.getEndDate())) {
                    service.setActive(false);
                    contractRepository.save(contract);
                    continue;
                }

                if(seF.serviceIsActive(service)) {
                    contract.setActive(true);
                    contractRepository.save(contract);
                    return true;
                }

            }
        }
        contract.setActive(false);
        contractRepository.save(contract);
        return contract.getActive();
    }

    public boolean contractIsValid(Contract contract) {
        if(contract.getName() == null || contract.getName().isEmpty()) return false;

        if(contract.getPaymentAsInt() < 0) return false;

        LocalDate start = contract.getStartDate();
        LocalDate end = contract.getEndDate();
        if(start == null || end == null) return false;
        if(start.isAfter(end)) return false;

        return true;
    }


    public void parseContract(Contract contract, MultipartFile pdffile) throws RuntimeException {
        if (pdffile.isEmpty()){
            return;
        }
        try {   
            contract.setPdfData(pdffile.getBytes());
            String cleanFilename = Paths.get(pdffile.getOriginalFilename()).getFileName().toString(); 
            contract.setFileName(cleanFilename);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Kunne ikke uploade kontrakt. Prøv venligst igen :)");
        }
    }

    public String deleteContract(@RequestParam Long contractId, HttpServletRequest request, RedirectAttributes redirectAttributes){
        Contract contract = contractRepository.findById(contractId).orElse(null);
        if(contract == null) {
            redirectAttributes.addFlashAttribute("responseMessage", "Intern serverfejl, prøv igen");
            return "redirect:/sponsors";
        }
        
        User user = uF.getUserFromToken(request);
        Eventlog log = new Eventlog(user, contract, "Slettede");
        logRepository.save(log);
        
        Iterable<Service> services = serviceRepository.findAll();
        for (Service service : services) {
            if (contractId.equals(service.getContractId())) {
                serviceRepository.deleteById(service.getId());
            }
        }
        
        contractRepository.deleteById(contractId);
        return "redirect:/sponsors";
    }

    public String addContractForSponsor(@ModelAttribute Contract contract, @RequestParam MultipartFile pdffile, HttpServletRequest request, RedirectAttributes redirectAttributes) {        
        if (!contractIsValid(contract)){
            redirectAttributes.addFlashAttribute("responseMessage", "Sponsor is invalid");
            return "redirect:/sponsors";
        }
        
        User user = uF.getUserFromToken(request);
        Eventlog log = new Eventlog(user, contract, "Oprettede");
        logRepository.save(log);
        
        try {
            parseContract(contract, pdffile);
            contractRepository.save(contract);
            redirectAttributes.addFlashAttribute("responseMessage", "Tilføjet kontrakt: " + contract.getName());
            return "redirect:/sponsors";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("responseMessage","Intern serverfejl, prøv igen");
            return "redirect:/sponsors";
        }
    }

    public String updateContractFields(@ModelAttribute Contract contract, @RequestParam MultipartFile pdffile, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (!contractIsValid(contract)){
            redirectAttributes.addFlashAttribute("responseMessage", "Sponsor is invalid");
            return "redirect:/sponsors";
        }

        Contract storedContract = contractRepository.findById(contract.getId()).orElse(null);
        if(storedContract == null) {
            redirectAttributes.addFlashAttribute("responseMessage", "Intern serverfejl, prøv igen");
            return "redirect:/sponsors";
        }
        
        if(!pdffile.isEmpty()){ //If a file is sent parse the data
            parseContract(contract, pdffile);
        } else { //if no pdf sent, get the stored values.
            contract.setPdfData(storedContract.getPdfData());
            contract.setFileName(storedContract.getFileName());
        }
        return eF.handleUpdateRequest(contract, storedContract, request, redirectAttributes);
    }

    public ResponseEntity<byte[]> getFile(long contractId) {
        Contract contract = contractRepository.findById(contractId).orElse(null);
        if(contract == null) throw new RuntimeException("/getFile, Contract not found");

        byte[] pdfData = contract.getPdfData();
        return ResponseEntity
                    .ok()
                    .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + contract.getFileName() + "\""
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfData);
    }
}