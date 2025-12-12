package p3project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import p3project.functions.ChangelogFunctions;
import p3project.functions.UserFunctions;

@Controller
public class ChangeLogController {
    @Autowired
    private ChangelogFunctions changelogFunctions;
    
    @Autowired
    private UserFunctions userFunctions;

    public String changelogPage(Model model, HttpServletRequest request) {
        return changelogFunctions.changelogPage(model, request);
    }


    //Wrappers
    private boolean userHasValidToken(HttpServletRequest request) {
        return userFunctions.userHasValidToken(request);
    }

    private boolean userIsAdmin(HttpServletRequest request) {
        return userFunctions.userIsAdmin(request);
    }

    @GetMapping("/changelog")
    public String changelogPageMapping(Model model, HttpServletRequest request) {
        if(!userHasValidToken(request)) return "redirect:/login";
        if(!userIsAdmin(request))       return "redirect:/homepage";

        return changelogPage(model, request);
    }
}
