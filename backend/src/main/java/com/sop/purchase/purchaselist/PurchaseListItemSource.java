package com.sop.purchase.purchaselist;

import com.sop.purchase.rule.ProductSupportRule;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
public class PurchaseListItemSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_list_item_id")
    private PurchaseListItem purchaseListItem;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_list_product_id")
    private PurchaseListProduct purchaseListProduct;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_support_rule_id")
    private ProductSupportRule productSupportRule;
    private BigDecimal calculatedQuantity;

    protected PurchaseListItemSource() {
    }

    public PurchaseListItemSource(PurchaseListItem purchaseListItem, PurchaseListProduct purchaseListProduct,
                                  ProductSupportRule productSupportRule, BigDecimal calculatedQuantity) {
        this.purchaseListItem = purchaseListItem;
        this.purchaseListProduct = purchaseListProduct;
        this.productSupportRule = productSupportRule;
        this.calculatedQuantity = calculatedQuantity;
    }

    public Long getId() { return id; }
    public PurchaseListItem getPurchaseListItem() { return purchaseListItem; }
    public PurchaseListProduct getPurchaseListProduct() { return purchaseListProduct; }
    public ProductSupportRule getProductSupportRule() { return productSupportRule; }
    public BigDecimal getCalculatedQuantity() { return calculatedQuantity; }
}
