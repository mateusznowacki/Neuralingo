package pl.pwr.Neuralingo.translation.word;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.TranslatedText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WordTextReplacer {

    public File replaceParagraphs(File originalFile,
                                  ExtractedText original,
                                  TranslatedText translated) throws IOException {

        var translatedMap = translated.paragraphs.stream()
                .collect(Collectors.toMap(p -> p.index, p -> p.text));

        // wygeneruj ścieżkę do nowego pliku
        String outputPath = originalFile.getAbsolutePath().replace(".docx", "") + "_translated.docx";
        File outputFile = new File(outputPath);

        try (FileInputStream fis = new FileInputStream(originalFile);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();

            for (int i = 0; i < paragraphs.size(); i++) {
                if (translatedMap.containsKey(i)) {
                    XWPFParagraph para = paragraphs.get(i);
                    String newText = translatedMap.get(i);

                    List<XWPFRun> runs = para.getRuns();
                    if (!runs.isEmpty()) {
                        runs.get(0).setText(newText, 0);
                        for (int r = 1; r < runs.size(); r++) {
                            runs.get(r).setText("", 0);
                        }
                    }
                }
            }

            // zapisz do nowego pliku
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                document.write(out);
            }
        }

        return outputFile;
    }
}
