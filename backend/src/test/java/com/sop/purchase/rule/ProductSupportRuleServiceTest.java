package com.sop.purchase.rule;

import static org.assertj.core.api.Assertions.assertThat;

import com.sop.purchase.product.ProductService;
import com.sop.purchase.product.dto.ProductDtos.CreateProductRequest;
import com.sop.purchase.rule.dto.RuleDtos.CreateRuleRequest;
import com.sop.purchase.supportitem.SupportItemService;
import com.sop.purchase.supportitem.dto.SupportItemDtos.CreateSupportItemRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProductSupportRuleServiceTest {
    @Autowired
    private ProductService productService;
    @Autowired
    private SupportItemService supportItemService;
    @Autowired
    private ProductSupportRuleService ruleService;

    @Test
    void listByProductReturnsDisabledRulesAndMapsLazyFieldsInsideTransaction() {
        Long productId = productService.create(new CreateProductRequest("RULE-P-1", "Rule Product", null, "box", null)).id();
        Long supportItemId = supportItemService.create(new CreateSupportItemRequest("RULE-S-1", "Rule Support",
                "pcs", new BigDecimal("1.00"), null)).id();
        Long ruleId = ruleService.create(productId, new CreateRuleRequest(supportItemId, CalcType.RATIO,
                BigDecimal.ONE, new BigDecimal("2"), RoundingMode.CEIL, null)).id();

        ruleService.setEnabled(ruleId, false);

        var rules = ruleService.listByProduct(productId);

        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).supportItemName()).isEqualTo("Rule Support");
        assertThat(rules.get(0).enabled()).isFalse();
    }
}
