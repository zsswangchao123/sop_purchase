package com.sop.purchase.purchaselist;

import static org.assertj.core.api.Assertions.assertThat;

import com.sop.purchase.purchaselist.dto.PurchaseListDtos.CalculatedSupportItem;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.GenerateProductInput;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.RuleInput;
import com.sop.purchase.rule.CalcType;
import com.sop.purchase.rule.RoundingMode;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PurchaseListCalculatorTest {
    private final PurchaseListCalculator calculator = new PurchaseListCalculator();

    @Test
    void fixedRuleAddsSupportQuantityOnceWhenProductExists() {
        List<CalculatedSupportItem> result = calculator.calculate(
                List.of(product(1L, "20")),
                List.of(rule(10L, 1L, 100L, CalcType.FIXED, null, "10", RoundingMode.CEIL))
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).quantity()).isEqualByComparingTo("10");
        assertThat(result.get(0).amount()).isEqualByComparingTo("5.00");
    }

    @Test
    void ratioRuleCalculatesByProductQuantity() {
        List<CalculatedSupportItem> result = calculator.calculate(
                List.of(product(1L, "20")),
                List.of(rule(10L, 1L, 100L, CalcType.RATIO, "1", "2", RoundingMode.CEIL))
        );

        assertThat(result.get(0).quantity()).isEqualByComparingTo("40");
    }

    @Test
    void ceilRoundingRoundsUpToWholeUnit() {
        List<CalculatedSupportItem> result = calculator.calculate(
                List.of(product(1L, "11")),
                List.of(rule(10L, 1L, 100L, CalcType.RATIO, "10", "1", RoundingMode.CEIL))
        );

        assertThat(result.get(0).quantity()).isEqualByComparingTo("2");
    }

    @Test
    void decimalRoundingKeepsTwoDecimals() {
        List<CalculatedSupportItem> result = calculator.calculate(
                List.of(product(1L, "1")),
                List.of(rule(10L, 1L, 100L, CalcType.RATIO, "3", "1", RoundingMode.DECIMAL))
        );

        assertThat(result.get(0).quantity()).isEqualByComparingTo("0.33");
    }

    @Test
    void sameSupportItemFromMultipleProductsIsMerged() {
        List<CalculatedSupportItem> result = calculator.calculate(
                List.of(product(1L, "20"), product(2L, "5")),
                List.of(
                        rule(10L, 1L, 100L, CalcType.RATIO, "1", "2", RoundingMode.CEIL),
                        rule(11L, 2L, 100L, CalcType.FIXED, null, "3", RoundingMode.CEIL)
                )
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).quantity()).isEqualByComparingTo("43");
        assertThat(result.get(0).sources()).hasSize(2);
    }

    private GenerateProductInput product(Long productId, String quantity) {
        return new GenerateProductInput(productId, "P" + productId, "Product" + productId, "box", new BigDecimal(quantity), null);
    }

    private RuleInput rule(Long ruleId, Long productId, Long supportItemId, CalcType calcType,
                           String baseQuantity, String supportQuantity, RoundingMode roundingMode) {
        return new RuleInput(ruleId, productId, supportItemId, "S" + supportItemId, "Support" + supportItemId,
                "pcs", new BigDecimal("0.50"), true, calcType,
                baseQuantity == null ? null : new BigDecimal(baseQuantity),
                new BigDecimal(supportQuantity), roundingMode);
    }
}
