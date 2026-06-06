package com.sop.purchase.purchaselist.dto;

import com.sop.purchase.purchaselist.PurchaseListStatus;
import com.sop.purchase.rule.CalcType;
import com.sop.purchase.rule.RoundingMode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PurchaseListDtos {
    public record GenerateProductInput(Long productId, String productCode, String productName,
                                       String unit, BigDecimal quantity, String remark) {
    }

    public record GeneratePurchaseListRequest(List<GenerateProductInput> items, String remark) {
    }

    public record PriceUpdateRequest(BigDecimal actualUnitPrice, String remark) {
    }

    public record RuleInput(Long ruleId, Long productId, Long supportItemId, String supportItemCode,
                            String supportItemName, String supportItemUnit, BigDecimal defaultPrice,
                            Boolean supportItemEnabled, CalcType calcType, BigDecimal baseQuantity,
                            BigDecimal supportQuantity, RoundingMode roundingMode) {
    }

    public record CalculatedSource(Long productId, Long ruleId, BigDecimal calculatedQuantity) {
    }

    public record CalculatedSupportItem(Long supportItemId, String code, String name, String unit,
                                        BigDecimal quantity, BigDecimal defaultPrice, BigDecimal actualPrice,
                                        BigDecimal amount, List<CalculatedSource> sources) {
    }

    public record PurchaseListProductResponse(Long id, Long productId, String productCode, String productName,
                                              String unit, BigDecimal quantity, String remark) {
    }

    public record PurchaseListItemResponse(Long id, Long supportItemId, String supportItemCode,
                                           String supportItemName, String unit, BigDecimal quantity,
                                           BigDecimal defaultUnitPrice, BigDecimal actualUnitPrice,
                                           BigDecimal amount, String remark) {
    }

    public record PurchaseListResponse(Long id, String listNo, PurchaseListStatus status, BigDecimal totalAmount,
                                       String remark, LocalDateTime createdAt, List<PurchaseListProductResponse> products,
                                       List<PurchaseListItemResponse> items, List<String> warnings) {
    }
}
