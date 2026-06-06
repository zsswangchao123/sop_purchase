package com.sop.purchase.supportitem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupportItemRepository extends JpaRepository<SupportItem, Long> {
    @Query("""
            select i from SupportItem i
            where (:enabled is null or i.enabled = :enabled)
              and (:keyword = '' or lower(i.code) like lower(concat('%', :keyword, '%'))
                   or lower(i.name) like lower(concat('%', :keyword, '%')))
            """)
    Page<SupportItem> search(@Param("keyword") String keyword, @Param("enabled") Boolean enabled, Pageable pageable);
}
