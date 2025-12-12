package p3project.functions;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Eventlog;
import p3project.repositories.LogRepository;

@Component
public class ChangelogFunctions {
    @Autowired
    private LogRepository logRepository;

    public String changelogPage(Model model, HttpServletRequest request) {

        // make newest logs appear first
        List<Eventlog> logs = logRepository.findAll();
        Collections.reverse(logs);
        model.addAttribute("changelogs", logs);
        return "changelog";
    }
}
