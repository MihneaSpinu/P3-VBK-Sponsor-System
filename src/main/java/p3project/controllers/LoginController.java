package p3project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import p3project.functions.LoginFunctions;

@Controller
public class LoginController {
    
    @Autowired
    private LoginFunctions lF;

    @PostMapping("/login/confirm")
    public String confirmLoginMapping(@RequestParam String username, @RequestParam String password, @RequestParam boolean rememberMe, Model model, HttpServletResponse response) {
        return lF.confirmLogin(username, password, rememberMe, model, response);
    }

    @GetMapping("/logout") 
    public String logoutMapping(HttpServletRequest request, HttpServletResponse response) {
        return lF.logout(request, response);
    }

    @GetMapping("/login")
    public String loginPageMapping(Model model, HttpServletRequest request) {
        return lF.loginPage(model, request);
    }


}
