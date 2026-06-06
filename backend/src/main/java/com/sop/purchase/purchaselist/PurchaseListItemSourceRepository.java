package com.sop.purchase.purchaselist;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseListItemSourceRepository extends JpaRepository<PurchaseListItemSource, Long> {
    List<PurchaseListItemSource> findByPurchaseListItemId(Long purchaseListItemId);
}
