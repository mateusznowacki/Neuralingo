package pl.pwr.Neuralingo.translation.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Ekstraktor tekstu oparty na Apache PDFBox 2.0.29.
 * Buduje HTML-owe paragrafy z <span>-ami opisującymi rodzinę czcionki,
 * rozmiar w punktach oraz heurystycznie pogrubienie/kursywę.
 */
@Component
public class PdfContentExtractor {

    public static class ExtractedText {

        public static class Paragraph {
            public final int index;
            public final String html;

            public Paragraph(int index, String html) {
                this.index = index;
                this.html = html;
            }
        }

        public final List<Paragraph> paragraphs;

        public ExtractedText(List<Paragraph> paragraphs) {
            this.paragraphs = paragraphs;
        }
    }

    public ExtractedText extractText(File pdfFile) throws IOException {
        try (PDDocument doc = PDDocument.load(pdfFile)) {
            StyledStripper stripper = new StyledStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(doc.getNumberOfPages());
            stripper.getText(doc);                       // wypełnia kolekcję wewnętrzną
            return new ExtractedText(stripper.getCollected());
        }
    }

    /* ===================================================================== */

    private static class StyledStripper extends PDFTextStripper {

        private static final float NEWLINE_THRESHOLD_PT = 2.5f;

        private final List<ExtractedText.Paragraph> collected = new ArrayList<>();
        private final StringBuilder paragraphBuf = new StringBuilder();
        private float baselineY = Float.NaN;
        private int paragraphIdx = 0;

        StyledStripper() throws IOException {
        }

        List<ExtractedText.Paragraph> getCollected() {
            return collected;
        }

        @Override
        protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
            // iterujemy po każdym glifie, aby zebrać pełne metadane
            for (TextPosition tp : textPositions) {
                float y = tp.getYDirAdj();
                if (Float.isNaN(baselineY)) baselineY = y;

                boolean newLine = Math.abs(y - baselineY) > NEWLINE_THRESHOLD_PT;
                if (newLine) {
                    flushParagraph();
                    baselineY = y;
                }
                appendStyledSpan(tp);
            }
        }

        @Override
        protected void writeLineSeparator() throws IOException {
            flushParagraph();               // koniec fizycznej linii => koniec akapitu
        }

        /* -------------------------- pomocnicze --------------------------- */

        private void flushParagraph() {
            if (paragraphBuf.length() == 0) return;
            collected.add(new ExtractedText.Paragraph(paragraphIdx++, "<p>" + paragraphBuf + "</p>"));
            paragraphBuf.setLength(0);
        }

        private void appendStyledSpan(TextPosition tp) {
            String glyph = tp.getUnicode();
            if (glyph == null || glyph.isBlank()) return;

            String family = tp.getFont().getName();          // np. QWYBJF+TimesNewRomanPSMT
            float fontSize = tp.getFontSizeInPt();

            String familyLower = family.toLowerCase(Locale.ROOT);
            boolean bold = familyLower.contains("bold");
            boolean italic = familyLower.contains("italic") || familyLower.contains("oblique");

            String style = "font-family:'" + escapeCss(family) + "';"
                    + "font-size:" + round(fontSize) + "pt;"
                    + "color:rgb(0,0,0);"            // PDFTextStripper nie udostępnia koloru
                    + (bold ? "font-weight:bold;" : "")
                    + (italic ? "font-style:italic;" : "");

            paragraphBuf.append("<span style=\"")
                    .append(style)
                    .append("\">")
                    .append(escapeHtml(glyph))
                    .append("</span>");
        }

        private static String escapeCss(String v) {
            return v.replace("'", "\\'");
        }

        private static String escapeHtml(String s) {
            return s.chars()
                    .mapToObj(ch -> switch (ch) {
                        case '&' -> "&amp;";
                        case '<' -> "&lt;";
                        case '>' -> "&gt;";
                        case '"' -> "&quot;";
                        case '\'' -> "&#x27;";
                        default -> String.valueOf((char) ch);
                    })
                    .collect(Collectors.joining());
        }

        private static String round(float v) {
            return String.format(Locale.US, "%.2f", v);
        }
    }
}
