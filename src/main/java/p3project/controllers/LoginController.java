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
    private LoginFunctions loginFunctions;

    //Wrappers
    private  String confirmLogin(String username,String password,boolean rememberMe, Model model, HttpServletResponse response) {
        return loginFunctions.confirmLogin(username, password, rememberMe, model, response);
    }

    private  String logout(HttpServletRequest request, HttpServletResponse response) {
        return loginFunctions.logout(request, response);
    }

    private String loginPage(Model model, HttpServletRequest request) {
        return loginFunctions.loginPage(model, request);
    }
    

    

    @PostMapping("/login/confirm")
    public String confirmLoginMapping(@RequestParam String username, @RequestParam String password, @RequestParam boolean rememberMe, Model model, HttpServletResponse response) {
        return confirmLogin(username, password, rememberMe, model, response);
    }

    @GetMapping("/logout") 
    public String logoutMapping(HttpServletRequest request, HttpServletResponse response) {
        return logout(request, response);
    }

    @GetMapping("/login")
    public String loginPageMapping(Model model, HttpServletRequest request) {
        return loginPage(model, request);
    }


}
