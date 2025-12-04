package p3project;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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


    @GetMapping("/")
    public String home() {
        return "redirect:/login";
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
    public String showSponsors(Model model, HttpServletRequest request) {
        if(userHasValidToken(request)) {
            model.addAttribute("sponsors", sponsorRepository.findAll());
            model.addAttribute("contracts", contractRepository.findAll());

            Iterable<String> sponsorNames = fetchContractSponsorNames();
            model.addAttribute("sponsorNames", sponsorNames);
            return "sponsors";
        }
        return "redirect:/login";
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
    public String changelogPage(Model model, HttpServletRequest request) {
        if(userHasValidToken(request)) {
            model.addAttribute("changelogs", logRepository.findAll());
            return "changelog";
        }
        return "redirect:/login";
    }

    // boilerplate update handlers
    @PostMapping("/update/sponsor")
    public ResponseEntity<String> updateSponsorFields(@ModelAttribute Sponsor sponsor, HttpServletRequest request) {

        Sponsor storedSponsor = sponsorRepository.findById(sponsor.getId())
        .orElseThrow(() -> new RuntimeException("Unable to retrieve sponsor with id: " + sponsor.getId()));
        return handleUpdateRequest(sponsor, storedSponsor, request);
    }

    @PostMapping("/update/contract")
    public ResponseEntity<String> updateContractFields(@ModelAttribute Contract contract, HttpServletRequest request) { // pdfdata dead on arrival
        Contract storedContract = contractRepository.findById(contract.getId())
        .orElseThrow(() -> new RuntimeException("Unable to retrieve contract with id: " + contract.getId()));
        return handleUpdateRequest(contract, storedContract, request);
    }

    @PostMapping("/update/service")
    public ResponseEntity<String> updateServiceFields(@ModelAttribute Service service, HttpServletRequest request) {
        Service storedService = serviceRepository.findById(service.getId())
        .orElseThrow(() -> new RuntimeException("Unable to retrieve service with id: " + service.getId()));

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

        return handleUpdateRequest(requestService, storedService);
    }

    private <T> ResponseEntity<String> handleUpdateRequest(T requestObject, T storedObject, HttpServletRequest request) {
        Integer fieldsChanged;
        try {
            fieldsChanged = compareFields(requestObject, storedObject, request);
        } catch (ClassNotFoundException error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("redirect:/sponsors");
        }
        return ResponseEntity.status(HttpStatus.OK).body("redirect:/sponsors");
    }

    // fejl håndtering tba, if(==null) etc...
    private <T> Integer compareFields(T requestObject, T storedObject, HttpServletRequest request) throws ClassNotFoundException {

        Integer fieldsChanged = 0;
        Field[] fields = requestObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if(field.getName().equals("pdfData")) continue; // hack
                Object before = field.get(storedObject);
                Object after = field.get(requestObject);
                if (!Objects.equals(before, after)) { // indbygget null håndtering
                    User user = getUserFromToken(request);
                    Changelog log = new Changelog(user, requestObject, field, before, after);
                    // handleSpecialFields(field, log, storedObject, requestObject);
                    logRepository.save(log);
                    field.set(storedObject, after);

                    fieldsChanged++;
                }
            } catch (IllegalAccessException error) {
                throw new RuntimeException(error);
            }
        }

        // Gem ændringerne i den relevante repository baseret på objekt-type
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
            return "redirect:/sponsors"; // Post/Redirect/Get for at undgå double submit
        } catch (IllegalArgumentException ex) {

            // Håndterer fejl og viser siden igen med fejlbesked
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("sponsors", sponsorRepository.findAll());
            model.addAttribute("contracts", contractRepository.findAll());
          
            Iterable<String> sponsorNames = fetchContractSponsorNames();
            model.addAttribute("sponsorNames", sponsorNames);
            return "sponsors"; // Geninlæs siden og vis sponsors-siden med fejlbesked
        }
    }

    // Handles creating a new service for a contract
    @PostMapping("/sponsors/addService")
    public String addServiceForContract(
            @RequestParam Long contractId,
            @RequestParam(required = false) String name,
            @RequestParam String type,
            @RequestParam(required = false, defaultValue = "0") int amountOrDivision,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false, defaultValue = "AKTIV") String status,
            Model model) {
        try {
            java.time.LocalDate start = startDate == null || startDate.isEmpty() ? null : java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = endDate == null || endDate.isEmpty() ? null : java.time.LocalDate.parse(endDate);
            p3project.classes.ServiceType st = p3project.classes.ServiceType.valueOf(type);
            Service.ServiceStatus ss = Service.ServiceStatus.valueOf(status);
            p3project.classes.Service service = new p3project.classes.Service(contractId, name == null ? "" : name, st, ss, amountOrDivision, start, end);
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

    // Deletes a service by ID
    @PostMapping("/sponsors/deleteService")
    public String deleteService(@RequestParam Long serviceId) {
        serviceRepository.deleteById(serviceId);
        return "redirect:/sponsors";
    }

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


//     // Handles editing an existing contract
//     @PostMapping("/sponsors/editContract")
//     public String editContract(
//             @RequestParam Long contractId,
//             @RequestParam Long sponsorId,
//             @RequestParam String startDate,
//             @RequestParam String endDate,
//             @RequestParam String payment,
//             @RequestParam(required = false, defaultValue = "false") boolean status,
//             @RequestParam String type,
//             @RequestParam(required = false) String name,
//             Model model) {
//         java.util.Optional<Contract> contractOpt = contractRepository.findById(contractId);
//         // Hent kontrakten, opdater felter og gem. Valider datoer koncentrisk.
//         if (contractOpt.isPresent()) {
//             Contract contract = contractOpt.get();
//             contract.setSponsorId(sponsorId);
//             java.util.Optional<Sponsor> sponsorOpt = sponsorRepository.findById(sponsorId);
//             if (sponsorOpt.isPresent())
//                 contract.setSponsorName(sponsorOpt.get().getName());
//             try {
//                 contract.setStartDate(LocalDate.parse(startDate));
//                 contract.setEndDate(LocalDate.parse(endDate));
//                 contract.setPayment(payment);
//                 contract.setStatus(status);
//                 contract.setType(type);
//                 // Update kontrakt navn when provided from the edit form
//                 if (name != null) {
//                     contract.setName(name);
//                 }
//                 contractRepository.save(contract);
//             } catch (IllegalArgumentException ex) {
//                 model.addAttribute("error", ex.getMessage());
//                 model.addAttribute("sponsors", sponsorRepository.findAll());
//                 model.addAttribute("contracts", contractRepository.findAll());
//                 return "sponsors"; // Ved fejl -> vis sponsors-siden med fejl
//             }
//         }
//         return "redirect:/sponsors";
//     }

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
    public String uploadFileTo(@RequestParam MultipartFile pdfFile, @RequestParam Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("/uploadFile, Contract not found"));

        try {
            contract.setPdfData(pdfFile.getBytes());
			String cleanFilename = Paths.get(pdfFile.getOriginalFilename()).getFileName().toString(); //Get the name of the file
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
    public String showTestPage(Model model, HttpServletRequest request) {
        if (userHasValidToken(request)) {
            System.out.println("\n\nTOKEN IS VALID\n\n");
            return "test";
        }
        return "redirect:/login";
    }

    @GetMapping("/archive")
    public String showArchivePage(Model model, HttpServletRequest request) {
        if (userHasValidToken(request)) {
            System.out.println("\n\nTOKEN IS VALID\n\n");
            return "archive";
        }
        return "redirect:/login";
    }

    @GetMapping("/AdminPanel")
    public String showAdminPanelPage(Model model, HttpServletRequest request) {
        if (userHasValidToken(request)) {
            System.out.println("\n\nTOKEN IS VALID\n\n");
            return "AdminPanel";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        if (userHasValidToken(request)) {
            System.out.println("\n\nTOKEN IS VALID\n\n");
            return "redirect:/homepage"; // må ikke logge ind igen
        }        
        return "login";
    }

    @GetMapping("/testuser")
    public String testUser(HttpServletResponse response) {
        User user = new User();
        user.setName("testnavn123");
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

    @PostMapping("/login/confirm")
    public String confirmLogin(@RequestParam String username, @RequestParam String hashedPassword, @RequestParam boolean rememberMe, Model model, HttpServletResponse response) {
        User user = userRepository.findByName(username);
        //BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(8);
        //if(passwordEncoder.matches(hashedPassword, user.getPassword())) {
        if(hashedPassword.equals(user.getPassword())) {
            String id = user.getId().toString();
            Token token = Token.sign(id);
            String formattedToken = id + "." + token.getHash();
            String encodedToken = URLEncoder.encode(formattedToken, StandardCharsets.UTF_8);
            int time = rememberMe ? (60 * 60 * 24 * 365) : (60 * 60 * 24); // 1 måned vs 1 dag
            ResponseCookie cookie = ResponseCookie.from("token", encodedToken)
            .httpOnly(true)  // javascript kan ikke røre den B-)
            .secure(false)    // HTTPS only
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
    public String addUserFromWeb(@RequestParam String name, @RequestParam String password, Model model) {
        List<User> users = userRepository.findAll();
        for(User user : users) {
            if(name.equals(user.getName())) {
                model.addAttribute("errorMessage", "Username already exists");
                return "redirect:/AdminPanel";
            }
        }
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        



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
}
