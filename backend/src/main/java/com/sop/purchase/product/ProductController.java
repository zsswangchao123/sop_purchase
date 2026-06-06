package com.sop.purchase.product;

import com.sop.purchase.common.ApiResponse;
import com.sop.purchase.common.PageResponse;
import com.sop.purchase.product.dto.ProductDtos.CreateProductRequest;
import com.sop.purchase.product.dto.ProductDtos.EnabledRequest;
import com.sop.purchase.product.dto.ProductDtos.ProductResponse;
import com.sop.purchase.product.dto.ProductDtos.UpdateProductRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> list(@RequestParam(defaultValue = "") String keyword,
                                                   @RequestParam(required = false) Boolean enabled,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(PageResponse.from(service.search(keyword, enabled, PageRequest.of(Math.max(page, 0),
                Math.min(Math.max(size, 1), 100), Sort.by("id").descending()))));
    }

    @PostMapping
    public ApiResponse<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @RequestBody UpdateProductRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<ProductResponse> setEnabled(@PathVariable Long id, @RequestBody EnabledRequest request) {
        return ApiResponse.ok(service.setEnabled(id, request.enabled()));
    }
}
