package p3project.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import static p3project.functions.homeFunction.*;

@Controller

public class homeController {
    @GetMapping("/")
    public String home() {
        return handleHomeRequest();
    }
}