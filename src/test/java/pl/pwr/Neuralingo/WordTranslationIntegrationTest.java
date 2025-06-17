package pl.pwr.Neuralingo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;
import pl.pwr.Neuralingo.service.AzureDocumentTranslationService;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@SpringBootTest
@AutoConfigureMockMvc
public class WordTranslationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AzureDocumentTranslationService azureService;

    private File translatedFile;

    @AfterEach
    void cleanup() throws Exception {
        if (translatedFile != null && translatedFile.exists()) {
            Files.deleteIfExists(translatedFile.toPath());
        }
    }

    @Test
    void shouldTranslateWordFileAndReturnTranslatedFilePath() throws Exception {
        // 1. Load test Word document
        File inputFile = new ClassPathResource("sample.docx").getFile();
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            MockMultipartFile mockFile = new MockMultipartFile(
                    "file",
                    "sample.docx",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    fis
            );

            // 2. Mock Azure Translation Service
            when(azureService.translate(any(), eq("pl")))
                    .thenReturn(new TranslatedText(List.of(
                            new Paragraph(0, "Cześć"),
                            new Paragraph(1, "Świecie")
                    )));

            // 3. Perform request
            String response = mockMvc.perform(multipart("/translate")
                            .file(mockFile)
                            .param("language", "pl"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // 4. Store path for cleanup
            translatedFile = new File(response);

        }
    }
}
