package com.sop.purchase.rule.dto;

import com.sop.purchase.rule.CalcType;
import com.sop.purchase.rule.ProductSupportRule;
import com.sop.purchase.rule.RoundingMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class RuleDtos {
    public record CreateRuleRequest(
            @NotNull Long supportItemId,
            @NotNull CalcType calcType,
            BigDecimal baseQuantity,
            @NotNull @Positive BigDecimal supportQuantity,
            @NotNull RoundingMode roundingMode,
            String remark
    ) {
    }

    public record UpdateRuleRequest(Long supportItemId, CalcType calcType, BigDecimal baseQuantity,
                                    BigDecimal supportQuantity, RoundingMode roundingMode,
                                    Boolean enabled, String remark) {
    }

    public record EnabledRequest(Boolean enabled) {
    }

    public record RuleResponse(Long id, Long productId, String productName, Long supportItemId,
                               String supportItemName, CalcType calcType, BigDecimal baseQuantity,
                               BigDecimal supportQuantity, RoundingMode roundingMode,
                               Boolean enabled, String remark) {
        public static RuleResponse from(ProductSupportRule rule) {
            return new RuleResponse(
                    rule.getId(),
                    rule.getProduct().getId(),
                    rule.getProduct().getName(),
                    rule.getSupportItem().getId(),
                    rule.getSupportItem().getName(),
                    rule.getCalcType(),
                    rule.getBaseQuantity(),
                    rule.getSupportQuantity(),
                    rule.getRoundingMode(),
                    rule.getEnabled(),
                    rule.getRemark()
            );
        }
    }
}
