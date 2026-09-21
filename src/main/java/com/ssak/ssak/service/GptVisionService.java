package com.ssak.ssak.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssak.ssak.exception.CustomException;
import com.ssak.ssak.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GptVisionService {

    private static final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";
    private static final int MAX_IMAGE_PX = 512;
    private static final String SYSTEM_PROMPT =
        "당신은 구내식당 식판의 잔반(먹고 남은 음식)을 정밀하게 분석하는 전문가입니다. " +
        "사용자가 식사 후 식판을 촬영한 이미지를 보내면, 남은 음식의 비율을 객관적으로 측정합니다. " +
        "반드시 JSON 형식으로만 응답하며, 분석 근거나 설명은 절대 포함하지 않습니다.";

    private static final String USER_PROMPT =
        "아래 기준에 따라 이 식판 사진의 잔반 비율을 분석해 주세요.\n\n" +

        "[식판 인식]\n" +
        "- 식판은 금속 또는 플라스틱 재질이며, 보통 4~6개의 칸(밥, 국, 반찬)으로 구성됩니다.\n" +
        "- 식판이 사진에 일부만 보여도 보이는 범위 내에서 분석하세요.\n\n" +

        "[잔반 비율 계산 기준]\n" +
        "- 잔반 비율 = (남아있는 음식 면적) / (식판 전체 칸 면적)\n" +
        "- 밥, 국, 반찬 각 칸을 개별 확인 후 전체 평균을 냅니다.\n" +
        "- 국물이 남은 경우도 잔반으로 계산합니다.\n" +
        "- 식판 재질(금속 광택, 빈 칸 바닥, 물기)은 음식으로 계산하지 않습니다.\n" +
        "- 고명·양념 자국만 남은 경우는 0.05 이하로 처리합니다.\n\n" +

        "[비율 기준 예시]\n" +
        "- 0.0 : 모든 칸이 깨끗하게 비워진 상태\n" +
        "- 0.05~0.15 : 소량의 자국·국물만 남은 상태 (거의 다 먹음)\n" +
        "- 0.2~0.4 : 반찬 일부 또는 밥·국 소량 남김\n" +
        "- 0.5~0.7 : 전체적으로 절반 이상 남긴 상태\n" +
        "- 0.8~1.0 : 음식을 거의 먹지 않은 상태\n\n" +

        "[무효 판정 기준 — leftover_ratio: -1 반환]\n" +
        "- 식판 또는 음식이 이미지에 전혀 보이지 않는 경우\n" +
        "- 식판이 아닌 다른 사물(책상, 손 등)만 찍힌 경우\n" +
        "- 이미지가 너무 어둡거나 흐려서 판단이 불가능한 경우\n\n" +

        "[reason 필드 작성 규칙]\n" +
        "- 정상 분석 시: reason은 null\n" +
        "- 무효(-1) 또는 판단이 불확실(신뢰도 낮음)한 경우: 한국어로 간단한 이유 작성\n" +
        "  예) \"이미지가 너무 어두워 식판을 인식할 수 없습니다\"\n" +
        "  예) \"식판이 아닌 다른 사물이 찍혔습니다\"\n" +
        "  예) \"사진이 흔들려 음식 구분이 어렵습니다\"\n\n" +

        "반드시 아래 JSON 형식으로만 답하세요:\n" +
        "{\"leftover_ratio\": <0.0~1.0 소수점 둘째 자리, 또는 무효 시 -1>, \"reason\": <null 또는 이유 문자열>}";

    public record AnalysisResult(double ratio, String reason) {}

    private final RestClient restClient;

    @Value("${openai.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisResult analyzeLeftoverRatio(byte[] rawBytes, String contentType) {
        try {
            byte[] imageBytes = resizeImage(rawBytes);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = contentType != null ? contentType : "image/jpeg";

            String requestBody = buildRequestBody(base64Image, mimeType);

            String rawResponse = restClient.post()
                    .uri(GPT_API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseResult(rawResponse);

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
        request.put("max_tokens", 200);

        Map<String, Object> systemMessage = new LinkedHashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);

        Map<String, Object> textContent = new LinkedHashMap<>();
        textContent.put("type", "text");
        textContent.put("text", USER_PROMPT);

        Map<String, Object> imageUrlContent = new LinkedHashMap<>();
        imageUrlContent.put("type", "image_url");
        imageUrlContent.put("image_url", Map.of(
                "url", "data:" + mimeType + ";base64," + base64Image,
                "detail", "high"
        ));

        Map<String, Object> userMessage = new LinkedHashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", List.of(textContent, imageUrlContent));

        request.put("messages", List.of(systemMessage, userMessage));

        return objectMapper.writeValueAsString(request);
    }

    private AnalysisResult parseResult(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        JsonNode result = objectMapper.readTree(content);

        double ratio = result.path("leftover_ratio").asDouble(Double.NaN);
        if (Double.isNaN(ratio)) {
            throw new CustomException(ErrorCode.INCORRECT_RESPONSE);
        }

        String reason = result.path("reason").isNull() ? null : result.path("reason").asText(null);
        if (reason != null && !reason.isBlank()) {
            log.warn("GPT 분석 불확실 — ratio={}, reason={}", ratio, reason);
        }

        return new AnalysisResult(ratio, reason);
    }

    private byte[] resizeImage(byte[] rawBytes) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(rawBytes));
        if (original == null) {
            throw new CustomException(ErrorCode.INCORRECT_IMAGE);
        }

        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= MAX_IMAGE_PX && height <= MAX_IMAGE_PX) {
            return rawBytes;
        }

        double scale = (double) MAX_IMAGE_PX / Math.max(width, height);
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpeg", out);
        return out.toByteArray();
    }
}
