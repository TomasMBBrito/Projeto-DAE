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

    //Extrai texto de pdfs dentro de um ficheiro zip
    public static String extractTextFromPDFs(InputStream zipInputStream) throws IOException {
        List<String> pdfTexts = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;

            // Percorre todos os ficheiros dentro do ZIP
            while ((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName().toLowerCase();

                // Verificar se é um PDF
                if (!entry.isDirectory() && fileName.endsWith(".pdf")) {
                    try {
                        // Ler o conteúdo do PDF para um byte array
                        byte[] pdfBytes = readEntryBytes(zis);

                        // Criar um InputStream a partir dos bytes
                        InputStream pdfStream = new ByteArrayInputStream(pdfBytes);

                        // Extrair texto do PDF
                        String pdfText = PdfTextExtractor.extractText(pdfStream);
                        pdfTexts.add(pdfText);

                    } catch (Exception e) {
                        // Se falhar a extração de um PDF, continua para o próximo
                        System.err.println("Erro ao extrair PDF '" + entry.getName() + "': " + e.getMessage());
                    }
                }

                zis.closeEntry();
            }
        }

        if (pdfTexts.isEmpty()) {
            return ""; // Nenhum PDF encontrado
        }

        // Concatenar todos os textos extraídos
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
