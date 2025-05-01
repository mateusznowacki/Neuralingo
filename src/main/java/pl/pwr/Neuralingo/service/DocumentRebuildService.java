package pl.pwr.Neuralingo.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.springframework.stereotype.Service;
import pl.pwr.Neuralingo.dto.docContent.*;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class DocumentRebuildService {

    public String buildHtml(ExtractedDocumentContent content) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");

        // Paragrafy
        if (content.paragraphs() != null) {
            for (Paragraph para : content.paragraphs()) {
                html.append("<p>").append(para.content()).append("</p>");
            }
        }

        // Tabele
        if (content.tables() != null) {
            for (Table table : content.tables()) {
                html.append("<table border='1'>");
                for (List<String> row : table.cells()) {
                    html.append("<tr>");
                    for (String cell : row) {
                        html.append("<td>").append(cell).append("</td>");
                    }
                    html.append("</tr>");
                }
                html.append("</table>");
            }
        }

        html.append("</body></html>");
        return html.toString();
    }

    public void saveHtmlAsPdf(String html, Path outputPath) throws Exception {
        try (OutputStream os = Files.newOutputStream(outputPath)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
        }
    }

    public void saveHtmlAsDocx(String html, Path outputPath) throws Exception {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        XHTMLImporterImpl importer = new XHTMLImporterImpl(wordMLPackage);
        wordMLPackage.getMainDocumentPart().getContent().addAll(importer.convert(html, null));
        wordMLPackage.save(outputPath.toFile());
    }
}
