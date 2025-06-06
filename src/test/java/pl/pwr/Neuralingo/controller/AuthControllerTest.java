package pl.pwr.Neuralingo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.pwr.Neuralingo.dto.auth.AccessTokenDTO;
import pl.pwr.Neuralingo.dto.auth.LoginRequestDTO;
import pl.pwr.Neuralingo.dto.auth.RegisterRequestDTO;
import pl.pwr.Neuralingo.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "test@example.com",
                "testuser",
                "securePassword123",
                "Test",
                "User"
        );

        AccessTokenDTO tokenDTO = new AccessTokenDTO("accessToken");

        when(authService.register(dto, response)).thenReturn(new ResponseEntity<>(tokenDTO, HttpStatus.CREATED));

        ResponseEntity<AccessTokenDTO> result = authController.register(dto, response);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(tokenDTO, result.getBody());
        verify(authService).register(dto, response);
    }


    @Test
    void testLogin() {
        LoginRequestDTO dto = new LoginRequestDTO("testuser", "securePassword123");
        AccessTokenDTO tokenDTO = new AccessTokenDTO("accessToken");

        when(authService.login(dto, response)).thenReturn(new ResponseEntity<>(tokenDTO, HttpStatus.OK));

        ResponseEntity<AccessTokenDTO> result = authController.login(dto, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(tokenDTO, result.getBody());
        verify(authService).login(dto, response);
    }

    @Test
    void testRefresh() {
        String refreshToken = "dummyRefreshToken";
        AccessTokenDTO tokenDTO = new AccessTokenDTO("newAccessToken");

        when(authService.refresh(refreshToken, response)).thenReturn(new ResponseEntity<>(tokenDTO, HttpStatus.OK));

        ResponseEntity<AccessTokenDTO> result = authController.refresh(refreshToken, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(tokenDTO, result.getBody());
        verify(authService).refresh(refreshToken, response);
    }

    @Test
    void testLogout() {
        ResponseEntity<Void> result = authController.logout(response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(authService).logout(response);
    }
}
