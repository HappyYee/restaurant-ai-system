package com.example.restaurant.client;

import com.example.restaurant.common.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DeepSeekClient {
    private final ObjectMapper objectMapper;

    @Value("${ai.deepseek.base-url}")
    private String baseUrl;

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.model}")
    private String model;

    @Value("${ai.deepseek.timeout-seconds:20}")
    private long timeoutSeconds;

    public String chat(List<Map<String, String>> messages) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException("DeepSeek API Key 未配置");
        }
        String payload = "";
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                    .build();
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", messages,
                    "temperature", 0.4,
                    "stream", false,
                    "response_format", Map.of("type", "json_object"),
                    "thinking", Map.of("type", "disabled")
            );
            payload = objectMapper.writeValueAsString(requestBody);
            String endpoint = baseUrl.replaceAll("/+$", "") + "/chat/completions";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("DeepSeek 调用失败：" + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new BusinessException("DeepSeek 返回内容为空");
            }
            return content.asText();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            return curlChat(payload, ex);
        }
    }

    public String model() {
        return model;
    }

    public boolean configured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String provider() {
        return "DeepSeek";
    }

    private String curlChat(String payload, Exception cause) {
        try {
            String endpoint = baseUrl.replaceAll("/+$", "") + "/chat/completions";
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "sh",
                    "-c",
                    "curl -sS --connect-timeout \"$DEEPSEEK_TIMEOUT\" --max-time \"$DEEPSEEK_TIMEOUT\" "
                            + "-X POST \"$DEEPSEEK_ENDPOINT\" "
                            + "-H \"Authorization: Bearer $DEEPSEEK_API_KEY\" "
                            + "-H \"Content-Type: application/json\" "
                            + "--data-binary @-"
            );
            processBuilder.environment().put("DEEPSEEK_API_KEY", apiKey);
            processBuilder.environment().put("DEEPSEEK_ENDPOINT", endpoint);
            processBuilder.environment().put("DEEPSEEK_TIMEOUT", String.valueOf(timeoutSeconds));
            Process process = processBuilder.start();
            process.getOutputStream().write(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            process.getOutputStream().close();
            boolean finished = process.waitFor(timeoutSeconds + 3, TimeUnit.SECONDS);
            String responseBody = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String errorBody = new String(process.getErrorStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException("DeepSeek curl 调用超时");
            }
            if (process.exitValue() != 0) {
                throw new BusinessException("DeepSeek curl 调用失败：" + errorBody);
            }
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new BusinessException("DeepSeek curl 返回内容为空");
            }
            return content.asText();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("DeepSeek 调用异常：" + cause.getMessage() + "；curl兜底异常：" + ex.getMessage());
        }
    }
}
