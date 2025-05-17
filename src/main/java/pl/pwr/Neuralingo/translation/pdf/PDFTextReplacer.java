package pl.pwr.Neuralingo.translation.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Component
public class PDFTextReplacer {


    public File replaceText(File originalHtml, ExtractedText original, TranslatedText translated) throws IOException {
        // Mapa z przetłumaczonymi tekstami
        Map<Integer, String> translationMap = new HashMap<>();
        for (TranslatedText.Paragraph p : translated.paragraphs) {
            translationMap.put(p.index, p.text);
        }

        // Wczytaj HTML jako tekst
        String html = Files.readString(originalHtml.toPath(), StandardCharsets.UTF_8);

        // Podmień każdy paragraf po data-index
        for (ExtractedText.Paragraph p : original.paragraphs) {
            String originalTag = String.format("<p data-index='%d'>%s</p>", p.index, p.text);
            String translatedText = translationMap.get(p.index);
            if (translatedText != null) {
                String translatedTag = String.format("<p data-index='%d'>%s</p>", p.index, translatedText);
                html = html.replace(originalTag, translatedTag);
            }
        }


        // Zapisz zmodyfikowany HTML do pliku tymczasowego
        File updatedHtmlFile = new File(originalHtml.getAbsolutePath().replace(".html", "_translated.html"));
        Files.writeString(updatedHtmlFile.toPath(), html, StandardCharsets.UTF_8);

        // Konwertuj HTML do PDF
        File outputPdf = new File(originalHtml.getAbsolutePath().replace(".html", "_translated.pdf"));
        try (OutputStream os = new FileOutputStream(outputPdf)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withUri(outputPdf.toURI().toString());
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            throw new IOException("Failed to generate PDF from HTML", e);
        }

        return outputPdf;
    }
}

