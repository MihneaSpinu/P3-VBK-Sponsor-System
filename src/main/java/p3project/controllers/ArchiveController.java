package p3project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import p3project.functions.ArchiveFunctions;

@Controller
public class ArchiveController {
    
    @Autowired
    private ArchiveFunctions archiveFunctions;

    public String showArchivePage(Model model, HttpServletRequest request) {
        return archiveFunctions.showArchivePage(model, request);
    }

    @GetMapping("/archive")
    public String showArchivePageMapping(Model model, HttpServletRequest request) {
        return showArchivePage(model, request);
    }
}
