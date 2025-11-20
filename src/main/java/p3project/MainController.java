package p3project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import p3project.classes.Contract;
import p3project.classes.Sponsor;
import p3project.classes.User;
import p3project.classes.Changelog;
import p3project.repositories.ContractRepository;
import p3project.repositories.SponsorRepository;
import p3project.repositories.UserRepository;
import p3project.repositories.LogRepository;

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

    @GetMapping("/changelog")
    public String changelogPage(Model model) {
        model.addAttribute("changelog", logRepository.findAll());
        return "changelog";
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
        return "Loginpage3"; // Thymeleaf login template
    }

    @PostMapping("/login/confirm")
    public String confirmLogin(
            @RequestParam String username,
            @RequestParam String email,
            Model model) {

        User user = userRepository.findByName(username);

        // null-safe comparison
        if (user == null || !Objects.equals(email, user.getEmail())) {
            model.addAttribute("error", "Invalid username or email");
            return "Loginpage3";
        }

        model.addAttribute("user", user);
        return "Complete"; // login success page
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

    // ==========================
    // File upload
    // ==========================
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

    // ==========================
    // Add user with demo sponsor & contract
    // ==========================
    @PostMapping("/users/add")
    public String addUserFromWeb(@RequestParam String name, @RequestParam String email) {
        // Create demo sponsor
        Sponsor sponsor = new Sponsor(
                "Demo",
                "DemoName",
                "Demo Email",
                "12345678",
                "12345678",
                false,
                "Demo Comment");
        sponsorRepository.save(sponsor);

        // Create demo contract
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

        // Save user
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        userRepository.save(user);

        return "redirect:/users";
    }
}
