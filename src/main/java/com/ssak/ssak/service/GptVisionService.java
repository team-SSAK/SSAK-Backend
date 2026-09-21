package com.ssak.ssak.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

@Slf4j
@Service
public class GptVisionService {

    private static final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";
    private static final int MAX_IMAGE_PX = 512;
    private static final String PROMPT =
        "이 이미지는 구내식당 식판 사진입니다. 식판 위에 남은 음식(잔반)의 비율을 분석해 주세요. " +
        "반드시 다음 JSON 형식으로만 답하세요: {\"leftover_ratio\": <숫자>}. " +
        "leftover_ratio는 0.0~1.0 사이 숫자이며, 1.0은 음식이 가득 남은 상태, 0.0은 완전히 비운 상태입니다. " +
        "식판이 보이지 않거나 음식 사진이 아닌 경우 {\"leftover_ratio\": -1}을 반환하세요.";

    @Value("${openai.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public double analyzeLeftoverRatio(MultipartFile file) {
        try {
            byte[] imageBytes = resizeImage(file);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

            String requestBody = buildRequestBody(base64Image, mimeType);

            RestClient restClient = RestClient.create();
            String rawResponse = restClient.post()
                    .uri(GPT_API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseLeftoverRatio(rawResponse);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("GPT Vision API 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INCORRECT_RESPONSE);
        }
    }

    private String buildRequestBody(String base64Image, String mimeType) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", MODEL);
        request.put("response_format", Map.of("type", "json_object"));
        request.put("max_tokens", 100);

        Map<String, Object> textContent = new LinkedHashMap<>();
        textContent.put("type", "text");
        textContent.put("text", PROMPT);

        Map<String, Object> imageUrlContent = new LinkedHashMap<>();
        imageUrlContent.put("type", "image_url");
        imageUrlContent.put("image_url", Map.of(
                "url", "data:" + mimeType + ";base64," + base64Image,
                "detail", "low"
        ));

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", List.of(textContent, imageUrlContent));

        request.put("messages", List.of(message));

        return objectMapper.writeValueAsString(request);
    }

    private double parseLeftoverRatio(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        JsonNode result = objectMapper.readTree(content);
        double ratio = result.path("leftover_ratio").asDouble(Double.NaN);

        if (Double.isNaN(ratio)) {
            throw new CustomException(ErrorCode.INCORRECT_RESPONSE);
        }
        return ratio;
    }

    private byte[] resizeImage(MultipartFile file) throws IOException {
        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) {
            throw new CustomException(ErrorCode.INCORRECT_IMAGE);
        }

        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= MAX_IMAGE_PX && height <= MAX_IMAGE_PX) {
            return file.getBytes();
        }

        double scale = (double) MAX_IMAGE_PX / Math.max(width, height);
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpeg", out);
        return out.toByteArray();
    }
}
