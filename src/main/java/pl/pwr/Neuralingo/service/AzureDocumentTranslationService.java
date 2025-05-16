package pl.pwr.Neuralingo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;



import java.util.List;

@Service
public class AzureDocumentTranslationService {

//    @Value("${azure.translator.endpoint}")
//    private String translatorEndpoint;
//
//    @Value("${azure.translator.apiKey}")
//    private String translatorApiKey;
//
//    @Value("${azure.translator.region}")
//    private String translatorRegion;
//
//    private final WebClient webClient;
//
//    public AzureDocumentTranslationService(WebClient.Builder webClientBuilder) {
//        this.webClient = webClientBuilder.build();
//    }
//
//    public ExtractedDocumentContentDto translate(ExtractedDocumentContentDto content, String targetLang) {
//        // 1. Zbuduj HTML z ExtractedDocumentContent
//        String html = buildHtml(content);
//
//        // 2. Wyślij do Azure Translatora
//        String translatedHtml = translateHtml(html, targetLang);
//
//        // 3. Utwórz nowy ExtractedDocumentContent ze zaktualizowanym tekstem
//        ExtractedDocumentContent translatedContent = new ExtractedDocumentContent(
//                translatedHtml,                  // text
//                content.language(),              // language
//                content.paragraphs(),            // paragraphs
//                content.tables(),                // tables
//                content.styles(),                // styles
//                content.sections(),              // sections
//                content.keyValuePairs(),         // keyValuePairs
//                content.entities(),              // entities
//                content.lines(),                 // lines
//                content.words()                  // words
//        );
//
//        return translatedContent;
//    }
//
//    private String buildHtml(ExtractedDocumentContent content) {
//        StringBuilder html = new StringBuilder();
//
//        // Dodaj paragrafy
//        List<?> paragraphs = content.paragraphs();
//        if (paragraphs != null) {
//            for (Object para : paragraphs) {
//                html.append("<p>").append(para.toString()).append("</p>");
//            }
//        }
//
//if (content.tables() != null) {
//    html.append("<table border='1'>");
//    for (Table table : content.tables()) {
//        for (List<String> row : table.cells()) {   // Każdy wiersz
//            html.append("<tr>");
//            for (String cellContent : row) {       // Każda komórka w wierszu
//                html.append("<td>").append(cellContent).append("</td>");
//            }
//            html.append("</tr>");
//        }
//    }
//    html.append("</table>");
//}
//
//
//        return html.toString();
//    }
//
//    private String translateHtml(String html, String targetLang) {
//        String url = translatorEndpoint + "/translate?api-version=3.0&to=" + targetLang;
//
//        // Body w JSON
//        String body = "[{\"Text\":\"" + html.replace("\"", "\\\"") + "\"}]";
//
//        String response = webClient.post()
//                .uri(url)
//                .header("Ocp-Apim-Subscription-Key", translatorApiKey)
//                .header("Ocp-Apim-Subscription-Region", translatorRegion)
//                .header(HttpHeaders.CONTENT_TYPE, "application/json")
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//
//        // Uproszczone parsowanie — dla produkcji polecam Jackson/JsonPath
//        String translatedText = response.split("\"text\":\"")[1].split("\"")[0];
//
//        return translatedText;
//    }
}
