package com.sop.purchase.purchaselist;

import com.sop.purchase.product.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
public class PurchaseListProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_list_id")
    private PurchaseList purchaseList;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    private String productCodeSnapshot;
    private String productNameSnapshot;
    private String unitSnapshot;
    private BigDecimal quantity;
    private String remark;

    protected PurchaseListProduct() {
    }

    public PurchaseListProduct(PurchaseList purchaseList, Product product, BigDecimal quantity, String remark) {
        this.purchaseList = purchaseList;
        this.product = product;
        this.productCodeSnapshot = product.getCode();
        this.productNameSnapshot = product.getName();
        this.unitSnapshot = product.getUnit();
        this.quantity = quantity;
        this.remark = remark;
    }

    public Long getId() { return id; }
    public PurchaseList getPurchaseList() { return purchaseList; }
    public Product getProduct() { return product; }
    public String getProductCodeSnapshot() { return productCodeSnapshot; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public String getUnitSnapshot() { return unitSnapshot; }
    public BigDecimal getQuantity() { return quantity; }
    public String getRemark() { return remark; }
}
