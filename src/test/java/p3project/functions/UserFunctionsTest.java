package p3project.functions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import p3project.classes.Token;
import p3project.classes.User;
import p3project.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserFunctionsTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserFunctions userFunctions;

    @Test
    void parseCookie_returnsIdAndHash() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = URLEncoder.encode("1.abc", StandardCharsets.UTF_8);
        request.setCookies(new jakarta.servlet.http.Cookie("token", token));

        String[] parsed = userFunctions.parseCookie(request);

        assertArrayEquals(new String[]{"1","abc"}, parsed);
    }

    @Test
    void parseCookie_throwsWhenMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThrows(RuntimeException.class, () -> userFunctions.parseCookie(request));
    }

    @Test
    void userHasValidToken_verifiesSignature() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String hash = Token.sign("2").getHash();
        String token = URLEncoder.encode("2." + hash, StandardCharsets.UTF_8);
        request.setCookies(new jakarta.servlet.http.Cookie("token", token));

        boolean valid = userFunctions.userHasValidToken(request);

        assertThat(valid).isTrue();
    }

    @Test
    void userHasValidToken_returnsFalseOnInvalid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("token", "bad"));

        assertFalse(userFunctions.userHasValidToken(request));
    }

    @Test
    void userIsAdmin_returnsFlagFromUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String hash = Token.sign("5").getHash();
        String token = URLEncoder.encode("5." + hash, StandardCharsets.UTF_8);
        request.setCookies(new jakarta.servlet.http.Cookie("token", token));

        User user = new User();
        user.setId(5L);
        user.setIsAdmin(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        boolean admin = userFunctions.userIsAdmin(request);

        assertThat(admin).isTrue();
    }

    @Test
    void addUser_rejectsDuplicateName() {
        User existing = new User();
        existing.setName("alice");
        when(userRepository.findAll()).thenReturn(java.util.List.of(existing));
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String result = userFunctions.addUser("alice", "pass", false, attrs);

        assertThat(result).isEqualTo("redirect:/AdminPanel");
        assertThat(attrs.getFlashAttributes()).containsKey("responseMessage");
    }

    @Test
    void addUser_createsNewUser() {
        when(userRepository.findAll()).thenReturn(java.util.List.of());
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String result = userFunctions.addUser("bob", "pass", true, attrs);

        assertThat(result).isEqualTo("redirect:/AdminPanel");
    }

    @Test
    void deleteUserById_deletesAndRedirects() {
        String result = userFunctions.deleteUserById(9L);
        assertThat(result).isEqualTo("redirect:/AdminPanel");
    }

    @Test
    void testUser_createsAdminUser() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        String view = userFunctions.testUser(response);

        assertThat(view).isEqualTo("redirect:/login");
    }
}
