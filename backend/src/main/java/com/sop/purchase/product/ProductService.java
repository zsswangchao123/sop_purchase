package com.sop.purchase.product;

import com.sop.purchase.common.BusinessException;
import com.sop.purchase.product.dto.ProductDtos.CreateProductRequest;
import com.sop.purchase.product.dto.ProductDtos.ProductResponse;
import com.sop.purchase.product.dto.ProductDtos.UpdateProductRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<ProductResponse> list() {
        return repository.findAll().stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String keyword, Boolean enabled, Pageable pageable) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : "";
        return repository.search(normalizedKeyword, enabled, pageable).map(ProductResponse::from);
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        repository.findByCode(request.code()).ifPresent(product -> {
            throw new BusinessException("Product code already exists");
        });
        Product product = new Product(request.code(), request.name(), request.specification(), request.unit(), request.remark());
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        return ProductResponse.from(repository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = get(id);
        if (StringUtils.hasText(request.name())) {
            product.setName(request.name());
        }
        if (StringUtils.hasText(request.unit())) {
            product.setUnit(request.unit());
        }
        product.setSpecification(request.specification());
        product.setRemark(request.remark());
        if (request.enabled() != null) {
            product.setEnabled(request.enabled());
        }
        product.setUpdatedAt(LocalDateTime.now());
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse setEnabled(Long id, Boolean enabled) {
        Product product = get(id);
        product.setEnabled(Boolean.TRUE.equals(enabled));
        product.setUpdatedAt(LocalDateTime.now());
        return ProductResponse.from(product);
    }

    public Product get(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException("Product not found"));
    }
}
