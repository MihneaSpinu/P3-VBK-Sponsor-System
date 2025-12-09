package p3project;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import p3project.classes.Changelog;
import p3project.classes.Contract;
import p3project.classes.Eventlog;
import p3project.classes.Service;
import p3project.classes.Sponsor;
import p3project.classes.Token;
import p3project.classes.User;
import p3project.repositories.ContractRepository;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;
import p3project.repositories.SponsorRepository;
import p3project.repositories.UserRepository;

@Controller
public class MainController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private SponsorRepository sponsorRepository;
    @Autowired
    private ServiceRepository serviceRepository;


    // ==========================
    // Web page endpoints (HTML)
    // ==========================
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }


    @GetMapping("/sponsors")
    public String showSponsors(Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        model.addAttribute("sponsors", sponsorRepository.findAll());
        model.addAttribute("contracts", contractRepository.findAll());
        model.addAttribute("services", serviceRepository.findAll());
        return "sponsors";
    }

    private boolean userIsAdmin(HttpServletRequest request) {
        User user = getUserFromToken(request);
        return user.getIsAdmin();
    }

    
    // Changelog page
    @GetMapping("/changelog")
    public String changelogPage(Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        // make newest logs appear first
        List<Eventlog> logs = logRepository.findAll();
        Collections.reverse(logs);
        model.addAttribute("changelogs", logs);
        return "changelog";
    }

    // boilerplate update handlers
    @PostMapping("/update/sponsor")
    public String updateSponsorFields(@ModelAttribute Sponsor sponsor, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        Sponsor storedSponsor = sponsorRepository.findById(sponsor.getId()).orElse(null);
        if(storedSponsor == null) {
            redirectAttributes.addAttribute("responseMessage", "FEJL!! PRØV IGEN");
            return "redirect:/sponsors";
        }

        return handleUpdateRequest(sponsor, storedSponsor, request, redirectAttributes);
    }

    @PostMapping("/update/contract")
    public String updateContractFields(@ModelAttribute Contract contract, @RequestParam MultipartFile pdffile, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        Contract storedContract = contractRepository.findById(contract.getId()).orElse(null);
        if(storedContract == null) {
            redirectAttributes.addAttribute("responseMessage", "FEJL!! PRØV IGEN");
            return "redirect:/sponsors";
        }
        //If a file is sent parse the data
        if(!pdffile.isEmpty()){
            parseContract(contract, pdffile);
        } else {
            //if no pdf sent, get the stored values.
            contract.setPdfData(storedContract.getPdfData());
            contract.setFileName(storedContract.getFileName());
        }
        return handleUpdateRequest(contract, storedContract, request, redirectAttributes);
    }

     
    @PostMapping("/update/service")
    public String updateServiceFields(@ModelAttribute Service service, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        Service storedService = serviceRepository.findById(service.getId()).orElse(null);
        if(storedService == null) {
            redirectAttributes.addAttribute("responseMessage", "FEJL!! PRØV IGEN");
            return "redirect:/sponsors";
        }
        return handleUpdateRequest(service, storedService, request, redirectAttributes);

    }


    private <T> String handleUpdateRequest(T requestObject, T storedObject, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Integer fieldsChanged;
        try {
            fieldsChanged = compareFields(requestObject, storedObject, request);
            redirectAttributes.addAttribute("repsonseMessage", "Opdateret " + fieldsChanged + " felter");
            return "redirect:/sponsors";
        } catch (ClassNotFoundException error) {
            redirectAttributes.addAttribute("responseMessage","FEJL!! PRØ GEN");
            return "redirect:/sponsors";
        }
    }


    private <T> Integer compareFields(T requestObject, T storedObject, HttpServletRequest request) throws ClassNotFoundException {

        Integer fieldsChanged = 0;
        Field[] fields = requestObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object before = field.get(storedObject);
                Object after = field.get(requestObject);
                if (!Objects.equals(before, after)) {
                    if(fieldShouldBeLogged(field)) {
                        User user = getUserFromToken(request);
                        Changelog log = new Changelog(user, requestObject, field, before, after);
                        logRepository.save(log);
                        fieldsChanged++;
                    }
                    field.set(storedObject, after);
                }
            } catch (IllegalAccessException error) {
                throw new RuntimeException(error);
            }
        }

        if (requestObject instanceof Sponsor)         sponsorRepository.save((Sponsor) storedObject);
        else if (requestObject instanceof Contract)   contractRepository.save((Contract) storedObject);
        else if (requestObject instanceof Service)    serviceRepository.save((Service) storedObject);
        else throw new ClassNotFoundException();

        return fieldsChanged;
    }

    private boolean fieldShouldBeLogged(Field field) { // jank
        String fieldName = field.getName();
        switch(fieldName) {
            case "pdfData":
                return false;
            default:
                return true;
        }
    }

    @PostMapping("/sponsors/add")
    public String addSponsorFromWeb(@ModelAttribute Sponsor sponsor, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, sponsor, "CREATED");
        logRepository.save(log);

        sponsorRepository.save(sponsor);

        redirectAttributes.addAttribute("responseMessage", "tilføjet sponsor:");
        return "redirect:/sponsors";
    }


    // Handles creating a new contract for a sponsor
    @PostMapping("/sponsors/addContract")
    public String addContractForSponsor(@ModelAttribute Contract contract, @RequestParam MultipartFile pdffile, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, contract, "CREATED");
        logRepository.save(log);

        try {
            parseContract(contract, pdffile);
            contractRepository.save(contract);
            redirectAttributes.addAttribute("responseMessage", "Tilføejt klntrakt:");
            return "redirect:/sponsors";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addAttribute("responseMessage","FEJL!! PRØV IOGEN");
            return "redirect:/sponsors";
        }
    }

    // Handles creating a new service for a contract
    @PostMapping("/sponsors/addService")
    public String addServiceForContract(@ModelAttribute Service service, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, service, "CREATED");
        logRepository.save(log);

        try {
            serviceRepository.save(service);
            redirectAttributes.addAttribute("responseMessage", "tilføjet service: [navn]");
            return "redirect:/sponsors";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addAttribute("responseMessage", "FEJL");
            return "redirect:/sponsors";
        }
    }


    private boolean sponsorIsValid(Sponsor sponsor) {
        if(sponsor.getName() == null || sponsor.getName().isEmpty()) return false;

        if(sponsor.getPhoneNumber().matches("[\\+\\-0-9]*")) return false;

        if(sponsor.getCvrNumber().length() != 8
        || !sponsor.getCvrNumber().matches("[0-9]*")
        ) return false;

        return true;
    }

    private boolean contractIsValid(Contract contract) {
        if(contract.getName() == null || contract.getName().isEmpty()) return false;

        if(contract.getPaymentAsInt() < 0) return false;

        LocalDate start = contract.getStartDate();
        LocalDate end = contract.getEndDate();
        if(start != null && end != null && start.isAfter(end)) return false;

        return true;
    }

    private boolean serviceIsValid(Service service) {
        if(service.getName() == null || service.getName().isEmpty()) return false;

        LocalDate start = service.getStartDate();
        LocalDate end = service.getEndDate();
        if(start != null && end != null && start.isAfter(end)) return false;

        if(service.getAmountOrDivision() < 0) return false;

        return true;
    }


    // Deletes a service by ID
    @PostMapping("/sponsors/deleteService")
    public String deleteService(@RequestParam Long serviceId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        Service service = serviceRepository.findById(serviceId).orElse(null);
        if(service == null) {
            redirectAttributes.addFlashAttribute("responseMessage", "FEJL PRØV IGEN");
            return "redirect:/sponsors";
        }

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, service, "DELETED");
        logRepository.save(log);

        serviceRepository.deleteById(serviceId);
        return "redirect:/sponsors";

    }


    // Deletes a sponsor and all contracts linked to that sponsor
    @PostMapping("/sponsors/delete")
    public String deleteSponsor(@RequestParam Long sponsorId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        Sponsor sponsor = sponsorRepository.findById(sponsorId).orElse(null);
        if(sponsor == null) {
            redirectAttributes.addFlashAttribute("responseMessage", "FEJL PRØV IGEN");
            return "redirect:/sponsors";
        }

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, sponsor, "DELETED");
        logRepository.save(log);

        sponsorRepository.deleteById(sponsorId);
        
        Iterable<Contract> contracts = contractRepository.findAll();
        for (Contract contract : contracts) {
            if (sponsorId.equals(contract.getSponsorId())) {
                // SLET OGSÅ TILHØRENDE SERVICES TIL KONTRAKTERNE, SÆT I FUNKTION MÅSKE
                contractRepository.deleteById(contract.getId());
            }
        }
        return "redirect:/sponsors";
    }


    // Deletes a contract by ID
    @PostMapping("/sponsors/deleteContract")
    public String deleteContract(@RequestParam Long contractId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        Contract contract = contractRepository.findById(contractId).orElse(null);
        if(contract == null) {
            redirectAttributes.addFlashAttribute("responseMessage", "FEJL PRØV IGEN");
            return "redirect:/sponsors";
        }

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, contract, "DELETED");
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


    @GetMapping("/getFile/{contractId}")
    public ResponseEntity<byte[]> getFile(@PathVariable long contractId) {

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


    private void parseContract(Contract contract, MultipartFile pdffile) throws RuntimeException {
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


    @GetMapping("/archive")
    public String showArchivePage(Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        return "archive";
    }


    @GetMapping("/AdminPanel")
    public String showAdminPanelPage(Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";
        model.addAttribute("users", userRepository.findAll());
        return "AdminPanel";
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        if (userHasValidToken(request)) {
            return "redirect:/homepage";
        }        
        return "login";
    }


    @PostMapping("/login/confirm")
    public String confirmLogin(@RequestParam String username, @RequestParam String password, @RequestParam boolean rememberMe, Model model, HttpServletResponse response) {
        User user = userRepository.findByName(username);
        if(user == null) return "redirect:/login";

        if(BCrypt.checkpw(password, user.getPassword())) {
            String id = user.getId().toString();
            Token token = Token.sign(id);
            String formattedToken = id + "." + token.getHash();
            String encodedToken = URLEncoder.encode(formattedToken, StandardCharsets.UTF_8);
            int time = rememberMe ? (60 * 60 * 24 * 365) : (60 * 60 * 24); // 1 år vs 1 dag
            ResponseCookie cookie = ResponseCookie.from("token", encodedToken)
                .httpOnly(true)  // noget med javascript
                .secure(false)    // HTTPS only
                .path("/")       // Bruges til alle sider i domænet
                .maxAge(time)
                .build();
            response.addHeader("Set-Cookie", cookie.toString());
            return "redirect:/homepage";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/logout") 
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, "token");
        if(cookie != null) {
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        return "redirect:/login";
    }

    private boolean userHasValidToken(HttpServletRequest request) {
        String cookieId;
        String cookieHash;
        try {
            String[] parsedCookie = parseCookie(request);
            cookieId = parsedCookie[0];
            cookieHash = parsedCookie[1];
        } catch(RuntimeException e) {
            return false;
        }
        Token token = Token.sign(cookieId);
        return token.verify(cookieHash);
    }   
    

    private String[] parseCookie(HttpServletRequest request) throws RuntimeException{ // find anden exception?
        Cookie cookie = WebUtils.getCookie(request, "token");
        if (cookie == null) throw new RuntimeException();
        
        String decodedToken = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);        
        String[] splitCookie = decodedToken.split("\\.");
        if(splitCookie.length != 2) throw new RuntimeException();

        return splitCookie;
    }

    private User getUserFromToken(HttpServletRequest request) throws RuntimeException {
        String[] parsedCookie = parseCookie(request);
        Long userId = Long.valueOf(parsedCookie[0]);
        User user = userRepository.findById((userId)).orElse(null);
        if(user == null) throw new RuntimeException("Unable to retrieve username");
        return user;
    }


    /* 
    private boolean isSponsorActive(Sponsor sponsor) {


    }

    private boolean isContractActive(Contract contract) {
        Iterable<Service> services = serviceRepository.findAll();
        if()

    }

    private boolean isServiceActive(Service service) {
        return service.getArchived() || LocalDate.now().isAfter(service.getEndDate());
    }
    */

    
    @GetMapping("/homepage")
    public String showhomepage(Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";

        Iterable<Sponsor> sponsors = sponsorRepository.findAll();
        Iterable<Contract> contracts = contractRepository.findAll();
        Iterable<Service> services = serviceRepository.findAll();
        model.addAttribute("sponsors", sponsors);
        model.addAttribute("contracts", contracts);
        model.addAttribute("services", services);
        return "homepage";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUserById(@PathVariable Long id){
        userRepository.deleteById(id);
        return "redirect:/AdminPanel";

    }

    @PostMapping("/users/add")
    public String addUserFromWeb(@RequestParam String name, @RequestParam String password, boolean isAdmin, Model model, RedirectAttributes redirectAttributes) {
        List<User> users = userRepository.findAll();
        for(User user : users) {
            if(name.equals(user.getName())) {
                redirectAttributes.addFlashAttribute("responseMessage", "Brugernavn allerede i brug, vælg et andet");
                return "redirect:/AdminPanel";
            }
        }
   
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(8));
        User user = new User();
        user.setName(name);
        user.setPassword(hashedPassword);
        user.setIsAdmin(isAdmin);
        userRepository.save(user);

        return "redirect:/AdminPanel";
    }


    @GetMapping("/testuser")
    public String testUser(HttpServletResponse response) {
        User user = new User();
        user.setName("søren");

        String password = "test123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(8));

        user.setPassword(hashedPassword);
        userRepository.save(user);

        return "redirect:/login";
    }
}