package pl.pwr.Neuralingo.controller;

import org.junit.jupiter.api.Test;
import pl.pwr.Neuralingo.dto.DocumentDTO;
import pl.pwr.Neuralingo.entity.DocumentEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentControllerTest {

    @Test
    void shouldMapFromDocumentEntity() {
        // Given
        DocumentEntity entity = DocumentEntity.builder()
                .id("123")
                .ownerId("user1")
                .originalFilename("file.pdf")
                .fileType("application/pdf")
                .originalStoragePath("/storage/original/file.pdf")
                .translatedFilename("file_translated.pdf")
                .translatedStoragePath("/storage/translated/file_translated.pdf")
                .sourceLanguage("en")
                .targetLanguage("pl")
                .build();

        // When
        DocumentDTO dto = DocumentDTO.from(entity);

        // Then
        assertEquals("123", dto.id());
        assertEquals("user1", dto.ownerId());
        assertEquals("file.pdf", dto.originalFilename());
        assertEquals("application/pdf", dto.fileType());
        assertEquals("/storage/original/file.pdf", dto.originalStoragePath());
        assertEquals("file_translated.pdf", dto.translatedFilename());
        assertEquals("/storage/translated/file_translated.pdf", dto.translatedStoragePath());
        assertEquals("en", dto.sourceLanguage());
        assertEquals("pl", dto.targetLanguage());
    }
}
