package pl.pwr.Neuralingo.config;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.cors.CorsConfigurationSource;

import pl.pwr.Neuralingo.utils.JwtAuthenticationFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CorsConfigurationSource corsConfigurationSource;

    @Test
    void passwordEncoderShouldEncodeAndMatch() {
        String rawPassword = "mySecret123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(encoded).isNotNull();
        assertThat(encoded).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void corsConfigurationShouldAllowAll() throws Exception {
        mockMvc.perform(options("/api/some-endpoint")
                        .header("Origin", "http://example.com")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "X-Custom-Header"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().string("Access-Control-Allow-Methods", Matchers.containsString("GET")))
                .andExpect(header().string("Access-Control-Allow-Headers", Matchers.containsString("X-Custom-Header")))
                .andExpect(header().string("Access-Control-Expose-Headers", Matchers.containsString("Authorization")));
    }



    @Test
    void authenticationManagerBeanExists() {
        assertNotNull(authenticationManager, "AuthenticationManager bean should be created");
    }
}
