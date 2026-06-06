package com.sop.purchase.purchaselist;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseListRepository extends JpaRepository<PurchaseList, Long> {
    List<PurchaseList> findByStatus(PurchaseListStatus status);

    @Query("""
            select p from PurchaseList p
            where (:status is null or p.status = :status)
              and (:keyword = '' or lower(p.listNo) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(p.remark, '')) like lower(concat('%', :keyword, '%')))
              and (:startTime is null or p.createdAt >= :startTime)
              and (:endTime is null or p.createdAt < :endTime)
            """)
    Page<PurchaseList> search(@Param("status") PurchaseListStatus status, @Param("keyword") String keyword,
                              @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
                              Pageable pageable);
}
