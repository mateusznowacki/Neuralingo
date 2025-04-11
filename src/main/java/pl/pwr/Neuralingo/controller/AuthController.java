package pl.pwr.Neuralingo.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pwr.Neuralingo.dto.auth.LoginRequestDTO;
import pl.pwr.Neuralingo.dto.auth.RegisterRequestDTO;
import pl.pwr.Neuralingo.dto.auth.TokenResponseDTO;
import pl.pwr.Neuralingo.entity.User;
import pl.pwr.Neuralingo.enums.ROLE;
import pl.pwr.Neuralingo.service.PasswordService;
import pl.pwr.Neuralingo.service.UserService;
import pl.pwr.Neuralingo.utils.JwtUtil;

import java.time.LocalDateTime;
import java.util.Collections;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;  // Wstrzykujemy UserService

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO dto) {
        if (userService.existsByEmail(dto.email())) {
            return ResponseEntity.status(400).body("User with this email already exists.");
        }

        // Utwórz obiekt User z danych DTO
        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(passwordService.encodePassword(dto.password())); // haszowanie hasła!
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setNativeLanguage(dto.nativeLanguage());
        user.setRole(ROLE.USER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setRecentDocumentIds(Collections.emptyList());

        userService.registerUser(user);

        return ResponseEntity.ok("User registered successfully.");
    }


    // Endpoint do logowania i generowania tokenów
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        String email = loginRequestDTO.email();
        String password = loginRequestDTO.password();

        // Weryfikacja użytkownika
        if (userService.isValidUser(email, password)) {
            // Generowanie tokenów
            String accessToken = jwtUtil.generateAccessToken(email);
            String refreshToken = jwtUtil.generateRefreshToken(email);

            // Dodajemy tokeny do ciasteczek
            addCookieToResponse("access_token", accessToken, response);
            addCookieToResponse("refresh_token", refreshToken, response);

            // Zwracamy odpowiedź
            return ResponseEntity.ok(new TokenResponseDTO(accessToken, refreshToken));
        }

        return ResponseEntity.status(401).body(new TokenResponseDTO("Invalid credentials", null)); // Zwracamy błąd przy złych danych logowania
    }

    // Endpoint do odświeżania tokenów
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@CookieValue(value = "refresh_token", defaultValue = "") String refreshToken, HttpServletResponse response) {
        if (jwtUtil.validateToken(refreshToken)) {
            // Jeśli refresh token jest poprawny, generujemy nowy access token i refresh token
            String userId = jwtUtil.extractUserId(refreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(userId);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId);

            // Dodajemy nowe tokeny do ciasteczek
            addCookieToResponse("access_token", newAccessToken, response);
            addCookieToResponse("refresh_token", newRefreshToken, response);

            return ResponseEntity.ok(new TokenResponseDTO(newAccessToken, newRefreshToken)); // Zwracamy nowe tokeny
        }

        return ResponseEntity.status(401).body(new TokenResponseDTO("Invalid or expired refresh token", null));
    }

    // Pomocnicza metoda do tworzenia ciasteczek
    private void addCookieToResponse(String cookieName, String token, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true); // Zapobiega dostępowi do ciasteczka przez JavaScript
        cookie.setSecure(true);   // Ciasteczko będzie wysyłane tylko przez HTTPS
        cookie.setPath("/");      // Ciasteczko jest dostępne w całej aplikacji
        cookie.setMaxAge(3600);   // Opcjonalnie: ustawienie maksymalnego czasu życia ciasteczka (w sekundach)

        response.addCookie(cookie); // Dodajemy ciasteczko do odpowiedzi
    }

}
