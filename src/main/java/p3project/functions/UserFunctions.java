package p3project.functions;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import p3project.classes.Token;
import p3project.classes.User;
import p3project.repositories.UserRepository;

@Component
public class UserFunctions {

    @Autowired
    private UserRepository userRepository;

    public User getUserFromToken(HttpServletRequest request) throws RuntimeException {
        String[] parsedCookie = parseCookie(request);
        Long userId = Long.valueOf(parsedCookie[0]);
        User user = userRepository.findById((userId)).orElse(null);
        if(user == null) throw new RuntimeException("Unable to retrieve username");
        return user;
    }

    public boolean userHasValidToken(HttpServletRequest request) {
        String cookieId;
        String cookieHash;
        try {
            String[] parsedCookie = parseCookie(request);
            cookieId = parsedCookie[0];
            cookieHash = parsedCookie[1];
        } catch(RuntimeException e) {
            return false;
        }
        Token token = Token.sign(cookieId);
        return token.verify(cookieHash);
    }   

    public boolean userIsAdmin(HttpServletRequest request) {
        User user = getUserFromToken(request);
        return user.getIsAdmin();
    }


    public String[] parseCookie(HttpServletRequest request) throws RuntimeException{ // find anden exception?
        Cookie cookie = WebUtils.getCookie(request, "token");
        if (cookie == null) throw new RuntimeException();
        
        String decodedToken = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);        
        String[] splitCookie = decodedToken.split("\\.");
        if(splitCookie.length != 2) throw new RuntimeException();

        return splitCookie;
    }


    public String addUser(String name, String password, boolean isAdmin, RedirectAttributes redirectAttributes) {
        List<User> users = userRepository.findAll();
        for(User user : users) {
            if(name.equals(user.getName())) {
                redirectAttributes.addFlashAttribute("responseMessage", "Brugernavn allerede i brug, vælg et andet");
                return "redirect:/AdminPanel";
            }
        }
   
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(8));
        User user = new User();
        user.setName(name);
        user.setPassword(hashedPassword);
        user.setIsAdmin(isAdmin);
        userRepository.save(user);

        return "redirect:/AdminPanel";
    }

    public String deleteUserById(Long id){
        userRepository.deleteById(id);
        return "redirect:/AdminPanel";
    }  


    public String testUser(HttpServletResponse response) {
        User user = new User();
        user.setName("søren");

        String password = "test123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(8));

        user.setPassword(hashedPassword);
        userRepository.save(user);

        return "redirect:/login";
    }
}
