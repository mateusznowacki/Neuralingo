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
import pl.pwr.Neuralingo.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

@PostMapping("/register")
public ResponseEntity<AccessTokenDTO> register(@RequestBody RegisterRequestDTO dto, HttpServletResponse response) {
    return authService.register(dto, response);
}

    @PostMapping("/login")
    public ResponseEntity<AccessTokenDTO> login(@RequestBody LoginRequestDTO dto, HttpServletResponse response) {
        return authService.login(dto, response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenDTO> refresh(
            @CookieValue(value = "refresh_token", defaultValue = "") String refreshToken,
            HttpServletResponse response) {
        return authService.refresh(refreshToken, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok().build();
    }
}
