package com.scorelens.Controller.v3;

import com.scorelens.Entity.ResponseObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Health V3", description = "Health Check API")
@RestController
@RequestMapping("v3/health")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HealthCheck {
    @GetMapping
    public ResponseObject check(){
        return ResponseObject.builder()
                .status(1000)
                .message("Check")
                .build();
    }
}
