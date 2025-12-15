package p3project.functions;

import java.lang.reflect.Field;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Changelog;
import p3project.classes.Contract;
import p3project.classes.Service;
import p3project.classes.Sponsor;
import p3project.classes.User;
import p3project.repositories.ContractRepository;
import p3project.repositories.LogRepository;
import p3project.repositories.ServiceRepository;
import p3project.repositories.SponsorRepository;

@Component
public class EventlogFunctions {
    
    @Autowired
    private LogRepository logRepository;

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private UserFunctions userFunctions;
    
    //Wrappers
    private User getUserFromToken(HttpServletRequest request) {
        return userFunctions.getUserFromToken(request);
    }

    public <T> String handleUpdateRequest(T requestObject, T storedObject, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Integer fieldsChanged;
        try {
            fieldsChanged = compareFields(requestObject, storedObject, request);
            String message;
            if(fieldsChanged == 0)      message = "Ingen felter ændret";
            else if(fieldsChanged == 1) message = "Opdateret 1 felt";
            else                        message = "Opdateret " + fieldsChanged + " felter";
            
            redirectAttributes.addFlashAttribute("responseMessage", message);
            return "redirect:/sponsors";
        } catch (ClassNotFoundException error) {
            redirectAttributes.addFlashAttribute("responseMessage","Intern serverfejl, prøv igen");
            return "redirect:/sponsors";
        }
    }


    public <T> Integer compareFields(T requestObject, T storedObject, HttpServletRequest request) throws ClassNotFoundException {

        Integer fieldsChanged = 0;
        Field[] fields = requestObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object before = field.get(storedObject);
                Object after = field.get(requestObject);
                if (!Objects.equals(before, after)) {
                    if(fieldShouldBeLogged(field)) {
                        User user = getUserFromToken(request);
                        Changelog log = new Changelog(user, requestObject, field, before, after);
                        logRepository.save(log);
                        fieldsChanged++;
                    }
                    field.set(storedObject, after);
                }
            } catch (IllegalAccessException error) {
                throw new RuntimeException(error);
            }
        }

        if (requestObject instanceof Sponsor)         sponsorRepository.save((Sponsor) storedObject);
        else if (requestObject instanceof Contract)   contractRepository.save((Contract) storedObject);
        else if (requestObject instanceof Service)    serviceRepository.save((Service) storedObject);
        else throw new ClassNotFoundException();

        return fieldsChanged;
    }

    public boolean fieldShouldBeLogged(Field field) { // jank
        String fieldName = field.getName();
        switch(fieldName) {
            case "pdfData":
                return false;
            default:
                return true;
        }
    }
}
