package pl.pwr.Neuralingo.translation.ocr;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.pwr.Neuralingo.service.AzureDocumentIntelligenceService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Component
public class DocumentExtractor {

    @Autowired
    private AzureDocumentIntelligenceService azure;

    public String extractTextAsHtml(File inputFile) throws IOException {
        byte[] bytes = Files.readAllBytes(inputFile.toPath());
        JSONObject root = azure.analyzeLayout(bytes, detectFileType(inputFile));

        StringBuilder html = new StringBuilder("""
                    <!DOCTYPE html><html><head><meta charset="UTF-8">
                    <style>
                      body{background:#fff}
                      .page{position:relative;border:1px solid #000;margin:20px auto}
                      .text{position:absolute;white-space:pre}
                      .tableWrap{position:absolute}
                      .table{display:grid;border:1px solid #000;border-collapse:collapse;width:100%;height:100%}
                      .cell{border:1px solid #888;padding:4px;white-space:pre}
                    </style></head><body>
                """);

        JSONArray pages = root.getJSONObject("analyzeResult").getJSONArray("pages");
        JSONArray tables = root.getJSONObject("analyzeResult").optJSONArray("tables");

        Map<Integer, List<TableBox>> pageTableBoxes = groupTableBoxesByPage(tables);

        for (int p = 0; p < pages.length(); p++) {
            JSONObject page = pages.getJSONObject(p);
            int pageNum = page.getInt("pageNumber");
            float dpi = dpi(page.optString("unit", "pixel"));
            float PW = page.getFloat("width") * dpi, PH = page.getFloat("height") * dpi;

            html.append(String.format("<div class=\"page\" style=\"width:%.0fpx;height:%.0fpx;\">", PW, PH));

            // ---- render tables first ----
            if (pageTableBoxes.containsKey(pageNum)) {
                for (TableBox tb : pageTableBoxes.get(pageNum)) {
                    renderTable(html, tb.table, dpi);
                }
            }

            // ---- render words, skipping those that belong to tables ----
            renderWords(html, page.optJSONArray("words"), dpi, pageTableBoxes.getOrDefault(pageNum, List.of()));

            html.append("</div>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    /* ---------------- WORDS ---------------- */
    private void renderWords(StringBuilder html, JSONArray words, float dpi, List<TableBox> boxes) {
        if (words == null || words.isEmpty()) return;

        TreeMap<Float, List<JSONObject>> lines = new TreeMap<>();
        float tol = 2f;

        for (int i = 0; i < words.length(); i++) {
            JSONObject w = words.getJSONObject(i);
            if (isInsideTable(w, dpi, boxes)) continue;     // pomiń jeśli słowo jest w tabeli
            float y = w.getJSONArray("polygon").getFloat(1) * dpi;
            Float key = lines.keySet().stream().filter(k -> Math.abs(k - y) <= tol).findFirst().orElse(y);
            lines.computeIfAbsent(key, k -> new ArrayList<>()).add(w);
        }

        for (var e : lines.entrySet()) {
            List<JSONObject> lw = e.getValue();
            lw.sort(Comparator.comparingDouble(w -> w.getJSONArray("polygon").getFloat(0)));

            float minX = Float.MAX_VALUE, minY = e.getKey(), maxX = 0, maxY = 0;
            Set<String> colors = new HashSet<>(), fonts = new HashSet<>();
            Set<Float> sizes = new HashSet<>();
            Set<Boolean> bolds = new HashSet<>(), itals = new HashSet<>();

            for (JSONObject w : lw) {
                JSONObject s = span(w);
                colors.add(s.optString("color", "#000"));
                fonts.add(s.optString("fontName", "Arial"));
                sizes.add((float) s.optDouble("fontSize", 12));
                String st = s.optString("styleName", "");
                bolds.add(st.toLowerCase().contains("bold"));
                itals.add(st.toLowerCase().contains("italic"));

                JSONArray poly = w.getJSONArray("polygon");
                float x0 = poly.getFloat(0) * dpi, y0 = poly.getFloat(1) * dpi;
                float x4 = poly.getFloat(4) * dpi, y5 = poly.getFloat(5) * dpi;
                minX = Math.min(minX, x0);
                maxX = Math.max(maxX, x4);
                maxY = Math.max(maxY, y5);
            }
            float width = maxX - minX, height = maxY - minY;
            boolean uni = colors.size() == 1 && fonts.size() == 1 && sizes.size() == 1 && bolds.size() == 1 && itals.size() == 1;
            StringBuilder txt = new StringBuilder();

            for (JSONObject w : lw) {
                if (txt.length() > 0) txt.append(" ");
                if (uni) {
                    txt.append(esc(w.getString("content")));
                } else {
                    JSONObject s = span(w);
                    String col = s.optString("color", "#000"), font = s.optString("fontName", "Arial");
                    float fs = (float) s.optDouble("fontSize", 12);
                    boolean b = s.optString("styleName", "").toLowerCase().contains("bold");
                    boolean i = s.optString("styleName", "").toLowerCase().contains("italic");
                    txt.append(String.format(
                            "<span style=\"font-family:'%s';font-size:%.1fpx;color:%s;font-weight:%s;font-style:%s;\">%s</span>",
                            esc(font), fs, col, b ? "bold" : "normal", i ? "italic" : "normal", esc(w.getString("content"))));
                }
            }
            if (uni) {
                String col = colors.iterator().next(), font = fonts.iterator().next();
                float fs = sizes.iterator().next();
                boolean b = bolds.iterator().next(), i = itals.iterator().next();
                html.append(String.format(
                        "<div class=\"text\" style=\"left:%.2fpx;top:%.2fpx;width:%.2fpx;height:%.2fpx;font-family:'%s';font-size:%.1fpx;color:%s;font-weight:%s;font-style:%s;\">%s</div>",
                        minX, minY, width, height, esc(font), fs, col, b ? "bold" : "normal", i ? "italic" : "normal", txt));
            } else {
                html.append(String.format(
                        "<div class=\"text\" style=\"left:%.2fpx;top:%.2fpx;width:%.2fpx;height:%.2fpx;\">%s</div>",
                        minX, minY, width, height, txt));
            }
        }
    }

    /* ---------------- TABLES ---------------- */
    private void renderTable(StringBuilder html, JSONObject table, float dpi) {
        TableBox tb = new TableBox(table, dpi);
        int cols = table.getInt("columnCount");

        html.append(String.format(
                "<div class=\"tableWrap\" style=\"left:%.2fpx;top:%.2fpx;width:%.2fpx;height:%.2fpx;\">",
                tb.x, tb.y, tb.w, tb.h));
        html.append("<div class=\"table\" style=\"grid-template-columns:repeat(")
                .append(cols).append(",1fr);\">");

        JSONArray cells = table.getJSONArray("cells");
        for (int i = 0; i < cells.length(); i++) {
            JSONObject c = cells.getJSONObject(i);
            int r = c.optInt("rowIndex") + 1, col = c.optInt("columnIndex") + 1;
            int rs = c.optInt("rowSpan", 1), cs = c.optInt("columnSpan", 1);
            String cnt = esc(c.getString("content").trim());

            JSONObject sp = c.has("spans") && c.getJSONArray("spans").length() > 0 ? c.getJSONArray("spans").getJSONObject(0) : new JSONObject();
            String font = sp.optString("fontName", "Arial");
            float fs = (float) sp.optDouble("fontSize", 12);
            String colr = sp.optString("color", "#000");
            boolean b = sp.optString("styleName", "").toLowerCase().contains("bold");
            boolean it = sp.optString("styleName", "").toLowerCase().contains("italic");

            html.append(String.format(
                    "<div class=\"cell\" style=\"grid-row:%d/span %d;grid-column:%d/span %d;font-family:'%s';font-size:%.1fpx;color:%s;font-weight:%s;font-style:%s;\">%s</div>",
                    r, rs, col, cs, esc(font), fs, colr, b ? "bold" : "normal", it ? "italic" : "normal", cnt));
        }
        html.append("</div></div>");
    }

    private Map<Integer, List<TableBox>> groupTableBoxesByPage(JSONArray tables) {
        Map<Integer, List<TableBox>> map = new HashMap<>();
        if (tables == null) return map;
        for (int i = 0; i < tables.length(); i++) {
            JSONObject t = tables.getJSONObject(i);
            int pg = t.getJSONArray("boundingRegions").getJSONObject(0).getInt("pageNumber");
            map.computeIfAbsent(pg, k -> new ArrayList<>()).add(new TableBox(t, dpi("inch"))); // dpi val replaced later
        }
        return map;
    }

    private boolean isInsideTable(JSONObject word, float dpi, List<TableBox> boxes) {
        if (boxes.isEmpty()) return false;
        JSONArray poly = word.getJSONArray("polygon");
        float x0 = poly.getFloat(0) * dpi, y0 = poly.getFloat(1) * dpi;
        float x4 = poly.getFloat(4) * dpi, y5 = poly.getFloat(5) * dpi;
        for (TableBox b : boxes) {
            if (x0 >= b.x && x4 <= b.x + b.w && y0 >= b.y && y5 <= b.y + b.h) return true;
        }
        return false;
    }

    private static class TableBox {
        final JSONObject table;
        final float x, y, w, h;

        TableBox(JSONObject t, float dpi) {
            table = t;
            JSONArray box = t.getJSONArray("boundingRegions").getJSONObject(0)
                    .optJSONArray("boundingBox");
            if (box == null) box = t.getJSONArray("boundingRegions").getJSONObject(0).getJSONArray("polygon");
            float minX = box.getFloat(0), minY = box.getFloat(1), maxX = box.getFloat(4), maxY = box.getFloat(5);
            x = minX * dpi;
            y = minY * dpi;
            w = (maxX - minX) * dpi;
            h = (maxY - minY) * dpi;
        }
    }

    /* ---------- helpers ---------- */
    private static JSONObject span(JSONObject w) {
        return w.has("spans") && w.getJSONArray("spans").length() > 0
                ? w.getJSONArray("spans").getJSONObject(0) : new JSONObject();
    }

    private static float dpi(String unit) {
        return switch (unit) {
            case "inch" -> 96f;
            case "point" -> 96f / 72f;
            default -> 1f;
        };
    }

    private static String detectFileType(File f) {
        String n = f.getName().toLowerCase();
        if (n.endsWith(".pdf")) return "application/pdf";
        if (n.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        throw new RuntimeException("Nieznany typ: " + n);
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
