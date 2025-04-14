package pl.pwr.Neuralingo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.pwr.Neuralingo.config.TestBeansConfig;
import pl.pwr.Neuralingo.dto.auth.LoginRequestDTO;
import pl.pwr.Neuralingo.dto.auth.RegisterRequestDTO;
import pl.pwr.Neuralingo.utils.JwtUtil;
import pl.pwr.Neuralingo.service.UserService;
import pl.pwr.Neuralingo.service.PasswordService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AuthController.class)
@Import(TestBeansConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordService passwordService;

    private final String dummyEmail = "test@example.com";
    private final String dummyPassword = "secret";
    private final String dummyRefreshToken = "mocked-refresh-token";
    private final String newAccessToken = "mocked-access-token";
    private final String newRefreshToken = "mocked-new-refresh-token";

    @Test
    void shouldRegisterUser() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO(dummyEmail, dummyPassword, "Jan", "Kowalski", "PL");

        when(userService.existsByEmail(dto.email())).thenReturn(false);
        when(passwordService.encodePassword(dto.password())).thenReturn("encodedPassword");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully."));
    }

    @Test
    void shouldLoginSuccessfullyAndSetRefreshTokenCookie() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO(dummyEmail, dummyPassword);

        when(userService.isValidUser(dummyEmail, dummyPassword)).thenReturn(true);
        when(jwtUtil.generateAccessToken(dummyEmail)).thenReturn(newAccessToken);
        when(jwtUtil.generateRefreshToken(dummyEmail)).thenReturn(dummyRefreshToken);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(cookie().value("refresh_token", dummyRefreshToken));
    }

    @Test
    void shouldRefreshTokenAndReturnNewAccessToken() throws Exception {
        Cookie cookie = new Cookie("refresh_token", dummyRefreshToken);

        when(jwtUtil.validateToken(dummyRefreshToken)).thenReturn(true);
        when(jwtUtil.extractUserId(dummyRefreshToken)).thenReturn(dummyEmail);
        when(jwtUtil.generateAccessToken(dummyEmail)).thenReturn(newAccessToken);
        when(jwtUtil.generateRefreshToken(dummyEmail)).thenReturn(newRefreshToken);

        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(cookie().value("refresh_token", newRefreshToken))
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        System.out.println("RESPONSE BODY:\n" + actualResponseBody);

        String expectedJson = "{\"accessToken\":\"" + newAccessToken + "\"}";
        System.out.println("EXPECTED BODY:\n" + expectedJson);

        assert actualResponseBody.equals(expectedJson) : "Response body does not match expected!";
    }
}
