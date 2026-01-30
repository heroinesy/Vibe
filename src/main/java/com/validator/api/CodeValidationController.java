package com.validator.api;

import com.validator.application.CodeValidationService;
import com.validator.dto.request.CodeValidationRequest;
import com.validator.dto.response.CodeValidationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/code")
public class CodeValidationController {

    private final CodeValidationService codeValidationService;

    public CodeValidationController(CodeValidationService codeValidationService) {
        this.codeValidationService = codeValidationService;
    }

    @PostMapping("/review")
    public ResponseEntity<CodeValidationResponse> reviewCode(@Valid @RequestBody CodeValidationRequest request) {
        return ResponseEntity.ok(codeValidationService.review(request));
    }
}
