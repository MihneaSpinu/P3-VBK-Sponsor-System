package p3project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import p3project.functions.AdminPanelFunctions;
import p3project.functions.UserFunctions;

@Controller
public class AdminPanelController {

    @Autowired
    private UserFunctions userFunctions;

    @Autowired AdminPanelFunctions adminPanelFunctions;

    //Wrappers
    private boolean userHasValidToken(HttpServletRequest request) {
        return userFunctions.userHasValidToken(request);
    }

    private boolean userIsAdmin(HttpServletRequest request) {
        return userFunctions.userIsAdmin(request);
    }

    private String showAdminPanelPage(Model model, HttpServletRequest request) {
        return adminPanelFunctions.showAdminPanelPage(model, request);
    }
    
    @GetMapping("/AdminPanel")
    public String showAdminPanelPageMapping(Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";
        return showAdminPanelPage(model, request);
    }
}
