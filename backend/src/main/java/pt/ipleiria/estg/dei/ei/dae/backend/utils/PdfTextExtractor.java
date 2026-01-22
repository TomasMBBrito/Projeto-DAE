package pt.ipleiria.estg.dei.ei.dae.backend.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;

public class PdfTextExtractor {
    public static String extractText(InputStream pdfInputStream) throws IOException {
        PDDocument document = null;
        try {
            document = PDDocument.load(pdfInputStream);

            if (document.isEncrypted()) {
                throw new IOException("O PDF está encriptado e não pode ser processado");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);

        } finally {
            if (document != null) {
                document.close();
            }
        }
    }
}
