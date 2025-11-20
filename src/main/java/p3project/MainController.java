package p3project;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import p3project.classes.Changelog;
import p3project.classes.Contract;
import p3project.classes.Service;
import p3project.classes.Sponsor;
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
    // API endpoints (JSON)
    // ==========================
    @PostMapping(path = "/demo/add")
    public @ResponseBody String addNewUser(@RequestParam String name, @RequestParam String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
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
        return "users";
    }

    // Displays the sponsors page with lists of sponsors and contracts
    @GetMapping("/sponsors")
    public String showSponsors(Model model) {
        model.addAttribute("sponsors", sponsorRepository.findAll());
        model.addAttribute("contracts", contractRepository.findAll());
        return "sponsors";
    }


    // Changelog page
    @GetMapping("/changelog")
    public String changelogPage(Model model) {
        model.addAttribute("changelogs", logRepository.findAll());
        return "changelog";
    }

    
    // boilerplate update handlers
    @PostMapping("/update/sponsor")
    public ResponseEntity<String> updateSponsorFields(@RequestBody Sponsor sponsor) {
        Sponsor storedSponsor = sponsorRepository.getReferenceById(sponsor.getId());
        return handleUpdateRequest(sponsor, storedSponsor);
    }

    @PostMapping("/update/contract")
    public ResponseEntity<String> updateContractFields(@RequestBody Contract contract) {
        Contract storedContract = contractRepository.getReferenceById(contract.getId());
        return handleUpdateRequest(contract, storedContract);
    }

    @PostMapping("/update/service")
    public ResponseEntity<String> updateServiceFields(@RequestBody Service service) {
        Service storedService = serviceRepository.getReferenceById(service.getId());
        return handleUpdateRequest(service, storedService);
    }

    private <T> ResponseEntity<String> handleUpdateRequest(T requestObject, T storedObject) {
        Integer fieldsChanged;
        try {
            fieldsChanged = compareFields(requestObject, storedObject);
        } catch(ClassNotFoundException error) {
            return new ResponseEntity<>("Internal server error: " + error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(fieldsChanged.toString(), HttpStatus.OK);
    }

    // fejl håndtering tba, if(==null) etc...
    private <T> Integer compareFields(T requestObject, T storedObject) throws ClassNotFoundException {
        // if(!(requestObject.getClass().equals(storedObject.getClass())))

        Integer fieldsChanged = 0;
        Field[] fields = requestObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.getName().equals("id")) continue; // slet?
                Object before = field.get(storedObject);
                Object after = field.get(requestObject);
                if (!before.equals(after)) {
                    Changelog log = Changelog.create(new User(), requestObject.toString(), requestObject.toString(), before.toString(), after.toString()); // wip
                    logRepository.save(log);
                    field.set(storedObject, after);
                    fieldsChanged++;
                    System.out.println("Updated " + field.toString() + ": " + before.toString() + " -> " + after.toString());
                }
            } catch (IllegalAccessException error) {
                throw new RuntimeException(error);
            }
        }

        // lav compareFields returnere "T", og lav nedenstående til sin egen funktion "saveUpdatedObject" eller w/e
        if (requestObject instanceof Sponsor)       sponsorRepository.save((Sponsor) storedObject);
        else if (requestObject instanceof Contract) contractRepository.save((Contract) storedObject);
        else if (requestObject instanceof Service)  serviceRepository.save((Service) storedObject);
        else throw new ClassNotFoundException();

        return fieldsChanged;
    }

    // Handles adding a new sponsor from the web form
    @PostMapping("/sponsors/add")
    public String addSponsorFromWeb(
            @RequestParam String sponsorName,
            @RequestParam String contactPerson,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String cvrNumber,
            @RequestParam(required = false, defaultValue = "false") boolean status,
            @RequestParam(required = false) String comments,
            Model model) {
        if (phoneNumber != null && phoneNumber.length() > 0 && !phoneNumber.matches("^[0-9]+$")) {
            model.addAttribute("error", "Phone number must contain digits only.");
            model.addAttribute("sponsors", sponsorRepository.findAll());
            model.addAttribute("contracts", contractRepository.findAll());
            return "sponsors";
        }

        Sponsor sponsor = new Sponsor(sponsorName, contactPerson, email, phoneNumber, cvrNumber, status,
                comments == null ? "" : comments);
        sponsorRepository.save(sponsor);
        return "redirect:/sponsors";
    }

    // Handles creating a new contract for a sponsor
    @PostMapping("/sponsors/addContract")
    public String addContractForSponsor(
            @RequestParam String sponsorName,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam int payment,
            @RequestParam(required = false, defaultValue = "false") boolean status,
            @RequestParam String type) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        Contract contract = new Contract(start, end, payment, status, type);
        contract.setSponsorName(sponsorName);
        contractRepository.save(contract);
        return "redirect:/sponsors";
    }

    // Handles editing an existing sponsor
    @PostMapping("/sponsors/edit")
    public String editSponsor(
            @RequestParam Long id,
            @RequestParam String originalSponsorName,
            @RequestParam String sponsorName,
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
        Sponsor maybe = sponsorRepository.getReferenceById(id);
        if (maybe != null) {
            Sponsor s = maybe;
            if (!id.equals(sponsorName)) {
                Sponsor newS = new Sponsor(sponsorName,
                        contactPerson == null ? s.getContactPerson() : contactPerson,
                        email == null ? s.getEmail() : email,
                        phoneNumber == null ? s.getPhoneNumber() : phoneNumber,
                        cvrNumber == null ? s.getCvrNumber() : cvrNumber,
                        status,
                        comments == null ? s.getComments() : comments);

                sponsorRepository.deleteById(id);
                sponsorRepository.save(newS);

                Iterable<Contract> contracts = contractRepository.findAll();
                for (Contract c : contracts) {
                    if (id.equals(c.getSponsorName())) {
                        c.setSponsorName(sponsorName);
                        contractRepository.save(c);
                    }
                }
            } else {
                s.setContactPerson(contactPerson == null ? s.getContactPerson() : contactPerson);
                s.setEmail(email == null ? s.getEmail() : email);
                s.setPhoneNumber(phoneNumber == null ? s.getPhoneNumber() : phoneNumber);
                s.setCvrNumber(cvrNumber == null ? s.getCvrNumber() : cvrNumber);
                s.setStatus(status);
                s.setComments(comments == null ? s.getComments() : comments);
                sponsorRepository.save(s);
            }
        }
        return "redirect:/sponsors";
    }

    // Deletes a sponsor and all contracts linked to that sponsor
    @PostMapping("/sponsors/delete")
    public String deleteSponsor(@RequestParam Long id) {
        sponsorRepository.deleteById(id);
        Iterable<Contract> contracts = contractRepository.findAll();
        for (Contract c : contracts) {
            if (id.equals(c.getId())) {
                contractRepository.deleteById(c.getId());
            }
        }
        return "redirect:/sponsors";
    }

    // Handles editing an existing contract
    @PostMapping("/sponsors/editContract")
    public String editContract(
            @RequestParam Long contractId,
            @RequestParam String sponsorName,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam int payment,
            @RequestParam(required = false, defaultValue = "false") boolean status,
            @RequestParam String type) {
        var maybe = contractRepository.findById(contractId);
        if (maybe.isPresent()) {
            Contract c = maybe.get();
            c.setSponsorName(sponsorName);
            c.setStartDate(LocalDate.parse(startDate));
            c.setEndDate(LocalDate.parse(endDate));
            c.setPayment(payment);
            c.setStatus(status);
            c.setType(type);
            contractRepository.save(c);
        }
        return "redirect:/sponsors";
    }

    // Deletes a contract by ID
    @PostMapping("/sponsors/deleteContract")
    public String deleteContract(@RequestParam Long contractId) {
        contractRepository.deleteById(contractId);
        return "redirect:/sponsors";
    }

    // File upload
    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam MultipartFile pdffile) {
        Contract contract = new Contract(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                2000,
                true,
                "Standard");
        try {
            contract.setPdfData(pdffile.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
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
        return "Loginpage3";
    }

    @PostMapping("/login/confirm")
    public String confirmLogin(
            @RequestParam String username,
            @RequestParam String email,
            Model model) {

        User user = userRepository.findByName(username);

        if (user == null || !Objects.equals(email, user.getEmail())) {
            model.addAttribute("error", "Invalid username or email");
            return "Loginpage3";
        }

        model.addAttribute("user", user);
        return "Complete";
    }

    @GetMapping("/Complete")
    public String showCompletePage() {
        return "Complete";
    }

    @GetMapping("/homepage")
    public String showHomepage(Model model) {
        Iterable<Sponsor> sponsors = sponsorRepository.findAll();
        model.addAttribute("sponsors", sponsors);
        return "homepage";
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
                2000,
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
        user.setEmail(email);
        userRepository.save(user);

        return "redirect:/users";
    }
}
