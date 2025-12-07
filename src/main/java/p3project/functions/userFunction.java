package p3project.functions;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Token;
import p3project.classes.User;

import p3project.repositories.UserRepository;

public class userFunction {
    @Autowired
    private UserRepository userRepository;

    public boolean userHasValidToken(HttpServletRequest request) {
        String cookieId;
        String cookieHash;
        try {
            String[] parsedCookie = parseCookie(request);
            cookieId = parsedCookie[0];
            cookieHash = parsedCookie[1];
        } catch (RuntimeException e) {
            return false;
        }
        Token token = Token.sign(cookieId);
        return token.verify(cookieHash);
    }

    public String[] parseCookie(HttpServletRequest request) throws RuntimeException { // find anden exception?
        Cookie cookie = WebUtils.getCookie(request, "token");
        if (cookie == null)
            throw new RuntimeException();

        String decodedToken = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
        String[] splitCookie = decodedToken.split("\\.");
        if (splitCookie.length != 2)
            throw new RuntimeException();

        return splitCookie;
    }

    public User getUserFromToken(HttpServletRequest request) throws RuntimeException {
        String[] parsedCookie = parseCookie(request);
        int userId = Integer.parseInt(parsedCookie[0]);
        User user = userRepository.findById((userId)).orElse(null);
        if (user == null)
            throw new RuntimeException("Unable to retrieve username");
        return user;
    }
}