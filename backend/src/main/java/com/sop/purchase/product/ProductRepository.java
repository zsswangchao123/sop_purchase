package com.sop.purchase.product;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByCode(String code);
    List<Product> findByName(String name);

    @Query("""
            select p from Product p
            where (:enabled is null or p.enabled = :enabled)
              and (:keyword = '' or lower(p.code) like lower(concat('%', :keyword, '%'))
                   or lower(p.name) like lower(concat('%', :keyword, '%')))
            """)
    Page<Product> search(@Param("keyword") String keyword, @Param("enabled") Boolean enabled, Pageable pageable);
}
