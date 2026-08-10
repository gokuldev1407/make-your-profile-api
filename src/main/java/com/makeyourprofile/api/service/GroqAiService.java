package com.makeyourprofile.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GroqAiService {

    private final RestClient restClient;
    private final String model;

    public GroqAiService(
            @Value("${groq.api.url}") String apiUrl,
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.api.model}") String model,
            RestClient.Builder restClientBuilder) {
        
        this.model = model;
        this.restClient = restClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Polishes a given resume bullet point or text snippet to sound more professional.
     */
    public String polishResumeText(String originalText) {
        String systemPrompt = "You are an expert resume writer. " +
                "Rewrite the following text to sound highly professional, action-oriented, and concise. " +
                "Only return the polished text. Do not include any explanations.";
        
        return generateCompletion(systemPrompt, originalText);
    }

    /**
     * Generates a professional summary based on a list of skills and current title.
     */
    public String generateProfessionalSummary(String title, String skillsJson) {
        String systemPrompt = "You are an expert resume writer. " +
                "Write a 2-3 sentence professional summary for a " + title + ". " +
                "Only return the summary text without quotes or explanations.";

        return generateCompletion(systemPrompt, "Skills: " + skillsJson);
    }

    /**
     * Updates a portfolio JSON based on a user prompt.
     */
    public String updatePortfolioJson(String currentJson, String prompt) {
        String systemPrompt = "You are an AI portfolio assistant. You will be provided with a JSON representation of a user's portfolio and a request to modify it. " +
                "Apply the modifications requested by the user to the JSON. " +
                "Return ONLY the updated JSON string. Do not use Markdown code blocks (no ```json or ```). " +
                "Ensure the output is strictly valid JSON matching the exact schema provided. " +
                "Do not add any conversational text or explanations.";

        String userMessage = "CURRENT JSON:\n" + currentJson + "\n\nREQUESTED MODIFICATION:\n" + prompt;
        return generateCompletion(systemPrompt, userMessage);
    }

    private String generateCompletion(String systemMessage, String userMessage) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemMessage),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "temperature", 0.7,
                    "max_tokens", 4000
            );

            Map<String, Object> response = restClient.post()
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "No content generated";
        } catch (Exception e) {
            log.error("Error communicating with Groq API", e);
            throw new RuntimeException("Failed to generate content from AI", e);
        }
    }
}
