package com.module_24.controller;

import com.module_24.dto.CVEvaluationResponse;
import com.module_24.service.CVEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
public class CVController {
    private final CVEvaluationService cvEvaluationService;

    @PostMapping("/evaluate")
    public CVEvaluationResponse evaluateCV(
            @RequestParam("file") MultipartFile file) {

        return cvEvaluationService.evaluate(file);
    }
}
