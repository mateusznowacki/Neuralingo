package pl.pwr.Neuralingo.translation.ocr;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.dto.document.content.ExtractedText;
import pl.pwr.Neuralingo.dto.document.content.Paragraph;
import pl.pwr.Neuralingo.service.AzureDocumentIntelligenceService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Component
public class DocumentExtractor {

    @Autowired
    private AzureDocumentIntelligenceService azure;

    public ExtractedText extractText(String html) {
        Document doc = Jsoup.parse(html);
        List<Paragraph> paragraphs = new ArrayList<>();
        int index = 0;

        // Extract <div class="text">
        Elements textDivs = doc.select("div.text");
        for (Element textDiv : textDivs) {
            String text = textDiv.text().trim();
            if (!text.isEmpty()) {
                paragraphs.add(new Paragraph(index++, text));
            }
        }

        // Extract <div class="cell"> (for table cells)
        Elements tableCells = doc.select("div.cell");
        for (Element cell : tableCells) {
            String text = cell.text().trim();
            if (!text.isEmpty()) {
                paragraphs.add(new Paragraph(index++, text));
            }
        }

        return new ExtractedText(paragraphs);
    }


    public String extractTextAsHtml(File inputFile) throws IOException {

        byte[] bytes = Files.readAllBytes(inputFile.toPath());
        JSONObject root = azure.analyzeLayout(bytes, detectFileType(inputFile));

        Map<Integer, Style> styleMap = Style.build(
                root.getJSONObject("analyzeResult").optJSONArray("styles"));

        StringBuilder html = new StringBuilder("""
                <!DOCTYPE html><html><head><meta charset="UTF-8">
                <style>
                   body{background:#fff;margin:0}
                   .page{position:relative;border:1px solid #000;margin:20px auto}
                   .text{position:absolute;white-space:pre}
                   .tableWrap{position:absolute}
                   .table{display:grid;border:1px solid #000;border-collapse:collapse;width:100%;height:100%}
                   .cell{border:1px solid #888;padding:4px;white-space:pre}
                </style></head><body>""");

        JSONArray pages = root.getJSONObject("analyzeResult").getJSONArray("pages");
        JSONArray tables = root.getJSONObject("analyzeResult").optJSONArray("tables");

        for (int p = 0; p < pages.length(); p++) {

            JSONObject page = pages.getJSONObject(p);
            float dpi = toPx(page.optString("unit", "pixel"));
            float pageWidthPx = (float) page.getDouble("width") * dpi;
            float pageHeightPx = (float) page.getDouble("height") * dpi;
            int pageNo = page.getInt("pageNumber");

            html.append("<div class=\"page\" style=\"width:")
                    .append(pageWidthPx).append("px;height:")
                    .append(pageHeightPx).append("px;\">");

            /* tabele */
            if (tables != null)
                for (int t = 0; t < tables.length(); t++) {
                    JSONObject tb = tables.getJSONObject(t);
                    if (tb.getJSONArray("boundingRegions").getJSONObject(0)
                            .getInt("pageNumber") == pageNo)
                        renderTable(html, tb, dpi, styleMap);
                }

            /* tekst */
            renderWords(html, page, dpi, styleMap, tables);

            html.append("</div>");
        }

        html.append("</body></html>");
        return html.toString();
    }



    /*────────────────────────── TEXT ──────────────────────────*/

    private void renderWords(StringBuilder html, JSONObject page, float dpi,
                             Map<Integer, Style> styleMap,
                             JSONArray allTables) {

        JSONArray words = page.optJSONArray("words");
        if (words == null) return;

        List<Box> masks = new ArrayList<>();
        if (allTables != null)
            for (int i = 0; i < allTables.length(); i++) {
                JSONObject tb = allTables.getJSONObject(i);
                if (tb.getJSONArray("boundingRegions").getJSONObject(0)
                        .getInt("pageNumber") == page.getInt("pageNumber"))
                    masks.add(new Box(tb.getJSONArray("boundingRegions").getJSONObject(0), dpi));
            }

        TreeMap<Float, List<JSONObject>> lines = new TreeMap<>();
        for (int i = 0; i < words.length(); i++) {
            JSONObject w = words.getJSONObject(i);
            if (Box.insideAny(w, dpi, masks)) continue;

            float y = (float) w.getJSONArray("polygon").getDouble(1) * dpi;
            Float key = lines.keySet().stream()
                    .filter(k -> Math.abs(k - y) <= 2).findFirst().orElse(y);
            lines.computeIfAbsent(key, k -> new ArrayList<>()).add(w);
        }

        for (List<JSONObject> ln : lines.values()) {
            ln.sort(Comparator.comparingDouble(
                    w -> w.getJSONArray("polygon").getDouble(0)));

            float left = Float.MAX_VALUE, right = 0, top = 0, bottom = 0;
            Set<Integer> styleIds = new HashSet<>();
            for (JSONObject w : ln) {
                styleIds.add(styleIdOf(w));
                JSONArray poly = w.getJSONArray("polygon");
                float x0 = (float) poly.getDouble(0) * dpi;
                float y0 = (float) poly.getDouble(1) * dpi;
                float x4 = (float) poly.getDouble(4) * dpi;
                float y5 = (float) poly.getDouble(5) * dpi;
                left = Math.min(left, x0);
                right = Math.max(right, x4);
                top = y0;
                bottom = Math.max(bottom, y5);
            }
            float width = right - left;
            float height = bottom - top;
            boolean uniform = styleIds.size() == 1;
            StringBuilder text = new StringBuilder();
            for (JSONObject w : ln) {
                if (text.length() > 0) text.append(' ');
                Style st = styleMap.getOrDefault(styleIdOf(w), Style.DEFAULT);
                text.append(uniform ? escape(w.getString("content"))
                        : st.wrap(escape(w.getString("content"))));
            }
            Style lineStyle = styleMap.getOrDefault(styleIds.iterator().next(),
                    Style.DEFAULT);
            html.append(String.format(
                    "<div class=\"text\" style=\"left:%.2fpx;top:%.2fpx;width:%.2fpx;height:%.2fpx;%s\">%s</div>",
                    left, top, width, height, uniform ? lineStyle.css() : "", text));
        }
    }

