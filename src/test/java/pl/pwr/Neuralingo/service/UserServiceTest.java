package pl.pwr.Neuralingo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.pwr.Neuralingo.entity.User;
import pl.pwr.Neuralingo.repository.UserRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    private AutoCloseable closeable;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        sampleUser = new User();
        sampleUser.setId("123");
        sampleUser.setEmail("test@example.com");
        sampleUser.setPassword("encodedOldPassword");
        sampleUser.setNativeLanguage("en");
    }

    @Test
    void testGetUserById_UserExists() {
        when(userRepository.findById("123")).thenReturn(Optional.of(sampleUser));

        User result = userService.getUserById("123");

        assertNotNull(result);
        assertEquals("123", result.getId());
    }

    @Test
    void testGetUserById_UserNotFound() {
        when(userRepository.findById("456")).thenReturn(Optional.empty());

        User result = userService.getUserById("456");

        assertNull(result);
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<User> users = userService.getAllUsers();

        assertEquals(1, users.size());
    }

    @Test
    void testRegisterUser() {
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);

        User result = userService.registerUser(sampleUser);

        assertEquals(sampleUser.getEmail(), result.getEmail());
    }

    @Test
    void testEmailExists_True() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        assertTrue(userService.emailExists("test@example.com"));
    }

    @Test
    void testEmailExists_False() {
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.empty());

        assertFalse(userService.emailExists("other@example.com"));
    }

    @Test
    void testChangePassword_Success() {
        when(userRepository.findById("123")).thenReturn(Optional.of(sampleUser));
        when(passwordService.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordService.encodePassword("newPassword")).thenReturn("encodedNewPassword");

        userService.changePassword("123", "oldPassword", "newPassword");

        verify(userRepository).save(argThat(user -> user.getPassword().equals("encodedNewPassword")));
    }

    @Test
    void testChangePassword_InvalidOldPassword() {
        when(userRepository.findById("123")).thenReturn(Optional.of(sampleUser));
        when(passwordService.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                userService.changePassword("123", "wrongPassword", "newPassword"));
    }

    @Test
    void testChangePassword_UserNotFound() {
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userService.changePassword("999", "oldPassword", "newPassword"));
    }

    @Test
    void testFindByEmail_Found() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
    }

    @Test
    void testFindByEmail_NotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("missing@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteUser() {
        userService.deleteUser("123");

        verify(userRepository).deleteById("123");
    }

    @Test
    void testIsValidUser_Valid() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordService.matches("password", "encodedOldPassword")).thenReturn(true);

        assertTrue(userService.isValidUser("test@example.com", "password"));
    }

    @Test
    void testIsValidUser_InvalidPassword() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordService.matches("wrong", "encodedOldPassword")).thenReturn(false);

        assertFalse(userService.isValidUser("test@example.com", "wrong"));
    }

    @Test
    void testIsValidUser_UserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertFalse(userService.isValidUser("missing@example.com", "any"));
    }

    @Test
    void testExistsByEmail_True() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        assertTrue(userService.existsByEmail("test@example.com"));
    }

    @Test
    void testExistsByEmail_False() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertFalse(userService.existsByEmail("unknown@example.com"));
    }

    @Test
    void testGetDefaultLanguageByUserId_Found() {
        when(userRepository.findById("123")).thenReturn(Optional.of(sampleUser));

        String language = userService.getDefaultLanguageByUserId("123");

        assertEquals("en", language);
    }

    @Test
    void testGetDefaultLanguageByUserId_NotFound() {
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getDefaultLanguageByUserId("999"));
    }
}
