package pl.pwr.Neuralingo.entity;

import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.enums.Role;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldSetAndGetAllFields() {
        // Given
        User user = new User();
        String id = "user123";
        String email = "test@example.com";
        String password = "samplePassword";
        String firstName = "Jan";
        String lastName = "Kowalski";
        Role role = Role.USER;
        boolean active = true;
        String profileImageUrl = "http://example.com/image.jpg";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime lastLogin = LocalDateTime.now().minusDays(1);
        String nativeLanguage = "en";
        List<String> recentDocs = Arrays.asList("doc1", "doc2");

        // When
        user.setId(id);
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setActive(active);
        user.setProfileImageUrl(profileImageUrl);
        user.setCreatedAt(createdAt);
        user.setLastLogin(lastLogin);
        user.setNativeLanguage(nativeLanguage);
        user.setRecentDocumentIds(recentDocs);

        // Then
        assertEquals(id, user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(role, user.getRole());
        assertTrue(user.isActive());
        assertEquals(profileImageUrl, user.getProfileImageUrl());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(lastLogin, user.getLastLogin());
        assertEquals(nativeLanguage, user.getNativeLanguage());
        assertEquals(recentDocs, user.getRecentDocumentIds());
    }

    @Test
    void defaultConstructorShouldCreateNonNullUser() {
        // When
        User user = new User();

        // Then
        assertNotNull(user);
    }
}
