package com.sop.purchase.purchaselist;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseListItemRepository extends JpaRepository<PurchaseListItem, Long> {
    List<PurchaseListItem> findByPurchaseListId(Long purchaseListId);
}
