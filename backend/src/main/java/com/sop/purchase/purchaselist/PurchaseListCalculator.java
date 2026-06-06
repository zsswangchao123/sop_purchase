package com.sop.purchase.purchaselist;

import com.sop.purchase.purchaselist.dto.PurchaseListDtos.CalculatedSource;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.CalculatedSupportItem;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.GenerateProductInput;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.RuleInput;
import com.sop.purchase.rule.CalcType;
import com.sop.purchase.rule.RoundingMode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PurchaseListCalculator {
    public List<CalculatedSupportItem> calculate(List<GenerateProductInput> products, List<RuleInput> rules) {
        Map<Long, GenerateProductInput> productsById = new LinkedHashMap<>();
        for (GenerateProductInput product : products) {
            productsById.put(product.productId(), product);
        }

        Map<Long, MutableSupportItem> merged = new LinkedHashMap<>();
        for (RuleInput rule : rules) {
            GenerateProductInput product = productsById.get(rule.productId());
            if (product == null || !Boolean.TRUE.equals(rule.supportItemEnabled())) {
                continue;
            }
            BigDecimal finalQuantity = applyRounding(calculateRawQuantity(product.quantity(), rule), rule.roundingMode());
            MutableSupportItem item = merged.computeIfAbsent(rule.supportItemId(), id -> new MutableSupportItem(rule));
            item.quantity = item.quantity.add(finalQuantity);
            item.sources.add(new CalculatedSource(rule.productId(), rule.ruleId(), finalQuantity));
        }

        return merged.values().stream().map(MutableSupportItem::toResponse).toList();
    }

    private BigDecimal calculateRawQuantity(BigDecimal productQuantity, RuleInput rule) {
        if (rule.calcType() == CalcType.FIXED) {
            return rule.supportQuantity();
        }
        return productQuantity
                .divide(rule.baseQuantity(), 8, java.math.RoundingMode.HALF_UP)
                .multiply(rule.supportQuantity());
    }

    private BigDecimal applyRounding(BigDecimal rawQuantity, RoundingMode roundingMode) {
        if (roundingMode == RoundingMode.CEIL) {
            return rawQuantity.setScale(0, java.math.RoundingMode.CEILING);
        }
        return rawQuantity.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private static class MutableSupportItem {
        private final Long supportItemId;
        private final String code;
        private final String name;
        private final String unit;
        private final BigDecimal defaultPrice;
        private BigDecimal quantity = BigDecimal.ZERO;
        private final List<CalculatedSource> sources = new ArrayList<>();

        private MutableSupportItem(RuleInput rule) {
            this.supportItemId = rule.supportItemId();
            this.code = rule.supportItemCode();
            this.name = rule.supportItemName();
            this.unit = rule.supportItemUnit();
            this.defaultPrice = rule.defaultPrice();
        }

        private CalculatedSupportItem toResponse() {
            BigDecimal amount = quantity.multiply(defaultPrice).setScale(2, java.math.RoundingMode.HALF_UP);
            return new CalculatedSupportItem(supportItemId, code, name, unit, quantity, defaultPrice, defaultPrice, amount, sources);
        }
    }
}
