package pl.pwr.Neuralingo.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.pwr.Neuralingo.dto.docContent.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class AzureDocumentIntelligenceService {

    @Value("${azure.document.endpoint}")
    private String endpoint;

    @Value("${azure.document.apiKey}")
    private String apiKey;

    private static final String MODEL = "prebuilt-document";
    private static final String API_VERSION = "2023-07-31";

    public ExtractedDocumentContent analyzeDocument(byte[] fileBytes, String fileType) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            // 1. submit
            HttpRequest submit = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + "/formrecognizer/documentModels/" + MODEL + ":analyze?api-version=" + API_VERSION))
                    .header("Content-Type", fileType)
                    .header("Ocp-Apim-Subscription-Key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                    .build();
            HttpResponse<Void> subResp = client.send(submit, HttpResponse.BodyHandlers.discarding());
            String opLocation = subResp.headers().firstValue("operation-location")
                    .orElseThrow(() -> new RuntimeException("Brak operation-location"));

            // 2. poll
            JSONObject root = pollForResult(client, opLocation);
            JSONObject analyze = root.getJSONObject("analyzeResult");

            String text = analyze.optString("content", "");
            String language = analyze.optString("detectedLanguage", "unknown");

            List<Paragraph> paragraphs = parseParagraphs(analyze);
            List<Table> tables = parseTables(analyze);
            List<KeyValuePair> kvps = parseKeyValuePairs(analyze);
            List<NamedEntity> entities = parseEntities(analyze);
            List<Line> lines = new ArrayList<>();
            List<Word> words = new ArrayList<>();
            parsePages(analyze, lines, words);

            return new ExtractedDocumentContent(text, language, paragraphs, tables,
                    new ArrayList<>(), new ArrayList<>(), kvps, entities, lines, words);
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas przetwarzania dokumentu przez Azure", e);
        }
    }

    /* ------------------------ polling ------------------------ */
    private JSONObject pollForResult(HttpClient client, String url) throws Exception {
        for (int i = 0; i < 20; i++) { // 20*1.5s ≈ 30 s
            HttpResponse<String> r = client.send(HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Ocp-Apim-Subscription-Key", apiKey)
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            JSONObject o = new JSONObject(r.body());
            switch (o.optString("status")) {
                case "succeeded" -> { return o; }
                case "failed" -> throw new RuntimeException("Azure zwrócił status failed");
            }
            Thread.sleep(1500);
        }
        throw new RuntimeException("Przekroczono czas oczekiwania na wynik");
    }

    /* ------------------------ parsing helpers ------------------------ */

    private List<Paragraph> parseParagraphs(JSONObject analyze) {
        List<Paragraph> out = new ArrayList<>();
        JSONArray arr = analyze.optJSONArray("paragraphs");
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject p = arr.getJSONObject(i);
            JSONObject region = firstRegion(p);
            out.add(new Paragraph(p.getString("content"), page(region), toBox(region)));
        }
        return out;
    }

    private List<Table> parseTables(JSONObject analyze) {
        List<Table> list = new ArrayList<>();
        JSONArray arr = analyze.optJSONArray("tables");
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject t = arr.getJSONObject(i);
            int rows = t.getInt("rowCount");
            List<List<String>> cells = new ArrayList<>();
            for (int r = 0; r < rows; r++) cells.add(new ArrayList<>());
            JSONArray cellArr = t.getJSONArray("cells");
            for (int c = 0; c < cellArr.length(); c++) {
                JSONObject cell = cellArr.getJSONObject(c);
                int row = cell.getInt("rowIndex");
                cells.get(row).add(cell.getString("content"));
            }
            JSONObject region = firstRegion(t);
            list.add(new Table(page(region), cells, toBox(region)));
        }
        return list;
    }

    private List<KeyValuePair> parseKeyValuePairs(JSONObject analyze) {
        List<KeyValuePair> list = new ArrayList<>();
        JSONArray arr = analyze.optJSONArray("keyValuePairs");
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject kv = arr.getJSONObject(i);
            JSONObject keyObj = kv.getJSONObject("key");
            JSONObject valObj = kv.getJSONObject("value");

            String key = keyObj.getString("content");
            String value = valObj.optString("content", "");

            JSONObject keyReg = firstRegion(keyObj);
            JSONObject valReg = firstRegion(valObj);

            int page = page(keyReg != null ? keyReg : valReg);
            list.add(new KeyValuePair(key, value, page, toBox(keyReg), toBox(valReg)));
        }
        return list;
    }

    private List<NamedEntity> parseEntities(JSONObject analyze) {
        List<NamedEntity> list = new ArrayList<>();
        JSONArray arr = analyze.optJSONArray("entities");
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject e = arr.getJSONObject(i);
            JSONObject region = firstRegion(e);
            list.add(new NamedEntity(
                    e.getString("category"),
                    e.optString("subCategory", ""),
                    e.getString("content"),
                    page(region),
                    toBox(region),
                    (float) e.optDouble("confidence", 1.0)));
        }
        return list;
    }

    private void parsePages(JSONObject analyze, List<Line> lines, List<Word> words) {
        JSONArray pages = analyze.optJSONArray("pages");
        if (pages == null) return;
        for (int i = 0; i < pages.length(); i++) {
            JSONObject p = pages.getJSONObject(i);
            int pageNo = p.getInt("pageNumber");
            JSONArray lineArr = p.optJSONArray("lines");
            if (lineArr != null) {
                for (int l = 0; l < lineArr.length(); l++) {
                    JSONObject ln = lineArr.getJSONObject(l);
                    lines.add(new Line(ln.getString("content"), pageNo, toSimpleBox(ln), (float) ln.optDouble("confidence", 1.0)));
                }
            }
            JSONArray wordArr = p.optJSONArray("words");
            if (wordArr != null) {
                for (int w = 0; w < wordArr.length(); w++) {
                    JSONObject wd = wordArr.getJSONObject(w);
                    words.add(new Word(wd.getString("content"), pageNo, toSimpleBox(wd), (float) wd.optDouble("confidence", 1.0)));
                }
            }
        }
    }

    /* --------------- util ---------------- */
    private JSONObject firstRegion(JSONObject obj) {
        JSONArray br = obj.optJSONArray("boundingRegions");
        return br != null && !br.isEmpty() ? br.getJSONObject(0) : null;
    }

    private int page(JSONObject region) { return region != null ? region.getInt("pageNumber") : 0; }

    private BoundingBox toBox(JSONObject region) {
        if (region == null || !region.has("polygon")) return null;
        JSONArray poly = region.getJSONArray("polygon");
        float left = (float) poly.getDouble(0);
        float top = (float) poly.getDouble(1);
        float width = (float) Math.abs(poly.getDouble(2) - poly.getDouble(0));
        float height = (float) Math.abs(poly.getDouble(5) - poly.getDouble(1));
        return new BoundingBox(top, left, width, height);
    }

    private BoundingBox toSimpleBox(JSONObject obj) {
        if (obj.has("polygon")) return toBox(obj);
        return new BoundingBox(0, 0, 0, 0);
    }
}
