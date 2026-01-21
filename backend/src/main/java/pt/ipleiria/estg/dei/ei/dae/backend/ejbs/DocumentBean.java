package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.Document;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.FileType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Stateless
public class DocumentBean {
    private static final String UPLOAD_DIR = "/tmp/uploads";

    @PersistenceContext
    private EntityManager em;

    public Document create(String filename, String username, InputStream stream, FileType fileType)
            throws IOException {

        try {
            // Criar diretório para o utilizador se não existir
            var targetDirectoryPath = Paths.get(UPLOAD_DIR, username);
            if (!Files.exists(targetDirectoryPath)) {
                Files.createDirectories(targetDirectoryPath);
            }

            // Guardar ficheiro com nome único
            var targetFilePath = targetDirectoryPath.resolve("file_" + UUID.randomUUID());
            Files.copy(stream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);

            // Criar documento
            var document = new Document(filename, targetFilePath.toString(), fileType);
            //em.persist(document);

            return document;

        } catch (IOException e) {
            // Log detalhado do erro
            System.err.println("Error creating directory or saving file: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        }
    }
}
