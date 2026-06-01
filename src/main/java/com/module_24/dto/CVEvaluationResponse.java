package com.module_24.dto;

import lombok.Data;

import java.util.List;

@Data
public class CVEvaluationResponse {
    private int formatting_score;
    private int content_score;
    private int skills_score;
    private int experience_score;
    private int professionalism_score;
    private int total_score;
    private int percentage;

    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
}
