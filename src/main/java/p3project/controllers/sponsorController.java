package p3project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;

import p3project.repositories.SponsorRepository;
import p3project.repositories.UserRepository;
import p3project.repositories.ContractRepository;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;

import static p3project.functions.userFunction.*;

@Controller
public class sponsorController {
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private SponsorRepository sponsorRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping("/sponsors")
    public String showSponsors(Model model, HttpServletRequest request) {
    }
}   