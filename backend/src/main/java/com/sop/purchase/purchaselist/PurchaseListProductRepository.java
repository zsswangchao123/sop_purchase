package com.sop.purchase.purchaselist;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseListProductRepository extends JpaRepository<PurchaseListProduct, Long> {
    List<PurchaseListProduct> findByPurchaseListId(Long purchaseListId);
}
