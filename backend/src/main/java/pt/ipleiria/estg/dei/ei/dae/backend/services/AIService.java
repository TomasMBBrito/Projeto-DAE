package pt.ipleiria.estg.dei.ei.dae.backend.services;

import jakarta.ejb.Stateless;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.StringReader;
import java.util.logging.Logger;

@Stateless
public class AIService {

    private static final Logger logger = Logger.getLogger(AIService.class.getName());

    // URL do Ollama (dentro do Docker Compose)
    private static final String OLLAMA_URL = "http://ollama:11434/api/generate";

    // Modelo a usar
    private static final String MODEL = "llama3.2";

    public String generateSummary(String documentText) {
        logger.info("A gerar resumo com IA...");

        // Limitar o texto (modelos têm limites de contexto)
        String truncatedText = truncateText(documentText, 4000);

        // Criar o prompt em português
        String prompt = "Faz um resumo conciso e informativo (máximo 300 palavras) do seguinte documento científico:\n\n"
                + truncatedText
                + "\n\nResumo:";

        // Preparar o corpo do pedido JSON
        JsonObject requestBody = Json.createObjectBuilder()
                .add("model", MODEL)
                .add("prompt", prompt)
                .add("stream", false)  // Não queremos streaming
                .add("options", Json.createObjectBuilder()
                        .add("temperature", 0.5)      // Menos criativo, mais factual
                        .add("num_predict", 400)      // Máximo de tokens no resumo
                )
                .build();

        // Fazer o pedido HTTP POST ao Ollama
        Client client = ClientBuilder.newClient();
        try {
            Response response = client.target(OLLAMA_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody.toString()));

            if (response.getStatus() == 200) {
                String responseBody = response.readEntity(String.class);
                String summary = extractResponseText(responseBody);
                logger.info("Resumo gerado com sucesso!");
                return summary;
            } else {
                logger.severe("Erro ao gerar resumo: HTTP " + response.getStatus());
                throw new RuntimeException("Erro ao comunicar com o serviço de IA: " + response.getStatus());
            }
        } catch (Exception e) {
            logger.severe("Exceção ao gerar resumo: " + e.getMessage());
            throw new RuntimeException("Erro ao gerar resumo com IA", e);
        } finally {
            client.close();
        }
    }

    private String extractResponseText(String jsonResponse) {
        try (JsonReader reader = Json.createReader(new StringReader(jsonResponse))) {
            JsonObject obj = reader.readObject();
            return obj.getString("response", "");
        } catch (Exception e) {
            logger.severe("Erro ao extrair resposta: " + e.getMessage());
            return "";
        }
    }

    private String truncateText(String text, int maxChars) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "...";
    }
}
