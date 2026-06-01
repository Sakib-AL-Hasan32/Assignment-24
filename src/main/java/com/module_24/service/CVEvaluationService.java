package com.module_24.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.module_24.dto.CVEvaluationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CVEvaluationService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    public CVEvaluationResponse evaluate(MultipartFile file) {
        try {

            byte[] imageBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            String mimeType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

            String prompt = """
                    You are an expert CV evaluator and recruitment consultant.
                    
                    Your task is to analyze a CV provided as an image and evaluate its quality based on professional hiring standards.
                    
                    Evaluate the CV across the following dimensions:
                    
                    1. Formatting & Structure (0-10)
                       - Clear sections (Education, Experience, Skills, etc.)
                       - Readability and layout
                       - Proper alignment and spacing
                    
                    2. Content Quality (0-10)
                       - Clarity of descriptions
                       - Use of action verbs
                       - Relevance of information
                    
                    3. Skills & Technical Strength (0-10)
                       - Presence of relevant skills
                       - Depth of expertise
                       - Alignment with industry expectations
                    
                    4. Experience & Impact (0-10)
                       - Quantifiable achievements
                       - Real-world impact
                       - Internship/project relevance
                    
                    5. Overall Professionalism (0-10)
                       - Grammar and spelling
                       - Tone and presentation
                       - Completeness
                    
                    After evaluating all categories:
                    - Calculate TOTAL SCORE out of 50
                    - Convert it to a percentage (0-100)
                    
                    IMPORTANT:
                    - Be strict and realistic (do not give overly generous scores)
                    - Do not assume missing information
                    - Base evaluation only on visible content in the CV image
                    
                    Return your response ONLY in the following JSON format:
                    
                    {
                      "formatting_score": number,
                      "content_score": number,
                      "skills_score": number,
                      "experience_score": number,
                      "professionalism_score": number,
                      "total_score": number,
                      "percentage": number,
                      "strengths": ["point1", "point2", "point3"],
                      "weaknesses": ["point1", "point2", "point3"],
                      "suggestions": ["improvement1", "improvement2", "improvement3"]
                    }
                    
                    Do NOT include any explanation outside JSON.
                    Ensure all fields are present.
                    Ensure numbers are integers.
                    """;

            String requestBody = """
                    {
                      "contents": [
                        {
                          "parts": [
                            {
                              "text": %s
                            },
                            {
                              "inline_data": {
                                "mime_type": "%s",
                                "data": "%s"
                              }
                            }
                          ]
                        }
                      ]
                    }
                    """.formatted(
                    objectMapper.writeValueAsString(prompt),
                    mimeType,
                    base64Image
            );

            HttpClient client = HttpClient.newHttpClient();
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            var jsonResponse = objectMapper.readTree(response.body());
            String aiText = jsonResponse
                    .get("candidates")
                    .get(0)
                    .get("content")
                    .get("parts")
                    .get(0)
                    .get("text")
                    .asText();

            aiText = aiText.replaceAll("```json", "").replaceAll("```", "").trim();

            return objectMapper.readValue(aiText, CVEvaluationResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("CV evaluation failed: " + e.getMessage(), e);
        }
    }
}