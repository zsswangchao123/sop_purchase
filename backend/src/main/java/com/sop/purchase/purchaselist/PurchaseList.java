package com.sop.purchase.purchaselist;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class PurchaseList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String listNo;
    @Enumerated(EnumType.STRING)
    private PurchaseListStatus status = PurchaseListStatus.DRAFT;
    @Enumerated(EnumType.STRING)
    private SourceType sourceType = SourceType.MANUAL;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String remark;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime purchasedAt;

    protected PurchaseList() {
    }

    public PurchaseList(String listNo, SourceType sourceType, String remark) {
        this.listNo = listNo;
        this.sourceType = sourceType;
        this.remark = remark;
        this.status = PurchaseListStatus.DRAFT;
        this.totalAmount = BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public String getListNo() { return listNo; }
    public void setListNo(String listNo) { this.listNo = listNo; }
    public PurchaseListStatus getStatus() { return status; }
    public void setStatus(PurchaseListStatus status) { this.status = status; }
    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    public LocalDateTime getPurchasedAt() { return purchasedAt; }
    public void setPurchasedAt(LocalDateTime purchasedAt) { this.purchasedAt = purchasedAt; }
}
