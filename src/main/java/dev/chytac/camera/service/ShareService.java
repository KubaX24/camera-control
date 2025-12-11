package dev.chytac.camera.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class ShareService {

    private final Environment environment;

    /**
     *
     * @param file JPG image
     * @return image url
     */
    public Optional<String> uploadImage(File file) {
        try {
            String boundary = "------------------------" + UUID.randomUUID().toString().replace("-", "");
            String secret = environment.getProperty("application.secret-key");
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String mimeType = "image/jpeg";

            StringBuilder sb = new StringBuilder();
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"secret\"").append("\r\n\r\n");
            sb.append(secret).append("\r\n");

            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"photo\"; filename=\"").append(file.getName()).append("\"\r\n");
            sb.append("Content-Type: ").append(mimeType).append("\r\n\r\n");

            byte[] headerBytes = sb.toString().getBytes(StandardCharsets.UTF_8);

            byte[] footerBytes = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

            int totalLength = headerBytes.length + fileBytes.length + footerBytes.length;
            byte[] allBytes = new byte[totalLength];

            System.arraycopy(headerBytes, 0, allBytes, 0, headerBytes.length);
            System.arraycopy(fileBytes, 0, allBytes, headerBytes.length, fileBytes.length);
            System.arraycopy(footerBytes, 0, allBytes, headerBytes.length + fileBytes.length, footerBytes.length);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(environment.getProperty("application.upload-url")))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(allBytes))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Other status code than ok. Code {}", response.statusCode());
                return Optional.empty();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.body());

            if (!jsonNode.get("success").asBoolean()){
                log.error("Error while uploading image: {}", jsonNode.get("message").asText());
                return Optional.empty();
            }

            String viewUrl = environment.getProperty("application.view-url");
            viewUrl = viewUrl.replace("%s", jsonNode.get("id").asText());
            return Optional.of(viewUrl);
        } catch (IOException | InterruptedException e) {
            log.error("Error while uploading image", e);
        }

        return Optional.empty();
    }
}
