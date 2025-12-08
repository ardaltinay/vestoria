package io.vestoria.controller;

import io.vestoria.dto.response.ReportResponseDto;
import io.vestoria.service.ReportService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<ReportResponseDto> getReports(@RequestParam(defaultValue = "7") int days,
            Principal principal) {
        return ResponseEntity.ok(reportService.getReports(principal.getName(), days));
    }
}
