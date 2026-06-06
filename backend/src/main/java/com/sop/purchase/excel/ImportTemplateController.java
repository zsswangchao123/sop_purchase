package com.sop.purchase.excel;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import-templates")
public class ImportTemplateController {
    private final ExcelExportService exportService;

    public ImportTemplateController(ExcelExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/products")
    public ResponseEntity<byte[]> productTemplate() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("product-import-template.xlsx").build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(exportService.template());
    }
}
