//package pl.pwr.Neuralingo.config;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.web.servlet.MockMvc;
//import pl.pwr.Neuralingo.utils.JwtAuthenticationFilter;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class SecurityConfigTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    // Mock the JwtAuthenticationFilter to avoid needing a working JWT implementation in this test
//    @MockBean
//    private JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    @Test
//    void shouldPermitAuthEndpointsWithoutAuthentication() throws Exception {
//        mockMvc.perform(get("/api/auth/login"))
//                .andExpect(status().isOk()); // Or 404 if endpoint exists but no handler, still not 401
//    }
//
//    @Test
//    void shouldRejectAccessToProtectedEndpointWithoutAuthentication() throws Exception {
//        mockMvc.perform(get("/api/documents/123"))
//                .andExpect(status().isUnauthorized());
//    }
//}
