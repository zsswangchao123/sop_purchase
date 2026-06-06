package com.sop.purchase.rule;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSupportRuleRepository extends JpaRepository<ProductSupportRule, Long> {
    List<ProductSupportRule> findByProductId(Long productId);
    List<ProductSupportRule> findByProductIdAndEnabledTrue(Long productId);
}
