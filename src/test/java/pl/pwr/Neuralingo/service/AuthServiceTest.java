package pl.pwr.Neuralingo.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.pwr.Neuralingo.dto.auth.AccessTokenDTO;
import pl.pwr.Neuralingo.dto.auth.LoginRequestDTO;
import pl.pwr.Neuralingo.dto.auth.RegisterRequestDTO;
import pl.pwr.Neuralingo.entity.User;
import pl.pwr.Neuralingo.enums.Role;
import pl.pwr.Neuralingo.utils.JwtUtil;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private PasswordService passwordService;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailExists() {
        RegisterRequestDTO dto = new RegisterRequestDTO("test@example.com", "pass", "John", "Doe", "en");
        when(userService.existsByEmail(dto.email())).thenReturn(true);

        ResponseEntity<AccessTokenDTO> result = authService.register(dto, response);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());
        verify(userService, never()).registerUser(any());
        verify(response, never()).addCookie(any());
    }

    @Test
    void register_shouldRegisterUserAndReturnToken_whenEmailNotExists() {
        RegisterRequestDTO dto = new RegisterRequestDTO("test@example.com", "pass", "John", "Doe", "en");
        when(userService.existsByEmail(dto.email())).thenReturn(false);
        when(passwordService.encodePassword(dto.password())).thenReturn("encodedPass");
        doNothing().when(userService).registerUser(any());

        User user = new User();
        user.setId("userId123");
        // We need to capture the user passed to registerUser and mock jwtUtil with this id.
        doAnswer(invocation -> {
            User argUser = invocation.getArgument(0);
            argUser.setId("userId123"); // set id after save (simulate)
            return null;
        }).when(userService).registerUser(any());

        when(jwtUtil.generateAccessToken("userId123")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("userId123")).thenReturn("refresh-token");

        doNothing().when(response).addCookie(any());

        ResponseEntity<AccessTokenDTO> result = authService.register(dto, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("access-token", result.getBody().accessToken());

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals("refresh_token", cookie.getName());
        assertEquals("refresh-token", cookie.getValue());
    }

    @Test
    void login_shouldReturnUnauthorized_whenInvalidUser() {
        LoginRequestDTO loginDTO = new LoginRequestDTO("test@example.com", "wrongPass");

        when(userService.isValidUser(loginDTO.email(), loginDTO.password())).thenReturn(false);

        ResponseEntity<AccessTokenDTO> responseEntity = authService.login(loginDTO, response);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        verify(response, never()).addCookie(any());
    }

    @Test
    void login_shouldReturnTokens_whenValidUser() {
        LoginRequestDTO loginDTO = new LoginRequestDTO("test@example.com", "correctPass");
        User user = new User();
        user.setId("userId123");

        when(userService.isValidUser(loginDTO.email(), loginDTO.password())).thenReturn(true);
        when(userService.findByEmail(loginDTO.email())).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken("userId123")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("userId123")).thenReturn("refresh-token");
        doNothing().when(response).addCookie(any());

        ResponseEntity<AccessTokenDTO> responseEntity = authService.login(loginDTO, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("access-token", responseEntity.getBody().accessToken());

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals("refresh_token", cookie.getName());
        assertEquals("refresh-token", cookie.getValue());
    }

    @Test
    void refresh_shouldReturnUnauthorized_whenInvalidToken() {
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        ResponseEntity<AccessTokenDTO> responseEntity = authService.refresh("invalid-token", response);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        verify(response, never()).addCookie(any());
    }

    @Test
    void refresh_shouldReturnNewTokens_whenValidToken() {
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.extractUserId("valid-token")).thenReturn("userId123");
        when(jwtUtil.generateAccessToken("userId123")).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken("userId123")).thenReturn("new-refresh-token");
        doNothing().when(response).addCookie(any());

        ResponseEntity<AccessTokenDTO> responseEntity = authService.refresh("valid-token", response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("new-access-token", responseEntity.getBody().accessToken());

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals("refresh_token", cookie.getName());
        assertEquals("new-refresh-token", cookie.getValue());
    }

    @Test
    void logout_shouldAddExpiredCookie() {
        doNothing().when(response).addCookie(any());

        ResponseEntity<Void> responseEntity = authService.logout(response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();

        assertEquals("refresh_token", cookie.getName());
        assertNull(cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
    }
}
