package org.demo.backend.global.web;

import org.demo.backend.global.web.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HeathCheckController {

    @GetMapping("/api/v1/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok().body(ApiResponse.success("UP"));
    }
}
