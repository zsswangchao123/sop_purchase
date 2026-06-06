package com.sop.purchase.purchaselist;

import com.sop.purchase.supportitem.SupportItem;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
public class PurchaseListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_list_id")
    private PurchaseList purchaseList;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_item_id")
    private SupportItem supportItem;
    private String supportItemCodeSnapshot;
    private String supportItemNameSnapshot;
    private String unitSnapshot;
    private BigDecimal quantity;
    private BigDecimal defaultUnitPriceSnapshot;
    private BigDecimal actualUnitPrice;
    private BigDecimal amount;
    private String remark;

    protected PurchaseListItem() {
    }

    public PurchaseListItem(PurchaseList purchaseList, SupportItem supportItem, BigDecimal quantity) {
        this.purchaseList = purchaseList;
        this.supportItem = supportItem;
        this.supportItemCodeSnapshot = supportItem.getCode();
        this.supportItemNameSnapshot = supportItem.getName();
        this.unitSnapshot = supportItem.getUnit();
        this.quantity = quantity;
        this.defaultUnitPriceSnapshot = supportItem.getDefaultPrice();
        this.actualUnitPrice = supportItem.getDefaultPrice();
        this.amount = quantity.multiply(actualUnitPrice).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public PurchaseList getPurchaseList() { return purchaseList; }
    public SupportItem getSupportItem() { return supportItem; }
    public String getSupportItemCodeSnapshot() { return supportItemCodeSnapshot; }
    public String getSupportItemNameSnapshot() { return supportItemNameSnapshot; }
    public String getUnitSnapshot() { return unitSnapshot; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getDefaultUnitPriceSnapshot() { return defaultUnitPriceSnapshot; }
    public BigDecimal getActualUnitPrice() { return actualUnitPrice; }
    public void setActualUnitPrice(BigDecimal actualUnitPrice) { this.actualUnitPrice = actualUnitPrice; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
