package p3project.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import p3project.classes.User;
import p3project.repositories.UserRepository;
import p3project.MainController;

/**
 * Web layer tests for MainController
 * @WebMvcTest focuses only on the web layer (controllers)
 */
@WebMvcTest(MainController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MainControllerTest {

    @Autowired
    private MockMvc mockMvc; // For simulating HTTP requests
    
    @MockBean
    private UserRepository userRepository; // Mock the repository
    
    @Test
    public void testHomeRedirect() throws Exception {
        // Test that home page redirects to /users
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
    }
    
    @Test
    public void testShowUsersPage() throws Exception {
        // Arrange - Mock repository to return test users
        User user1 = new User();
        user1.setName("John");
        user1.setEmail("john@example.com");
        
        User user2 = new User();
        user2.setName("Jane");
        user2.setEmail("jane@example.com");
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        
        // Act & Assert - Test the /users endpoint
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users"));
    }
    
    @Test
    public void testAddUserAPI() throws Exception {
        // Test the JSON API endpoint for adding users
        mockMvc.perform(post("/demo/add")
                .param("name", "Test User")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Saved"));
        
        // Verify that repository save was called
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    public void testGetAllUsersAPI() throws Exception {
        // Arrange - Mock repository
        User user = new User();
        user.setName("API User");
        user.setEmail("api@example.com");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        
        // Act & Assert - Test JSON API
        mockMvc.perform(get("/demo/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
    
    @Test
    public void testAddUserFormSubmission() throws Exception {
        // Test form submission from web page
        mockMvc.perform(post("/users/add")
                .param("name", "Form User")
                .param("email", "form@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        
        // Verify repository was called
        verify(userRepository, times(1)).save(any(User.class));
    }
}