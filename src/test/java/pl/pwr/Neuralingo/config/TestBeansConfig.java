package pl.pwr.Neuralingo.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import pl.pwr.Neuralingo.service.UserService;
import pl.pwr.Neuralingo.service.PasswordService;
import pl.pwr.Neuralingo.utils.JwtUtil;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestBeansConfig {

    @Bean
    public JwtUtil jwtUtil() {
        return mock(JwtUtil.class);
    }

    @Bean
    public UserService userService() {
        return mock(UserService.class);
    }

    @Bean
    public PasswordService passwordService() {
        return mock(PasswordService.class);
    }
}
