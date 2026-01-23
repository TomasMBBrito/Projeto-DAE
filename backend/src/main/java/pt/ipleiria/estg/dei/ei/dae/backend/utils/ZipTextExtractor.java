package pt.ipleiria.estg.dei.ei.dae.backend.utils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipTextExtractor {

    public static String extractTextFromPDFs(InputStream zipInputStream) throws IOException {
        List<String> pdfTexts = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName().toLowerCase();

                if (!entry.isDirectory() && fileName.endsWith(".pdf")) {
                    try {
                        byte[] pdfBytes = readEntryBytes(zis);

                        InputStream pdfStream = new ByteArrayInputStream(pdfBytes);

                        String pdfText = PdfTextExtractor.extractText(pdfStream);
                        pdfTexts.add(pdfText);

                    } catch (Exception e) {
                        System.err.println("Erro ao extrair PDF '" + entry.getName() + "': " + e.getMessage());
                    }
                }

                zis.closeEntry();
            }
        }

        if (pdfTexts.isEmpty()) {
            return "";
        }

        return String.join("\n\n=== PRÓXIMO DOCUMENTO ===\n\n", pdfTexts);
    }

    private static byte[] readEntryBytes(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;

        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }

        return baos.toByteArray();
    }

    public static List<String> listFiles(InputStream zipInputStream) throws IOException {
        List<String> fileNames = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    fileNames.add(entry.getName());
                }
                zis.closeEntry();
            }
        }

        return fileNames;
    }
}
