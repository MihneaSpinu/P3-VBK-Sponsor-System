package p3project.functions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import p3project.classes.User;
import p3project.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class LoginFunctionsTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFunctions userFunctions;

    @InjectMocks
    private LoginFunctions loginFunctions;

    @Test
    void confirmLogin_setsCookieAndRedirectsOnSuccess() {
        String hashed = BCrypt.hashpw("secret", BCrypt.gensalt(8));
        User user = new User();
        user.setId(1L);
        user.setPassword(hashed);
        when(userRepository.findByName("alice")).thenReturn(user);

        MockHttpServletResponse response = new MockHttpServletResponse();
        Model model = new ConcurrentModel();

        String result = loginFunctions.confirmLogin("alice", "secret", false, model, response);

        assertThat(result).isEqualTo("redirect:/homepage");
        assertThat(response.getHeader("Set-Cookie")).contains("token=");
    }

    @Test
    void confirmLogin_redirectsToLoginOnFailure() {
        User user = new User();
        user.setId(2L);
        user.setPassword(BCrypt.hashpw("secret", BCrypt.gensalt(8)));
        when(userRepository.findByName("bob")).thenReturn(user);

        HttpServletResponse response = new MockHttpServletResponse();
        Model model = new ConcurrentModel();

        String result = loginFunctions.confirmLogin("bob", "wrong", false, model, response);

        assertThat(result).isEqualTo("redirect:/login");
    }

    @Test
    void loginPage_redirectsWhenTokenValid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Model model = new ConcurrentModel();
        when(userFunctions.userHasValidToken(request)).thenReturn(true);

        String view = loginFunctions.loginPage(model, request);

        assertThat(view).isEqualTo("redirect:/homepage");
        verify(userFunctions).userHasValidToken(any(HttpServletRequest.class));
    }

    @Test
    void loginPage_returnsLoginWhenNoToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Model model = new ConcurrentModel();
        when(userFunctions.userHasValidToken(request)).thenReturn(false);

        String view = loginFunctions.loginPage(model, request);

        assertThat(view).isEqualTo("login");
    }

    @Test
    void logout_clearsCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new jakarta.servlet.http.Cookie("token", "abc"));

        String view = loginFunctions.logout(request, response);

        assertThat(view).isEqualTo("redirect:/login");
        assertThat(response.getCookies()).isNotEmpty();
    }
}
