package p3project;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

  
    // Displays the sponsors page with lists of sponsors and contracts
    @GetMapping("/sponsors")
    public String showSponsors(Model model, HttpServletRequest request) {
        if(userHasValidToken(request)) {
            User user = getUserFromToken(request);
            if(user.getIsAdmin()) {
                model.addAttribute("sponsors", sponsorRepository.findAll());
                model.addAttribute("contracts", contractRepository.findAll());
                model.addAttribute("services", serviceRepository.findAll());
                return "sponsors";
            } else {
                return "redirect:/homepage";
            }
        }
        return "redirect:/login";
    }

    
    // Changelog page
    @GetMapping("/changelog")
    public String changelogPage(Model model, HttpServletRequest request) {
        if(userHasValidToken(request)) {
            List<Eventlog> logs = logRepository.findAll();
            Collections.reverse(logs);
            model.addAttribute("changelogs", logs);
            return "changelog";
        }
        return "redirect:/login";
    }

    // boilerplate update handlers
    @PostMapping("/update/sponsor")
    public ResponseEntity<String> updateSponsorFields(@ModelAttribute Sponsor sponsor, HttpServletRequest request) {
        if(userHasValidToken(request)) {
            User user = getUserFromToken(request);
            if(user.getIsAdmin()) {
                Sponsor storedSponsor = sponsorRepository.findById(sponsor.getId())
                .orElseThrow(() -> new RuntimeException("Unable to retrieve sponsor with id: " + sponsor.getId()));
                return handleUpdateRequest(sponsor, storedSponsor, request);
            }
        }
        return new ResponseEntity<>("FEJL!!", HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/update/contract")
    public ResponseEntity<String> updateContractFields(@ModelAttribute Contract contract, @RequestParam MultipartFile pdffile, HttpServletRequest request) { // pdfdata dead on arrival
        if(userHasValidToken(request)) {
            User user = getUserFromToken(request);
            if(user.getIsAdmin()) {
                Contract storedContract = contractRepository.findById(contract.getId())
                .orElseThrow(() -> new RuntimeException("Unable to retrieve contract with id: " + contract.getId()));
                // if(pdffile.isEmpty()) contract.setPdfData(storedContract.getPdfData()); <-- SKAL FIKSES!!!!
                parseContract(contract, pdffile);
                return handleUpdateRequest(contract, storedContract, request);
            }
        }
        return new ResponseEntity<>("FEJL!!", HttpStatus.UNAUTHORIZED);
    }

     
    @PostMapping("/update/service")
    public ResponseEntity<String> updateServiceFields(@ModelAttribute Service service, HttpServletRequest request) {
        if(userHasValidToken(request)) {
            User user = getUserFromToken(request);
            if(user.getIsAdmin()) {
                Service storedService = serviceRepository.findById(service.getId())
                .orElseThrow(() -> new RuntimeException("Unable to retrieve service with id: " + service.getId()));
                return handleUpdateRequest(service, storedService, request);
            }
        }
        return new ResponseEntity<>("FEJL!!", HttpStatus.UNAUTHORIZED);
    }
    

    private <T> ResponseEntity<String> handleUpdateRequest(T requestObject, T storedObject, HttpServletRequest request) {
        Integer fieldsChanged;
        try {
            fieldsChanged = compareFields(requestObject, storedObject, request);
        } catch (ClassNotFoundException error) {
            return new ResponseEntity<>("Internal server error: " + error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(fieldsChanged.toString(), HttpStatus.OK);
    }


    private <T> Integer compareFields(T requestObject, T storedObject, HttpServletRequest request) throws ClassNotFoundException {

        Integer fieldsChanged = 0;
        Field[] fields = requestObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object before = field.get(storedObject);
                Object after = field.get(requestObject);
                if (!Objects.equals(before, after) && fieldShouldBeEvaluated(field)) {
                    User user = getUserFromToken(request);
                    Changelog log = new Changelog(user, requestObject, field, before, after);
                    logRepository.save(log);
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

    private boolean fieldShouldBeEvaluated(Field field) {
        String fieldName = field.getName();
        switch(fieldName) {
            case "pdfData":
            case "mimeType":
                return false;
            default:
                return true;
        }
    }

    // Handles adding a new sponsor from the web form
    @PostMapping("/sponsors/add")
    public String addSponsorFromWeb(@ModelAttribute Sponsor sponsor, Model model, HttpServletRequest request) {
        // Valider telefonnummer: kun cifre er tilladt
        if(userHasValidToken(request)) {
            User user = getUserFromToken(request);
            if(user.getIsAdmin()) {
                Eventlog log = new Eventlog(user, sponsor, "CREATED");
                logRepository.save(log);
                sponsorRepository.save(sponsor);
                return "redirect:/sponsors";
            }
            return "redirect:/homepage";
        }
        return "redirect:/login";
    }

    // Handles creating a new contract for a sponsor
    @PostMapping("/sponsors/addContract")
    public String addContractForSponsor(@ModelAttribute Contract contract, @RequestParam MultipartFile pdffile, Model model, HttpServletRequest request) {
        if(userHasValidToken(request)) {
            User user = getUserFromToken(request);
            if(user.getIsAdmin()) {
                try {
                    parseContract(contract, pdffile);
                    contractRepository.save(contract);
                    return "redirect:/sponsors"; // Post/Redirect/Get for at undgå double submit
                } catch (IllegalArgumentException ex) {
                // Håndterer fejl og viser siden igen med fejlbesked
                    model.addAttribute("error", ex.getMessage());
                    model.addAttribute("sponsors", sponsorRepository.findAll());
                    model.addAttribute("contracts", contractRepository.findAll());
                    return "sponsors"; // Geninlæs siden og vis sponsors-siden med fejlbesked
                }
            }
            return "redirect:/homepage";
        }
        return "redirect:/login";

    }

    // Handles creating a new service for a contract
    @PostMapping("/sponsors/addService")
    public String addServiceForContract(@ModelAttribute Service service, Model model, HttpServletRequest request) {
        if(userHasValidToken(request)) {
            User user = getUserFromToken(request);
            if(user.getIsAdmin()) {
                try {
                    serviceRepository.save(service);
                    return "redirect:/sponsors";
                } catch (IllegalArgumentException ex) {
                    model.addAttribute("error", "Invalid data for service: " + ex.getMessage());
                    model.addAttribute("sponsors", sponsorRepository.findAll());
                    model.addAttribute("contracts", contractRepository.findAll());
                    model.addAttribute("services", serviceRepository.findAll());
                    return "sponsors";
                }
            }
            return "redirect:/homepage";
        }
        return "redirect:/login";
    }

    // Deletes a service by ID
    @PostMapping("/sponsors/deleteService")
    public String deleteService(@RequestParam Long serviceId, HttpServletRequest request) {
        if(userHasValidToken(request)) {
            User user = getUserFromToken(request);
            if(user.getIsAdmin()) {
                serviceRepository.deleteById(serviceId);
                return "redirect:/sponsors";
            }
            return "redirect:/homepage";
        }
        return "redirect:/login";
    }


    // Deletes a sponsor and all contracts linked to that sponsor
    @PostMapping("/sponsors/delete")
    public String deleteSponsor(@RequestParam Long sponsorId, HttpServletRequest request) {
        if(userHasValidToken(request)) {
            User user = getUserFromToken(request);
            if(user.getIsAdmin()) {
                sponsorRepository.deleteById(sponsorId);
                Iterable<Contract> contracts = contractRepository.findAll();
                for (Contract contract : contracts) {
                    if (sponsorId.equals(contract.getSponsorId())) {
                        contractRepository.deleteById(contract.getId());
                    }
                }
                return "redirect:/sponsors";
            }
            return "redirect:/homepage";
        }
        return "redirect:/login";
    }


    // Deletes a contract by ID
    @PostMapping("/sponsors/deleteContract")
    public String deleteContract(@RequestParam Long contractId, HttpServletRequest request) {
        if(userHasValidToken(request)) {
            User user = getUserFromToken(request);
            if(user.getIsAdmin()) {
                contractRepository.deleteById(contractId);
                return "redirect:/sponsors";
            }
            return "redirect:/homepage";
        }
        return "redirect:/login";
    }


    @GetMapping("/getFile/{contractId}")
    public ResponseEntity<byte[]> getFile(@PathVariable long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("/getFile, Contract not found"));
        byte[] pdfData = contract.getPdfData();
        String mime = contract.getMimeType() != null
            ? contract.getMimeType()
            : "application/octet-stream";
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + contract.getFileName() + "\""
            )
            .contentType(MediaType.parseMediaType(mime))
            .body(pdfData);

    }


    private void parseContract(Contract contract, MultipartFile pdffile) throws RuntimeException {
        try {   
            contract.setPdfData(pdffile.getBytes());
            String cleanFilename = Paths.get(pdffile.getOriginalFilename()).getFileName().toString(); 
            contract.setFileName(cleanFilename);
            contract.setMimeType(pdffile.getContentType());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Kunne ikke uploade kontrakt. Prøv venligst igen :)");
        }
    }


    @GetMapping("/archive")
    public String showArchivePage(Model model, HttpServletRequest request) {
        if (userHasValidToken(request)) {
            return "archive";
        }
        return "redirect:/login";
    }


    @GetMapping("/AdminPanel")
    public String showAdminPanelPage(Model model, HttpServletRequest request) {
        if (userHasValidToken(request)) {
            User user = getUserFromToken(request);
            if(user.getIsAdmin()) {
                return "AdminPanel";
            }
            return "redirect:/homepage";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        if (userHasValidToken(request)) {
            return "redirect:/homepage"; // må ikke logge ind igen
        }        
        return "login";
    }


    @PostMapping("/login/confirm")
    public String confirmLogin(@RequestParam String username, @RequestParam String hashedPassword, @RequestParam boolean rememberMe, Model model, HttpServletResponse response) {
        User user = userRepository.findByName(username);
        if(hashedPassword.equals(user.getPassword())) {
            String id = user.getId().toString();
            Token token = Token.sign(id);
            String formattedToken = id + "." + token.getHash();
            String encodedToken = URLEncoder.encode(formattedToken, StandardCharsets.UTF_8);
            int time = rememberMe ? (60 * 60 * 24 * 365) : (60 * 60 * 24); // 1 år vs 1 dag
            ResponseCookie cookie = ResponseCookie.from("token", encodedToken)
            .httpOnly(true)  // javascript kan ikke røre den B-)
            .secure(false)    // HTTPS only
            .path("/")       // Bruges til alle sider
            .maxAge(time)
            .build();
            response.addHeader("Set-Cookie", cookie.toString());
            return "redirect:/homepage";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/logout") 
    public String logout(HttpServletRequest request) {
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
    

    private String[] parseCookie(HttpServletRequest request) throws RuntimeException{
        Cookie cookie = WebUtils.getCookie(request, "token");
        if (cookie == null) throw new RuntimeException();
        
        String decodedToken = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);        
        String[] splitCookie = decodedToken.split("\\.");
        if(splitCookie.length != 2) throw new RuntimeException();


        return splitCookie;
    }

    private User getUserFromToken(HttpServletRequest request) throws RuntimeException {
        String[] parsedCookie = parseCookie(request);
        Integer userId = Integer.valueOf(parsedCookie[0]);
        User user = userRepository.findById((userId))
        .orElseThrow(() -> new RuntimeException("Unable to retrieve username"));
        return user;
    }


    
    @GetMapping("/homepage")
    public String showhomepage(Model model, HttpServletRequest request) {
        if(userHasValidToken(request)) {
            Iterable<Sponsor> sponsors = sponsorRepository.findAll();
            model.addAttribute("sponsors", sponsors);
            return "homepage";
        } else {
            return "redirect:/login";
        }
    }

    // Add user with demo sponsor & contract
    @PostMapping("/users/add")
    public String addUserFromWeb(@RequestParam String name, @RequestParam String password) {
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        userRepository.save(user);

        return "redirect:/users";
    }


    //    public String addUserFromWeb(@RequestParam String name, @RequestParam String password, Model model) {
    //     List<User> users = userRepository.findAll();
    //     for(User user : users) {
    //         if(name.equals(user.getName())) {
    //             model.addAttribute("errorMessage", "Username already exists");
    //             return "redirect:/AdminPanel";
    //         }
    //     }
    //     User user = new User();
    //     user.setName(name);
    //     user.setPassword(password);



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