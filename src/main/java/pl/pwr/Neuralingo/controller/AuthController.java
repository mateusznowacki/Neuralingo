package pl.pwr.Neuralingo.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pwr.Neuralingo.dto.auth.AccessTokenDTO;
import pl.pwr.Neuralingo.dto.auth.LoginRequestDTO;
import pl.pwr.Neuralingo.dto.auth.RegisterRequestDTO;
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
    private UserService userService;

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO dto) {
        if (userService.existsByEmail(dto.email())) {
            return ResponseEntity.status(400).body("User with this email already exists.");
        }

        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(passwordService.encodePassword(dto.password()));
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

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<AccessTokenDTO> login(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        String email = loginRequestDTO.email();
        String password = loginRequestDTO.password();

        if (userService.isValidUser(email, password)) {
            String accessToken = jwtUtil.generateAccessToken(email);
            String refreshToken = jwtUtil.generateRefreshToken(email);

            addCookieToResponse("refresh_token", refreshToken, response);

            return ResponseEntity.ok(new AccessTokenDTO(accessToken));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // REFRESH
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenDTO> refresh(
            @CookieValue(value = "refresh_token", defaultValue = "") String refreshToken,
            HttpServletResponse response) {

        if (jwtUtil.validateToken(refreshToken)) {
            String userId = jwtUtil.extractUserId(refreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(userId);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId);

            addCookieToResponse("refresh_token", newRefreshToken, response);

            return ResponseEntity.ok(new AccessTokenDTO(newAccessToken));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // LOGOUT – usuwa refresh_token
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // ➜ USTAW NA true W PRODUKCJI
        cookie.setPath("/");
        cookie.setMaxAge(0); // usuwa ciasteczko
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    // Ustawienie ciasteczka
    private void addCookieToResponse(String cookieName, String token, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // ➜ ZMIEŃ NA `true` w środowisku produkcyjnym!
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 3600); // 7 dni
        response.addCookie(cookie);
    }
}
