package p3project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import p3project.functions.UserFunctions;

@Controller
public class UserController {

    @Autowired
    private UserFunctions uF;

    @PostMapping("/users/add")
    public String addUserFromMapping(@RequestParam String name, @RequestParam String password, boolean isAdmin, Model model, RedirectAttributes redirectAttributes) {
        return uF.addUser(name, password, isAdmin, redirectAttributes);
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUserByIdMapping(@PathVariable Long id){
        return uF.deleteUserById(id);
    }    

    @GetMapping("/testuser")
    public String testUserMapping(HttpServletResponse response) {
        return uF.testUser(response);
    }
}
