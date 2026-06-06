package com.sop.purchase.rule;

import com.sop.purchase.product.Product;
import com.sop.purchase.supportitem.SupportItem;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class ProductSupportRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_item_id")
    private SupportItem supportItem;

    @Enumerated(EnumType.STRING)
    private CalcType calcType;
    private BigDecimal baseQuantity;
    private BigDecimal supportQuantity;
    @Enumerated(EnumType.STRING)
    private RoundingMode roundingMode;
    private Boolean enabled = true;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected ProductSupportRule() {
    }

    public ProductSupportRule(Product product, SupportItem supportItem, CalcType calcType,
                              BigDecimal baseQuantity, BigDecimal supportQuantity,
                              RoundingMode roundingMode, String remark) {
        this.product = product;
        this.supportItem = supportItem;
        this.calcType = calcType;
        this.baseQuantity = baseQuantity;
        this.supportQuantity = supportQuantity;
        this.roundingMode = roundingMode;
        this.remark = remark;
        this.enabled = true;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public SupportItem getSupportItem() { return supportItem; }
    public void setSupportItem(SupportItem supportItem) { this.supportItem = supportItem; }
    public CalcType getCalcType() { return calcType; }
    public void setCalcType(CalcType calcType) { this.calcType = calcType; }
    public BigDecimal getBaseQuantity() { return baseQuantity; }
    public void setBaseQuantity(BigDecimal baseQuantity) { this.baseQuantity = baseQuantity; }
    public BigDecimal getSupportQuantity() { return supportQuantity; }
    public void setSupportQuantity(BigDecimal supportQuantity) { this.supportQuantity = supportQuantity; }
    public RoundingMode getRoundingMode() { return roundingMode; }
    public void setRoundingMode(RoundingMode roundingMode) { this.roundingMode = roundingMode; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
