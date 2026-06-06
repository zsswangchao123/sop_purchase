package com.sop.purchase.purchaselist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sop.purchase.common.BusinessException;
import com.sop.purchase.product.ProductService;
import com.sop.purchase.product.dto.ProductDtos.CreateProductRequest;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.GenerateProductInput;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.GeneratePurchaseListRequest;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.PriceUpdateRequest;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.PurchaseListResponse;
import com.sop.purchase.rule.CalcType;
import com.sop.purchase.rule.ProductSupportRuleService;
import com.sop.purchase.rule.RoundingMode;
import com.sop.purchase.rule.dto.RuleDtos.CreateRuleRequest;
import com.sop.purchase.supportitem.SupportItemService;
import com.sop.purchase.supportitem.dto.SupportItemDtos.CreateSupportItemRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PurchaseListServiceTest {
    @Autowired
    private ProductService productService;
    @Autowired
    private SupportItemService supportItemService;
    @Autowired
    private ProductSupportRuleService ruleService;
    @Autowired
    private PurchaseListService purchaseListService;

    @Test
    void manualGenerationSavesDraftPurchaseList() {
        Fixture fixture = createFixture("A");

        PurchaseListResponse response = purchaseListService.generateManual(new GeneratePurchaseListRequest(
                List.of(new GenerateProductInput(fixture.productId(), null, null, null, new BigDecimal("20"), null)),
                "monthly"
        ));

        assertThat(response.status()).isEqualTo(PurchaseListStatus.DRAFT);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualByComparingTo("40");
        assertThat(response.totalAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void productWithoutRulesIsAcceptedAndReturnedAsWarning() {
        Long productId = productService.create(new CreateProductRequest("P-NO-RULE", "No Rule", null, "box", null)).id();

        PurchaseListResponse response = purchaseListService.generateManual(new GeneratePurchaseListRequest(
                List.of(new GenerateProductInput(productId, null, null, null, new BigDecimal("1"), null)),
                null
        ));

        assertThat(response.items()).isEmpty();
        assertThat(response.warnings()).contains("Product has no enabled support rules: No Rule");
    }

    @Test
    void disabledSupportItemIsSkippedAndReturnedAsWarning() {
        Fixture fixture = createFixture("B");
        supportItemService.setEnabled(fixture.supportItemId(), false);

        PurchaseListResponse response = purchaseListService.generateManual(new GeneratePurchaseListRequest(
                List.of(new GenerateProductInput(fixture.productId(), null, null, null, new BigDecimal("20"), null)),
                null
        ));

        assertThat(response.items()).isEmpty();
        assertThat(response.warnings()).contains("Disabled support item skipped: Ice Pack B");
    }

    @Test
    void draftListAllowsActualUnitPriceChanges() {
        Fixture fixture = createFixture("C");
        PurchaseListResponse draft = purchaseListService.generateManual(new GeneratePurchaseListRequest(
                List.of(new GenerateProductInput(fixture.productId(), null, null, null, new BigDecimal("20"), null)),
                null
        ));

        PurchaseListResponse updated = purchaseListService.updatePrice(draft.id(), draft.items().get(0).id(),
                new PriceUpdateRequest(new BigDecimal("0.60"), "actual"));

        assertThat(updated.items().get(0).actualUnitPrice()).isEqualByComparingTo("0.60");
        assertThat(updated.totalAmount()).isEqualByComparingTo("24.00");
    }

    @Test
    void confirmedListRejectsPriceChanges() {
        Fixture fixture = createFixture("D");
        PurchaseListResponse draft = purchaseListService.generateManual(new GeneratePurchaseListRequest(
                List.of(new GenerateProductInput(fixture.productId(), null, null, null, new BigDecimal("20"), null)),
                null
        ));
        purchaseListService.confirm(draft.id());

        assertThatThrownBy(() -> purchaseListService.updatePrice(draft.id(), draft.items().get(0).id(),
                new PriceUpdateRequest(new BigDecimal("0.60"), null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only draft");
    }

    @Test
    void confirmedListCanBecomePurchased() {
        Fixture fixture = createFixture("E");
        PurchaseListResponse draft = purchaseListService.generateManual(new GeneratePurchaseListRequest(
                List.of(new GenerateProductInput(fixture.productId(), null, null, null, new BigDecimal("20"), null)),
                null
        ));

        PurchaseListResponse confirmed = purchaseListService.confirm(draft.id());
        PurchaseListResponse purchased = purchaseListService.markPurchased(confirmed.id());

        assertThat(purchased.status()).isEqualTo(PurchaseListStatus.PURCHASED);
    }

    @Test
    void purchasedListCannotReturnToDraft() {
        Fixture fixture = createFixture("F");
        PurchaseListResponse draft = purchaseListService.generateManual(new GeneratePurchaseListRequest(
                List.of(new GenerateProductInput(fixture.productId(), null, null, null, new BigDecimal("20"), null)),
                null
        ));
        PurchaseListResponse confirmed = purchaseListService.confirm(draft.id());
        PurchaseListResponse purchased = purchaseListService.markPurchased(confirmed.id());

        assertThatThrownBy(() -> purchaseListService.confirm(purchased.id()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only draft");
    }

    @Test
    void searchFiltersByStatusAndRemark() {
        Fixture fixture = createFixture("SEARCH");
        PurchaseListResponse draft = purchaseListService.generateManual(new GeneratePurchaseListRequest(
                List.of(new GenerateProductInput(fixture.productId(), null, null, null, BigDecimal.ONE, null)),
                "weekly vegetable order"
        ));
        purchaseListService.confirm(draft.id());

        var page = purchaseListService.search(PurchaseListStatus.CONFIRMED, "vegetable", null, null,
                PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).id()).isEqualTo(draft.id());
    }

    @Test
    void searchFiltersByCreatedAtRange() {
        Fixture fixture = createFixture("DATE");
        PurchaseListResponse response = purchaseListService.generateManual(new GeneratePurchaseListRequest(
                List.of(new GenerateProductInput(fixture.productId(), null, null, null, BigDecimal.ONE, null)),
                "date range order"
        ));

        var included = purchaseListService.search(null, "", LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1), PageRequest.of(0, 20));
        var excluded = purchaseListService.search(null, "", LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2), PageRequest.of(0, 20));

        assertThat(included.getContent()).extracting("id").contains(response.id());
        assertThat(excluded.getContent()).extracting("id").doesNotContain(response.id());
        assertThat(response.createdAt()).isNotNull();
    }

    private Fixture createFixture(String suffix) {
        Long productId = productService.create(new CreateProductRequest("P-" + suffix, "Broccoli " + suffix, null, "box", null)).id();
        Long supportItemId = supportItemService.create(new CreateSupportItemRequest("S-" + suffix, "Ice Pack " + suffix,
                "pcs", new BigDecimal("0.50"), null)).id();
        ruleService.create(productId, new CreateRuleRequest(supportItemId, CalcType.RATIO,
                BigDecimal.ONE, new BigDecimal("2"), RoundingMode.CEIL, null));
        return new Fixture(productId, supportItemId);
    }

    private record Fixture(Long productId, Long supportItemId) {
    }
}
