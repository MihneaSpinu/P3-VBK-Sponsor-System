package p3project.functions;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import p3project.classes.Token;
import p3project.classes.User;
import p3project.repositories.UserRepository;

@Component
public class LoginFunctions {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFunctions uF;

    
    public String confirmLogin(String username,String password,boolean rememberMe, Model model, HttpServletResponse response) {
        User user = userRepository.findByName(username);
        if(user == null) return "redirect:/login";

        if(BCrypt.checkpw(password, user.getPassword())) {
            String id = user.getId().toString();
            Token token = Token.sign(id);
            String formattedToken = id + "." + token.getHash();
            String encodedToken = URLEncoder.encode(formattedToken, StandardCharsets.UTF_8);
            int time = rememberMe ? (60 * 60 * 24 * 365) : (60 * 60 * 24); // 1 år vs 1 dag
            ResponseCookie cookie = ResponseCookie.from("token", encodedToken)
                .httpOnly(true)  // javascript kan ikke ændre på cookien
                .secure(true)    // HTTPS only
                .path("/")       // Bruges til alle sider i domænet
                .maxAge(time)
                .build();
            response.addHeader("Set-Cookie", cookie.toString());
            return "redirect:/homepage";
        } else {
            return "redirect:/login";
        }
    }

    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, "token");
        if(cookie != null) {
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        return "redirect:/login";
    }

    public String loginPage(Model model, HttpServletRequest request) {
        if (uF.userHasValidToken(request)) {
            return "redirect:/homepage";
        }        
        return "login";
    }

}
