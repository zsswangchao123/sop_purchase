package com.sop.purchase.rule;

import com.sop.purchase.common.ApiResponse;
import com.sop.purchase.rule.dto.RuleDtos.CreateRuleRequest;
import com.sop.purchase.rule.dto.RuleDtos.EnabledRequest;
import com.sop.purchase.rule.dto.RuleDtos.RuleResponse;
import com.sop.purchase.rule.dto.RuleDtos.UpdateRuleRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductSupportRuleController {
    private final ProductSupportRuleService service;

    public ProductSupportRuleController(ProductSupportRuleService service) {
        this.service = service;
    }

    @GetMapping("/api/products/{productId}/support-rules")
    public ApiResponse<List<RuleResponse>> listByProduct(@PathVariable Long productId) {
        return ApiResponse.ok(service.listByProduct(productId));
    }

    @PostMapping("/api/products/{productId}/support-rules")
    public ApiResponse<RuleResponse> create(@PathVariable Long productId, @Valid @RequestBody CreateRuleRequest request) {
        return ApiResponse.ok(service.create(productId, request));
    }

    @PutMapping("/api/support-rules/{id}")
    public ApiResponse<RuleResponse> update(@PathVariable Long id, @RequestBody UpdateRuleRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @PatchMapping("/api/support-rules/{id}/enabled")
    public ApiResponse<RuleResponse> setEnabled(@PathVariable Long id, @RequestBody EnabledRequest request) {
        return ApiResponse.ok(service.setEnabled(id, request.enabled()));
    }

    @DeleteMapping("/api/support-rules/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok(null);
    }
}
