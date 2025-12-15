package p3project.functions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
import p3project.repositories.UserRepository;

@Component
public class AdminPanelFunctions{

    @Autowired
    private UserRepository userRepository;

    public String showAdminPanelPage(Model model, HttpServletRequest request) {
        model.addAttribute("users", userRepository.findAll());
        return "AdminPanel";
    }
}
