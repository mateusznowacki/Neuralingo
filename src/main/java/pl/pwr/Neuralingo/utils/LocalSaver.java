package pl.pwr.Neuralingo.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class LocalSaver {

    /**
     * Zapisuje plik jako bajty do katalogu tymczasowego, np. src/main/resources/tmp/
     *
     * @param fileContent zawartość pliku w bajtach
     * @param docId       ID dokumentu, używane jako nazwa pliku
     * @param fileType    typ MIME (np. application/pdf)
     */
    public void saveToLocalTempFile(byte[] fileContent, String docId, String fileType) {
        try {
            String extension = resolveExtensionFromMimeType(fileType); // np. "pdf"
            File dir = new File("src/main/resources/tmp/");
            if (!dir.exists()) dir.mkdirs();

            File localFile = new File(dir, docId + "." + extension);
            try (FileOutputStream fos = new FileOutputStream(localFile)) {
                fos.write(fileContent);
            }

        } catch (IOException e) {
            throw new RuntimeException("Błąd zapisu pliku lokalnego", e);
        }
    }

    /**
     * Zwraca rozszerzenie pliku na podstawie typu MIME.
     *
     * @param mimeType typ MIME (np. "application/pdf")
     * @return rozszerzenie pliku (np. "pdf")
     */
    private String resolveExtensionFromMimeType(String mimeType) {
        return switch (mimeType) {
            case "application/pdf" -> "pdf";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx";
            case "application/msword" -> "doc";
            default -> "bin";
        };
    }
}
