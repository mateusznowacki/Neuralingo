package pl.pwr.Neuralingo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordServiceTest {

    private PasswordEncoder passwordEncoder;
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordEncoder = mock(PasswordEncoder.class);
        passwordService = new PasswordService(passwordEncoder);
    }

    @Test
    void encodePassword_shouldReturnEncodedPassword() {
        // Given
        String rawPassword = "mySecret";
        String encodedPassword = "$2a$10$somethinghashed";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // When
        String result = passwordService.encodePassword(rawPassword);

        // Then
        assertEquals(encodedPassword, result);
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    void matches_shouldReturnTrueIfPasswordsMatch() {
        // Given
        String rawPassword = "password";
        String encodedPassword = "$2a$10$hashedpassword";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // When
        boolean matches = passwordService.matches(rawPassword, encodedPassword);

        // Then
        assertTrue(matches);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void matches_shouldReturnFalseIfPasswordsDoNotMatch() {
        // Given
        String rawPassword = "password";
        String encodedPassword = "$2a$10$hashedpassword";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // When
        boolean matches = passwordService.matches(rawPassword, encodedPassword);

        // Then
        assertFalse(matches);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }
}
