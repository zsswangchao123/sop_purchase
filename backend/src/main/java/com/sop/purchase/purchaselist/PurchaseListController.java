package com.sop.purchase.purchaselist;

import com.sop.purchase.common.ApiResponse;
import com.sop.purchase.common.PageResponse;
import com.sop.purchase.excel.ExcelExportService;
import com.sop.purchase.excel.ExcelImportService;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.GeneratePurchaseListRequest;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.PriceUpdateRequest;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.PurchaseListResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/purchase-lists")
public class PurchaseListController {
    private final PurchaseListService service;
    private final ExcelImportService excelImportService;
    private final ExcelExportService excelExportService;

    public PurchaseListController(PurchaseListService service, ExcelImportService excelImportService,
                                  ExcelExportService excelExportService) {
        this.service = service;
        this.excelImportService = excelImportService;
        this.excelExportService = excelExportService;
    }

    @PostMapping("/manual")
    public ApiResponse<PurchaseListResponse> generateManual(@Valid @RequestBody GeneratePurchaseListRequest request) {
        return ApiResponse.ok(service.generateManual(request));
    }

    @PostMapping("/excel")
    public ApiResponse<PurchaseListResponse> generateExcel(@RequestParam("file") MultipartFile file) throws Exception {
        return ApiResponse.ok(service.generateExcel(new GeneratePurchaseListRequest(
                excelImportService.parseProducts(file.getInputStream()), "Excel import")));
    }

    @GetMapping
    public ApiResponse<PageResponse<PurchaseListResponse>> list(@RequestParam(required = false) PurchaseListStatus status,
                                                        @RequestParam(defaultValue = "") String keyword,
                                                        @RequestParam(required = false)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                        @RequestParam(required = false)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(PageResponse.from(service.search(status, keyword,
                startDate == null ? null : startDate.atStartOfDay(),
                endDate == null ? null : endDate.plusDays(1).atStartOfDay(),
                PageRequest.of(Math.max(page, 0),
                Math.min(Math.max(size, 1), 100), Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseListResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.detail(id));
    }

    @PatchMapping("/{id}/items/{itemId}/price")
    public ApiResponse<PurchaseListResponse> updatePrice(@PathVariable Long id, @PathVariable Long itemId,
                                                         @RequestBody PriceUpdateRequest request) {
        return ApiResponse.ok(service.updatePrice(id, itemId, request));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<PurchaseListResponse> confirm(@PathVariable Long id) {
        return ApiResponse.ok(service.confirm(id));
    }

    @PostMapping("/{id}/mark-purchased")
    public ApiResponse<PurchaseListResponse> markPurchased(@PathVariable Long id) {
        return ApiResponse.ok(service.markPurchased(id));
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("purchase-list-" + id + ".xlsx").build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelExportService.exportPurchaseList(id));
    }
}
