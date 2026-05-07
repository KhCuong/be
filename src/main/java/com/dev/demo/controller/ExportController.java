package com.dev.demo.controller;

import com.dev.demo.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/class/{classCode}/excel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<byte[]> exportClassExcel(@PathVariable String classCode) {
        byte[] excelData = exportService.exportClassListToExcel(classCode);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Danh_Sach_Lop_" + classCode + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    @GetMapping("/student/{studentCode}/pdf")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STUDENT')")
    public ResponseEntity<byte[]> exportStudentTranscriptPdf(@PathVariable String studentCode) {
        byte[] pdfData = exportService.exportTranscriptToPdf(studentCode);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Bang_Diem_" + studentCode + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}