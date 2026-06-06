package com.sop.purchase.supportitem;

import com.sop.purchase.common.BusinessException;
import com.sop.purchase.supportitem.dto.SupportItemDtos.CreateSupportItemRequest;
import com.sop.purchase.supportitem.dto.SupportItemDtos.SupportItemResponse;
import com.sop.purchase.supportitem.dto.SupportItemDtos.UpdateSupportItemRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SupportItemService {
    private final SupportItemRepository repository;

    public SupportItemService(SupportItemRepository repository) {
        this.repository = repository;
    }

    public List<SupportItemResponse> list() {
        return repository.findAll().stream().map(SupportItemResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<SupportItemResponse> search(String keyword, Boolean enabled, Pageable pageable) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : "";
        return repository.search(normalizedKeyword, enabled, pageable).map(SupportItemResponse::from);
    }

    @Transactional
    public SupportItemResponse create(CreateSupportItemRequest request) {
        rejectNegative(request.defaultPrice());
        SupportItem item = new SupportItem(request.code(), request.name(), request.unit(), request.defaultPrice(), request.remark());
        LocalDateTime now = LocalDateTime.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        return SupportItemResponse.from(repository.save(item));
    }

    @Transactional
    public SupportItemResponse update(Long id, UpdateSupportItemRequest request) {
        SupportItem item = get(id);
        if (StringUtils.hasText(request.name())) {
            item.setName(request.name());
        }
        if (StringUtils.hasText(request.unit())) {
            item.setUnit(request.unit());
        }
        if (request.defaultPrice() != null) {
            rejectNegative(request.defaultPrice());
            item.setDefaultPrice(request.defaultPrice());
        }
        item.setRemark(request.remark());
        if (request.enabled() != null) {
            item.setEnabled(request.enabled());
        }
        item.setUpdatedAt(LocalDateTime.now());
        return SupportItemResponse.from(item);
    }

    @Transactional
    public SupportItemResponse setEnabled(Long id, Boolean enabled) {
        SupportItem item = get(id);
        item.setEnabled(Boolean.TRUE.equals(enabled));
        item.setUpdatedAt(LocalDateTime.now());
        return SupportItemResponse.from(item);
    }

    public SupportItem get(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException("Support item not found"));
    }

    private void rejectNegative(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Default price cannot be negative");
        }
    }
}
