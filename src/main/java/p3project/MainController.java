package p3project;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.WebUtils;

import p3project.classes.Changelog;
import p3project.classes.Contract;
import p3project.classes.Eventlog;
import p3project.classes.Service;
import p3project.classes.Sponsor;
import p3project.classes.User;
import p3project.classes.Token;
import p3project.repositories.ContractRepository;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;
import p3project.repositories.SponsorRepository;
import p3project.repositories.UserRepository;

import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.net.URLDecoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
    // API endpoints (JSON)
    // ==========================
    @PostMapping(path = "/demo/add")
    public @ResponseBody String addNewUser(@RequestParam String name, @RequestParam String email) {
        User user = new User();
        user.setName(name);
        //user.setEmail(email);
        userRepository.save(user);
        return "Saved";
    }

    @GetMapping(path = "/demo/all")
    public @ResponseBody Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ==========================
    // Web page endpoints (HTML)
    // ==========================
    @GetMapping("/")
    public String home() {
        return "redirect:/users";
    }

    @GetMapping("/users")
    public String showUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());

        // ---------------------------------------------------------------
        // DETTE KODE ER TIL TEST AF EVENTLOG, SKAL FJERNES VED FÆRDIG PRODUKTION
        Sponsor sponsor = new Sponsor();
        sponsor.setName("testSponsorNavn");
        Eventlog log = new Eventlog(new User(), sponsor, "CREATED");
        logRepository.save(log);
        // ---------------------------------------------------------------

        return "users";
    }

    // Displays the sponsors page with lists of sponsors and contracts
    @GetMapping("/sponsors")
    public String showSponsors(Model model) {
        model.addAttribute("sponsors", sponsorRepository.findAll());
        model.addAttribute("contracts", contractRepository.findAll());

        Iterable<String> sponsorNames = fetchContractSponsorNames();
        model.addAttribute("sponsorNames", sponsorNames);
        return "sponsors";
    }

    private List<String> fetchContractSponsorNames() {
        Iterable<Contract> contracts = contractRepository.findAll();
        List<String> sponsorNames = new ArrayList<>();

        for(Contract contract : contracts) {
            Long sponsorId = contract.getSponsorId();
            Sponsor sponsor = sponsorRepository.findById(sponsorId).orElseThrow();
            String name = sponsor.getName();
            sponsorNames.add(name);
        }
        return sponsorNames;
    }

    // Changelog page
    @GetMapping("/changelog")
    public String changelogPage(Model model) {
        model.addAttribute("changelogs", logRepository.findAll());
        return "changelog";
    }

    // boilerplate update handlers
    @PostMapping("/update/sponsor")
    public ResponseEntity<String> updateSponsorFields(@ModelAttribute Sponsor sponsor) {
        Sponsor storedSponsor = sponsorRepository.findById(sponsor.getId())
        .orElseThrow(() -> new RuntimeException("Unable to retrieve sponsor with id: " + sponsor.getId()));
        return handleUpdateRequest(sponsor, storedSponsor);
    }

    @PostMapping("/update/contract")
    public ResponseEntity<String> updateContractFields(@ModelAttribute Contract contract) { // pdfdata dead on arrival
        Contract storedContract = contractRepository.findById(contract.getId())
        .orElseThrow(() -> new RuntimeException("Unable to retrieve sponsor with id: " + contract.getId()));
        return handleUpdateRequest(contract, storedContract);
    }

    @PostMapping("/update/service")
    public ResponseEntity<String> updateServiceFields(@ModelAttribute Service service) {
        Service storedService = serviceRepository.findById(service.getId())
        .orElseThrow(() -> new RuntimeException("Unable to retrieve sponsor with id: " + service.getId()));
        return handleUpdateRequest(service, storedService);
    }

    private <T> ResponseEntity<String> handleUpdateRequest(T requestObject, T storedObject) {
        Integer fieldsChanged;
        try {
            fieldsChanged = compareFields(requestObject, storedObject);
        } catch (ClassNotFoundException error) {
            return new ResponseEntity<>("Internal server error: " + error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(fieldsChanged.toString(), HttpStatus.OK);
    }

    // fejl håndtering tba, if(==null) etc...
    private <T> Integer compareFields(T requestObject, T storedObject) throws ClassNotFoundException {

        Integer fieldsChanged = 0;
        Field[] fields = requestObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if(field.getName().equals("pdfData")) continue; // hack
                Object before = field.get(storedObject);
                Object after = field.get(requestObject);
                if (!Objects.equals(before, after)) { // indbygget null håndtering

                    Changelog log = new Changelog(new User(), requestObject, field, before, after);
                    // handleSpecialFields(field, log, storedObject, requestObject);
                    logRepository.save(log);
                    field.set(storedObject, after);

                    fieldsChanged++;
                    // System.out.println("Updated " + field.getName() + ": " + before.toString() + " -> " + after.toString());
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

    /*
    private <T> void handleSpecialFields(Field field, Changelog log, T storedObject, T requestObject) {
        String fieldName = field.getName();
        switch(fieldName) {
            case "pdfData":
                try {
                    Method getName = storedObject.getClass().getMethod("getFileName");
                    String oldName = getName.invoke(storedObject).toString();
                    String newName = getName.invoke(requestObject).toString();
                    log.setBefore(oldName);
                    log.setAfter(newName);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException error) {
                    throw new RuntimeException("Error getting name of target object: " + error);
                }
        }
    }
    */

    // Handles adding a new sponsor from the web form
    @PostMapping("/sponsors/add")
    public String addSponsorFromWeb(@ModelAttribute Sponsor sponsor, Model model) {
        if (sponsor.getPhoneNumber() != null && sponsor.getPhoneNumber().length() > 0 && !sponsor.getPhoneNumber().matches("^[0-9]+$")) {
            model.addAttribute("error", "Phone number must contain digits only.");
            model.addAttribute("sponsors", sponsorRepository.findAll());
            model.addAttribute("contracts", contractRepository.findAll());
            Iterable<String> sponsorNames = fetchContractSponsorNames();
            model.addAttribute("sponsorNames", sponsorNames);
            return "sponsors";
        }
        sponsorRepository.save(sponsor);

        return "redirect:/sponsors";
    }

    // Handles creating a new contract for a sponsor
    @PostMapping("/sponsors/addContract")
    public String addContractForSponsor(@ModelAttribute Contract contract, Model model) {
        try {
            contractRepository.save(contract);
            return "redirect:/sponsors";
        } catch (IllegalArgumentException ex) {
            // ???
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("sponsors", sponsorRepository.findAll());
            model.addAttribute("contracts", contractRepository.findAll());

            Iterable<String> sponsorNames = fetchContractSponsorNames();
            model.addAttribute("sponsorNames", sponsorNames);
            return "sponsors";
        }
    }


    // Deletes a sponsor and all contracts linked to that sponsor
    @PostMapping("/sponsors/delete")
    public String deleteSponsor(@RequestParam Long sponsorId) {
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
    public String deleteContract(@RequestParam Long contractId) {
        contractRepository.deleteById(contractId);
        return "redirect:/sponsors";
    }

    // fjern requestparam?
    @GetMapping("/getFile")
    public ResponseEntity<byte[]> getFile(@RequestParam long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("/uploadFile, Contract not found"));
        byte[] pdfData = contract.getPdfData();

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + contract.getFileName() + ".pdf\""
            )
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfData);

    }

    // File upload
    @PostMapping("/uploadFile")
    public String uploadFileTo(@RequestParam MultipartFile pdffile, @RequestParam Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("/uploadFile, Contract not found"));

        try {
            contract.setPdfData(pdffile.getBytes());
			String cleanFilename = Paths.get(pdffile.getOriginalFilename()).getFileName().toString(); //Get the name of the file
			contract.setFileName(cleanFilename); //save the name of the file in contract
            System.out.println("\n\nPDF file loaded successfully!\n\n");
        } catch (IOException e) {
            e.printStackTrace();
            return "error"; // 
        }

        contractRepository.save(contract);

        return "redirect:/users";
    }

    @GetMapping("/test")
    public String showTestPage() {
        return "test";
    }

    @GetMapping("/archive")
    public String showArchivePage() {
        return "archive";
    }

    @GetMapping("/AdminPanel")
    public String showAdminPanelPage() {
        return "AdminPanel";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        User user = new User();
        user.setName("test");
        //BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(8);
        //String hashedPassword = passwordEncoder.encode("test123");
        String hashedPassword = "test123";
        user.setPassword(hashedPassword);
        userRepository.save(user);
        
        return "login"; // Thymeleaf login template
    }

    @PostMapping("/login/confirm")
    public String confirmLogin(@RequestParam String username, @RequestParam String hashedPassword, @RequestParam boolean rememberMe, Model model, HttpServletResponse response) {
        User user = userRepository.findByName(username);
        //BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(8);
        //if(passwordEncoder.matches(hashedPassword, user.getPassword())) {
        if(hashedPassword.equals(user.getPassword())) {
            String id = user.getId().toString();
            Token token = Token.sign(id);
            String formattedToken = user.getId().toString() + "." + token.getHash();
            String encodedToken = URLEncoder.encode(formattedToken, StandardCharsets.UTF_8);
            int time = rememberMe ? 2_592_000 : 86400; // 1 måned vs 1 dag
            ResponseCookie cookie = ResponseCookie.from("token", encodedToken)
            .httpOnly(true)  // javascript kan ikke røre den B-)
            .secure(true)    // HTTPS only
            .path("/")       // Bruges til alle sider
            .maxAge(time)
            .build();
            response.addHeader("Set-Cookie", cookie.toString());
            System.out.println("\n\n\n\nPASSWORDS MATCH\n\n\n\n");
            return "redirect:/homepage";
        } else {
            System.out.println("\n\n\n\nPASSWORDS DO NOT MATCH\n\n\n\n");
            return "redirect:/login";
        }
    }

    private boolean userHasValidToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, "token");
        if (cookie == null) return false;

        String decodedToken = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);        
        String[] parts = decodedToken.split("\\.");

        String cookieId = parts[0];
        String cookieHash = parts[1];

        Token token = Token.sign(cookieId);
        return token.verify(cookieHash);
    }

    /* 
    @PostMapping("/login/confirm")
    public String confirmLogin(
            @RequestParam String username,
            @RequestParam String email,
            Model model) {

        User user = userRepository.findByName(username);

        if (user == null || !Objects.equals(email, user.getEmail())) {
            model.addAttribute("error", "Invalid username or email");
            return "login";
        }

        model.addAttribute("user", user);
        return "homepage"; // login success page
    }
    */

    @GetMapping("/homepage")
    public String showhomepage(Model model, HttpServletRequest request) {
        if(userHasValidToken(request)) {
            System.out.println("\n\nTOKEN IS VALID\n\n");
            Iterable<Sponsor> sponsors = sponsorRepository.findAll();
            model.addAttribute("sponsors", sponsors);
            return "homepage";
        } else {
            System.out.println("\n\n\n\nTOKEN IS NOT VALID\n\n\n\n");
            return "redirect:/login";
        }
    }

    // Add user with demo sponsor & contract
    @PostMapping("/users/add")
    public String addUserFromWeb(@RequestParam String name, @RequestParam String email) {
        Sponsor sponsor = new Sponsor(
                "Demo",
                "DemoName",
                "Demo Email",
                "12345678",
                "12345678",
                false,
                "Demo Comment");
        sponsorRepository.save(sponsor);

        Contract contract = new Contract(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                "2000",
                true,
                "Standard");
        try {
            byte[] pdfBytes = Files.readAllBytes(Paths.get("C:\\Users\\mathi\\Downloads\\easyFIle.pdf"));
            contract.setPdfData(pdfBytes);
        } catch (IOException e) {
            System.out.println("Could not set PDF data: " + e.getMessage());
        }
        contractRepository.save(contract);

        User user = new User();
        user.setName(name);
        //user.setEmail(email);
        userRepository.save(user);

        return "redirect:/users";
    }

    /*
    public boolean isContractActive(Contract contract) {
        List<Service> services = serviceRepository.findAll();
        for(Service service : services) {
            Long contractServiceId = service.getContractId();
            if(contractServiceId == contract.getId()) {
                Service contractService = serviceRepository.findById(contractServiceId).orElseThrow();
                if(!contractService.getEndDate().isAfter("I DAG") || !contractService.getDelivered()) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean checkForActiveContracts(Sponsor sponsor) {
        List<Contract> contracts = contractRepository.findAll();
        for(Contract contract : contracts) {
            if(sponsor.getId() == contract.getSponsorId() && !contract.getArhived()) {
            }
        }
        return false;
    }
    */




    // =================================================================================================================
    // DEPRECATED LEGACY CODE
    
    // Handles editing an existing sponsor
    @PostMapping("/sponsors/edit")
    public String editSponsor(
            @RequestParam Long sponsorId,
            @RequestParam String name,
            @RequestParam(required = false) String contactPerson,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String cvrNumber,
            @RequestParam(required = false, defaultValue = "false") boolean status,
            @RequestParam(required = false) String comments,
            Model model) {
        if (phoneNumber != null && phoneNumber.length() > 0 && !phoneNumber.matches("^[0-9]+$")) {
            model.addAttribute("error", "Phone number must contain digits only.");
            model.addAttribute("sponsors", sponsorRepository.findAll());
            model.addAttribute("contracts", contractRepository.findAll());
            Iterable<String> sponsorNames = fetchContractSponsorNames();
            model.addAttribute("sponsorNames", sponsorNames);
            return "sponsors";
        }
        java.util.Optional<Sponsor> sponsorOpt = sponsorRepository.findById(sponsorId);
        if (sponsorOpt.isPresent()) {
            Sponsor sponsor = sponsorOpt.get();
            // update fields (keep generated id)
            sponsor.setName(name == null ? sponsor.getName() : name);
            sponsor.setContactPerson(contactPerson == null ? sponsor.getContactPerson() : contactPerson);
            sponsor.setEmail(email == null ? sponsor.getEmail() : email);
            sponsor.setPhoneNumber(phoneNumber == null ? sponsor.getPhoneNumber() : phoneNumber);
            sponsor.setCvrNumber(cvrNumber == null ? sponsor.getCvrNumber() : cvrNumber);
            sponsor.setStatus(status);
            sponsor.setComments(comments == null ? sponsor.getComments() : comments);
            sponsorRepository.save(sponsor);

            // update stored name copy on contracts
            Iterable<Contract> contracts = contractRepository.findAll();
            for (Contract contract : contracts) {
                if (sponsorId.equals(contract.getSponsorId())) {
                    // contract.setSponsorName(sponsor.getName());
                    contractRepository.save(contract);
                }
            }
        }
        return "redirect:/sponsors";
    }
    // =================================================================================================================


    // =================================================================================================================
    // DEPRECATED LEGACY CODE

    // Handles editing an existing contract
    @PostMapping("/sponsors/editContract")
    public String editContract(
            @RequestParam Long contractId,
            @RequestParam Long sponsorId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String payment,
            @RequestParam(required = false, defaultValue = "false") boolean status,
            @RequestParam String type,
            Model model) {
        java.util.Optional<Contract> contractOpt = contractRepository.findById(contractId);
        if (contractOpt.isPresent()) {
            Contract contract = contractOpt.get();
            contract.setSponsorId(sponsorId);
            try {
                contract.setStartDate(LocalDate.parse(startDate));
                contract.setEndDate(LocalDate.parse(endDate));
                contract.setPayment(payment);
                contract.setStatus(status);
                contract.setType(type);
                contractRepository.save(contract);
            } catch (IllegalArgumentException ex) {
                model.addAttribute("error", ex.getMessage());
                model.addAttribute("sponsors", sponsorRepository.findAll());
                model.addAttribute("contracts", contractRepository.findAll());
                Iterable<String> sponsorNames = fetchContractSponsorNames();
                model.addAttribute("sponsorNames", sponsorNames);
                return "sponsors";
            }
        }
        return "redirect:/sponsors";
    }

    // =================================================================================================================


}
