package com.sop.purchase.rule;

import com.sop.purchase.common.BusinessException;
import com.sop.purchase.product.Product;
import com.sop.purchase.product.ProductService;
import com.sop.purchase.rule.dto.RuleDtos.CreateRuleRequest;
import com.sop.purchase.rule.dto.RuleDtos.RuleResponse;
import com.sop.purchase.rule.dto.RuleDtos.UpdateRuleRequest;
import com.sop.purchase.supportitem.SupportItem;
import com.sop.purchase.supportitem.SupportItemService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductSupportRuleService {
    private final ProductSupportRuleRepository repository;
    private final ProductService productService;
    private final SupportItemService supportItemService;

    public ProductSupportRuleService(ProductSupportRuleRepository repository, ProductService productService,
                                     SupportItemService supportItemService) {
        this.repository = repository;
        this.productService = productService;
        this.supportItemService = supportItemService;
    }

    @Transactional(readOnly = true)
    public List<RuleResponse> listByProduct(Long productId) {
        productService.get(productId);
        return repository.findByProductId(productId).stream().map(RuleResponse::from).toList();
    }

    @Transactional
    public RuleResponse create(Long productId, CreateRuleRequest request) {
        Product product = productService.get(productId);
        SupportItem supportItem = supportItemService.get(request.supportItemId());
        validate(request.calcType(), request.baseQuantity(), request.supportQuantity());
        ProductSupportRule rule = new ProductSupportRule(product, supportItem, request.calcType(),
                normalizedBaseQuantity(request.calcType(), request.baseQuantity()), request.supportQuantity(),
                request.roundingMode(), request.remark());
        LocalDateTime now = LocalDateTime.now();
        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);
        return RuleResponse.from(repository.save(rule));
    }

    @Transactional
    public RuleResponse update(Long id, UpdateRuleRequest request) {
        ProductSupportRule rule = get(id);
        CalcType calcType = request.calcType() == null ? rule.getCalcType() : request.calcType();
        BigDecimal baseQuantity = request.baseQuantity() == null ? rule.getBaseQuantity() : request.baseQuantity();
        BigDecimal supportQuantity = request.supportQuantity() == null ? rule.getSupportQuantity() : request.supportQuantity();
        validate(calcType, baseQuantity, supportQuantity);
        if (request.supportItemId() != null) {
            rule.setSupportItem(supportItemService.get(request.supportItemId()));
        }
        rule.setCalcType(calcType);
        rule.setBaseQuantity(normalizedBaseQuantity(calcType, baseQuantity));
        rule.setSupportQuantity(supportQuantity);
        if (request.roundingMode() != null) {
            rule.setRoundingMode(request.roundingMode());
        }
        if (request.enabled() != null) {
            rule.setEnabled(request.enabled());
        }
        rule.setRemark(request.remark());
        rule.setUpdatedAt(LocalDateTime.now());
        return RuleResponse.from(rule);
    }

    @Transactional
    public RuleResponse setEnabled(Long id, Boolean enabled) {
        ProductSupportRule rule = get(id);
        rule.setEnabled(Boolean.TRUE.equals(enabled));
        rule.setUpdatedAt(LocalDateTime.now());
        return RuleResponse.from(rule);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(get(id));
    }

    public ProductSupportRule get(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException("Support rule not found"));
    }

    private void validate(CalcType calcType, BigDecimal baseQuantity, BigDecimal supportQuantity) {
        if (supportQuantity == null || supportQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Support quantity must be greater than zero");
        }
        if (calcType == CalcType.RATIO && (baseQuantity == null || baseQuantity.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new BusinessException("Ratio rule requires base quantity greater than zero");
        }
    }

    private BigDecimal normalizedBaseQuantity(CalcType calcType, BigDecimal baseQuantity) {
        return calcType == CalcType.FIXED ? null : baseQuantity;
    }
}
