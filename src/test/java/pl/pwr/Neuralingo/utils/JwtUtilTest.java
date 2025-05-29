import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.pwr.Neuralingo.utils.JwtUtil;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Use a 32-byte secret key (256 bits) for HS256
    private final String secretKey = "0123456789ABCDEF0123456789ABCDEF";

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();

        // Generate a strong 256-bit key
        SecretKey secretKeyObj = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // Encode the key as Base64 string
        String base64Key = Base64.getEncoder().encodeToString(secretKeyObj.getEncoded());

        // Inject the Base64-encoded key string into the secretKey field
        Field secretKeyField = JwtUtil.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtUtil, base64Key);
    }

    @Test
    void testGenerateAndValidateAccessToken() {
        String userId = "test-user";
        String token = jwtUtil.generateAccessToken(userId);
        assertNotNull(token);

        assertTrue(jwtUtil.validateToken(token));

        Claims claims = jwtUtil.extractClaims(token);
        assertEquals(userId, claims.getSubject());
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void testIsTokenExpired_FalseForFreshToken() {
        String userId = "user";
        String token = jwtUtil.generateAccessToken(userId);
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void testIsTokenExpired_TrueForExpiredToken() throws InterruptedException {
        JwtUtil shortExpiryJwtUtil = new JwtUtil();

        // Generate a secure random key for HS256
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // Convert key to base64 string if your JwtUtil expects String
        String base64Key = Encoders.BASE64.encode(key.getEncoded());

        // Inject the base64Key as the secretKey in JwtUtil
        ReflectionTestUtils.setField(shortExpiryJwtUtil, "secretKey", base64Key);

        // Set expiration time to 1 millisecond before generating token
        ReflectionTestUtils.setField(shortExpiryJwtUtil, "jwtExpirationInMs", 1L);

        // Generate token
        String token = shortExpiryJwtUtil.generateAccessToken("user");

        // Wait longer than expiration to ensure token expires
        Thread.sleep(10);

        // Check that token is expired
        assertTrue(shortExpiryJwtUtil.isTokenExpired(token));
    }


    @Test
    void testExtractUserId() {
        String userId = "user123";
        String token = jwtUtil.generateAccessToken(userId);
        assertEquals(userId, jwtUtil.extractUserId(token));
    }

    @Test
    void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.token.value";

        Exception exception = assertThrows(Exception.class, () -> {
            jwtUtil.validateToken(invalidToken);
        });

        assertTrue(exception.getMessage().contains("JWT"));
    }
}
