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
    public String updateSponsorFields(@ModelAttribute Sponsor sponsor, HttpServletRequest request, Model model) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        Sponsor storedSponsor = sponsorRepository.findById(sponsor.getId()).orElse(null);
        if(storedSponsor == null) return renderSponsorPageWithResponse("Error retrieving sponsor. Please try again", model);

        return handleUpdateRequest(sponsor, storedSponsor, request, model);
    }

    @PostMapping("/update/contract")
    public String updateContractFields(@ModelAttribute Contract contract, @RequestParam MultipartFile pdffile, HttpServletRequest request, Model model) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        Contract storedContract = contractRepository.findById(contract.getId()).orElse(null);
        if(storedContract == null) return renderSponsorPageWithResponse("Error retrieving contract. Please try again", model);
        //If a file is sent parse the data
        if(!pdffile.isEmpty()){
            parseContract(contract, pdffile);
        } else {
            //if no pdf sent, get the stored values.
            contract.setPdfData(storedContract.getPdfData());
            contract.setFileName(storedContract.getFileName());
        }
        return handleUpdateRequest(contract, storedContract, request, model);
    }

     
    @PostMapping("/update/service")
    public String updateServiceFields(@ModelAttribute Service service, HttpServletRequest request, Model model) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        Service storedService = serviceRepository.findById(service.getId()).orElse(null);
        if(storedService == null) return renderSponsorPageWithResponse("Error retrieving service. Please try again", model);
        return handleUpdateRequest(service, storedService, request, model);

    }

    private String renderSponsorPageWithResponse(String responseMessage, Model model) { // tilføj flag til respons type?
        model.addAttribute("sponsors", sponsorRepository.findAll());
        model.addAttribute("contracts", contractRepository.findAll());
        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("responseMessage", responseMessage);
        return "sponsors";
    }
    

    private <T> String handleUpdateRequest(T requestObject, T storedObject, HttpServletRequest request, Model model) {
        Integer fieldsChanged;
        try {
            fieldsChanged = compareFields(requestObject, storedObject, request);
            return renderSponsorPageWithResponse("Updated " + fieldsChanged + " fields", model);
        } catch (ClassNotFoundException error) {
            return renderSponsorPageWithResponse("Internal server error. Please try again", model);
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
                    }
                    field.set(storedObject, after);
                    fieldsChanged++;
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
    public String addSponsorFromWeb(@ModelAttribute Sponsor sponsor, Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, sponsor, "CREATED");
        logRepository.save(log);

        sponsorRepository.save(sponsor);

        return "redirect:/sponsors";
    }


    // Handles creating a new contract for a sponsor
    @PostMapping("/sponsors/addContract")
    public String addContractForSponsor(@ModelAttribute Contract contract, @RequestParam MultipartFile pdffile, Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, contract, "CREATED");
        logRepository.save(log);

        try {
            parseContract(contract, pdffile);
            contractRepository.save(contract);
            return "redirect:/sponsors";
        } catch (IllegalArgumentException ex) {
            return renderSponsorPageWithResponse("FEJL!!!", model);
        }
    }

    // Handles creating a new service for a contract
    @PostMapping("/sponsors/addService")
    public String addServiceForContract(@ModelAttribute Service service, Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, service, "CREATED");
        logRepository.save(log);

        try {
            serviceRepository.save(service);
            return "redirect:/sponsors";
        } catch (IllegalArgumentException ex) {
            return renderSponsorPageWithResponse("FEJL!!!", model);
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
    public String deleteService(@RequestParam Long serviceId, HttpServletRequest request, Model model) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        Service service = serviceRepository.findById(serviceId).orElse(null);
        if(service == null) return renderSponsorPageWithResponse("Invalid id", model);

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, service, "DELETED");
        logRepository.save(log);

        serviceRepository.deleteById(serviceId);
        return "redirect:/sponsors";

    }


    // Deletes a sponsor and all contracts linked to that sponsor
    @PostMapping("/sponsors/delete")
    public String deleteSponsor(@RequestParam Long sponsorId, HttpServletRequest request, Model model) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        Sponsor sponsor = sponsorRepository.findById(sponsorId).orElse(null);
        if(sponsor == null) return renderSponsorPageWithResponse("Invalid id", model);

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, sponsor, "DELETED");
        logRepository.save(log);

        sponsorRepository.deleteById(sponsorId);
        Iterable<Contract> contracts = contractRepository.findAll();
        for (Contract contract : contracts) {
            if (sponsorId.equals(contract.getSponsorId())) {
                contractRepository.deleteById(contract.getId());
            }
        }
        return "redirect:/sponsors";
    }


    // Deletes a contract by ID
    @PostMapping("/sponsors/deleteContract")
    public String deleteContract(@RequestParam Long contractId, HttpServletRequest request, Model model) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        Contract contract = contractRepository.findById(contractId).orElse(null);
        if(contract == null) return renderSponsorPageWithResponse("Invalid id", model);

        User user = getUserFromToken(request);
        Eventlog log = new Eventlog(user, contract, "DELETED");
        logRepository.save(log);
        
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
        
        return "AdminPanel";
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        if (userHasValidToken(request)) {
            return "redirect:/homepage"; // må ikke logge ind igen
        }        
        return "login";
    }


    @PostMapping("/login/confirm")
    public String confirmLogin(@RequestParam String username, @RequestParam String password, @RequestParam boolean rememberMe, Model model, HttpServletResponse response) {
        User user = userRepository.findByName(username);
        System.out.println("\n\nNAVN: " + username);
        System.out.println("\n\nKODE: " + password);
        System.out.println("\n\nREMG: " + rememberMe);

        if(BCrypt.checkpw(user.getPassword(), password)) {
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
            return "login";
        } else {
            return "login";
        }
    }

    @GetMapping("/logout") 
    public String logout(HttpServletRequest request) { // ikke implementeret på frontend endnu
        Cookie cookie = WebUtils.getCookie(request, "token");
        if(cookie != null) cookie.setMaxAge(0);
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
        int userId = Integer.parseInt(parsedCookie[0]);
        User user = userRepository.findById((userId)).orElse(null);
        if(user == null) throw new RuntimeException("Unable to retrieve username");
        return user;
    }

    
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

    // Add user with demo sponsor & contract
    @PostMapping("/users/add")
    public String addUserFromWeb(@RequestParam String name, @RequestParam String password, boolean isAdmin, Model model) {
        List<User> users = userRepository.findAll();
        for(User user : users) {
            if(name.equals(user.getName())) {
                model.addAttribute("responseMessage", "Username already exists. Please choose another.");
                return "AdminPanel";
            }
        }
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setName(name);
        user.setPassword(hashedPassword);
        user.setIsAdmin(isAdmin);
        userRepository.save(user);

        return "redirect:/users";
    }


    @GetMapping("/testuser")
    public String testUser(HttpServletResponse response) {
        User user = new User();
        user.setName("søren");
        String hashedPassword = "test123";
        user.setPassword(hashedPassword);
        userRepository.save(user);
        ResponseCookie cookie = ResponseCookie.from("token", user.getId() + ".fihuayr78108hfnhfnhubr801gh89")
        .httpOnly(true)  // javascript kan ikke røre den B-)
        .secure(false)    // HTTPS only
        .path("/")       // Bruges til alle sider
        .maxAge(3600)
        .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return "redirect:/login";
    }


    /*
    @PostMapping("/update/service")
    public ResponseEntity<String> updateServiceFields(HttpServletRequest request,
            @RequestParam Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer amountOrDivision,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Service storedService = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unable to retrieve service with id: " + id));

        // Build a request Service object using stored values as defaults
        p3project.classes.Service requestService;
        try {
            p3project.classes.ServiceType st = null;
            p3project.classes.Service.ServiceStatus ss = null;
            java.time.LocalDate sd = null;
            java.time.LocalDate ed = null;

            if (type != null && !type.isEmpty()) {
                try { st = p3project.classes.ServiceType.valueOf(type); } catch (Exception e) { st = storedService.getType(); }
            } else {
                st = storedService.getType();
            }

            if (status != null && !status.isEmpty()) {
                try { ss = p3project.classes.Service.ServiceStatus.valueOf(status); } catch (Exception e) { ss = storedService.getStatusEnum(); }
            } else {
                ss = storedService.getStatusEnum();
            }

            if (startDate != null && !startDate.isEmpty()) sd = java.time.LocalDate.parse(startDate); else sd = storedService.getStartDate();
            if (endDate != null && !endDate.isEmpty()) ed = java.time.LocalDate.parse(endDate); else ed = storedService.getEndDate();

            int amt = amountOrDivision == null ? storedService.getAmountOrDivision() : amountOrDivision;
            String nm = name == null ? storedService.getName() : name;

            requestService = new p3project.classes.Service(storedService.getContractId(), nm, st, ss, amt, sd, ed);

            // set private id field via reflection so compareFields can inspect it
            java.lang.reflect.Field idField = requestService.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(requestService, id);

        } catch (Exception e) {
            return new ResponseEntity<>("Bad request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return handleUpdateRequest(requestService, storedService, request);
    }
    */
}