    private static int styleIdOf(JSONObject o) {
        JSONObject span = o.optJSONObject("span");
        if (span != null) return span.optInt("style", -1);
        JSONArray spans = o.optJSONArray("spans");
        return spans != null && spans.length() > 0
                ? spans.getJSONObject(0).optInt("style", -1)
                : -1;
    }

    /*───────────────────────── TABLES ─────────────────────────*/

    private void renderTable(StringBuilder html, JSONObject tb, float dpi,
                             Map<Integer, Style> styleMap) {

        Box box = new Box(tb.getJSONArray("boundingRegions").getJSONObject(0), dpi);
        int cols = tb.getInt("columnCount");

        html.append(String.format(
                        "<div class=\"tableWrap\" style=\"left:%.2fpx;top:%.2fpx;width:%.2fpx;height:%.2fpx;\">",
                        box.x, box.y, box.w, box.h))
                .append("<div class=\"table\" style=\"grid-template-columns:repeat(")
                .append(cols).append(",1fr);\">");

        JSONArray cells = tb.getJSONArray("cells");
        for (int i = 0; i < cells.length(); i++) {
            JSONObject c = cells.getJSONObject(i);
            int row = c.getInt("rowIndex") + 1;
            int col = c.getInt("columnIndex") + 1;
            int rs = c.optInt("rowSpan", 1);
            int cs = c.optInt("columnSpan", 1);
            Style st = styleMap.getOrDefault(styleIdOf(c), Style.DEFAULT);

            html.append(String.format(
                    "<div class=\"cell\" style=\"grid-row:%d/span %d;grid-column:%d/span %d;%s\">%s</div>",
                    row, rs, col, cs, st.css(),
                    escape(c.getString("content").trim())));
        }
        html.append("</div></div>");
    }

    /*──────────────────── DTO & UTIL ───────────────────*/

    private static class Box {
        final float x, y, w, h;

        Box(JSONObject region, float dpi) {
            JSONArray p = region.getJSONArray("polygon");
            x = (float) p.getDouble(0) * dpi;
            y = (float) p.getDouble(1) * dpi;
            w = (float) (p.getDouble(4) - p.getDouble(0)) * dpi;
            h = (float) (p.getDouble(5) - p.getDouble(1)) * dpi;
        }

        static boolean insideAny(JSONObject word, float dpi, List<Box> boxes) {
            JSONArray p = word.getJSONArray("polygon");
            float x0 = (float) p.getDouble(0) * dpi,
                    y0 = (float) p.getDouble(1) * dpi,
                    x1 = (float) p.getDouble(4) * dpi,
                    y1 = (float) p.getDouble(5) * dpi;
            for (Box b : boxes)
                if (x0 >= b.x - 1 && x1 <= b.x + b.w + 1 &&
                        y0 >= b.y - 1 && y1 <= b.y + b.h + 1) return true;
            return false;
        }
    }

    private static class Style {

        static final Style DEFAULT = new Style("Arial", 12, "#000", false, false);

        final String font, color;
        final float size;
        final boolean bold, italic;

        Style(String font, float size, String color, boolean bold, boolean italic) {
            this.font = font;
            this.size = size;
            this.color = color;
            this.bold = bold;
            this.italic = italic;
        }

        String css() {
            return "font-family:'" + escape(font) + "';font-size:" + size + "px;"
                    + "color:" + color + ';'
                    + "font-weight:" + (bold ? "bold" : "normal") + ';'
                    + "font-style:" + (italic ? "italic" : "normal") + ';';
        }

        String wrap(String t) {
            return "<span style=\"" + css() + "\">" + t + "</span>";
        }

        static Map<Integer, Style> build(JSONArray arr) {
            Map<Integer, Style> map = new HashMap<>();
            if (arr == null) return map;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                JSONObject f = o.optJSONObject("font");
                map.put(i, new Style(
                        f != null ? f.optString("fontName", "Arial") : "Arial",
                        f != null ? (float) f.optDouble("fontSize", 12) : 12,
                        f != null ? f.optString("color", "#000") : "#000",
                        o.optBoolean("isBold", false),
                        o.optBoolean("isItalic", false)));
            }
            return map;
        }
    }

    private static float toPx(String unit) {
        return switch (unit) {
            case "inch" -> 96f;
            case "point" -> 96f / 72f;
            default -> 1f;
        };
    }

    private static String detectFileType(File file) {
        String n = file.getName().toLowerCase();
        if (n.endsWith(".pdf")) return "application/pdf";
        if (n.endsWith(".docx"))
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        throw new RuntimeException("Nieznany typ pliku: " + n);
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
