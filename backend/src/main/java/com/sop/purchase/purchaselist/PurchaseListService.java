package com.sop.purchase.purchaselist;

import com.sop.purchase.common.BusinessException;
import com.sop.purchase.product.Product;
import com.sop.purchase.product.ProductRepository;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.CalculatedSupportItem;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.GenerateProductInput;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.GeneratePurchaseListRequest;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.PriceUpdateRequest;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.PurchaseListItemResponse;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.PurchaseListProductResponse;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.PurchaseListResponse;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.RuleInput;
import com.sop.purchase.rule.ProductSupportRule;
import com.sop.purchase.rule.ProductSupportRuleRepository;
import com.sop.purchase.supportitem.SupportItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PurchaseListService {
    private final ProductRepository productRepository;
    private final ProductSupportRuleRepository ruleRepository;
    private final PurchaseListRepository listRepository;
    private final PurchaseListProductRepository productLineRepository;
    private final PurchaseListItemRepository itemRepository;
    private final PurchaseListItemSourceRepository sourceRepository;
    private final PurchaseListCalculator calculator;

    public PurchaseListService(ProductRepository productRepository, ProductSupportRuleRepository ruleRepository,
                               PurchaseListRepository listRepository,
                               PurchaseListProductRepository productLineRepository,
                               PurchaseListItemRepository itemRepository,
                               PurchaseListItemSourceRepository sourceRepository,
                               PurchaseListCalculator calculator) {
        this.productRepository = productRepository;
        this.ruleRepository = ruleRepository;
        this.listRepository = listRepository;
        this.productLineRepository = productLineRepository;
        this.itemRepository = itemRepository;
        this.sourceRepository = sourceRepository;
        this.calculator = calculator;
    }

    @Transactional
    public PurchaseListResponse generateManual(GeneratePurchaseListRequest request) {
        return generate(request, SourceType.MANUAL);
    }

    @Transactional
    public PurchaseListResponse generateExcel(GeneratePurchaseListRequest request) {
        return generate(request, SourceType.EXCEL);
    }

    private PurchaseListResponse generate(GeneratePurchaseListRequest request, SourceType sourceType) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessException("At least one product is required");
        }
        List<String> warnings = new ArrayList<>();
        PurchaseList list = new PurchaseList(nextListNo(), sourceType, request.remark());
        LocalDateTime now = LocalDateTime.now();
        list.setCreatedAt(now);
        list.setUpdatedAt(now);
        list = listRepository.save(list);

        Map<Long, Product> products = new LinkedHashMap<>();
        List<PurchaseListProduct> productLines = new ArrayList<>();
        for (GenerateProductInput input : request.items()) {
            if (input.quantity() == null || input.quantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Product quantity must be greater than zero");
            }
            Product product = productRepository.findById(input.productId())
                    .orElseThrow(() -> new BusinessException("Product not found: " + input.productId()));
            if (!Boolean.TRUE.equals(product.getEnabled())) {
                throw new BusinessException("Product is disabled: " + product.getName());
            }
            products.put(product.getId(), product);
            productLines.add(new PurchaseListProduct(list, product, input.quantity(), input.remark()));
        }
        productLines = productLineRepository.saveAll(productLines);

        List<RuleInput> ruleInputs = new ArrayList<>();
        Map<Long, ProductSupportRule> rulesById = new LinkedHashMap<>();
        for (Product product : products.values()) {
            List<ProductSupportRule> rules = ruleRepository.findByProductIdAndEnabledTrue(product.getId());
            if (rules.isEmpty()) {
                warnings.add("Product has no enabled support rules: " + product.getName());
            }
            for (ProductSupportRule rule : rules) {
                SupportItem item = rule.getSupportItem();
                if (!Boolean.TRUE.equals(item.getEnabled())) {
                    warnings.add("Disabled support item skipped: " + item.getName());
                }
                rulesById.put(rule.getId(), rule);
                ruleInputs.add(new RuleInput(rule.getId(), product.getId(), item.getId(), item.getCode(),
                        item.getName(), item.getUnit(), item.getDefaultPrice(), item.getEnabled(),
                        rule.getCalcType(), rule.getBaseQuantity(), rule.getSupportQuantity(), rule.getRoundingMode()));
            }
        }

        List<GenerateProductInput> calculatorInputs = productLines.stream()
                .map(line -> new GenerateProductInput(line.getProduct().getId(), line.getProductCodeSnapshot(),
                        line.getProductNameSnapshot(), line.getUnitSnapshot(), line.getQuantity(), line.getRemark()))
                .toList();
        List<CalculatedSupportItem> calculatedItems = calculator.calculate(calculatorInputs, ruleInputs);
        Map<Long, PurchaseListProduct> productLineByProductId = new LinkedHashMap<>();
        for (PurchaseListProduct line : productLines) {
            productLineByProductId.put(line.getProduct().getId(), line);
        }

        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseListItem> savedItems = new ArrayList<>();
        for (CalculatedSupportItem calculated : calculatedItems) {
            SupportItem supportItem = rulesById.values().stream()
                    .filter(rule -> rule.getSupportItem().getId().equals(calculated.supportItemId()))
                    .findFirst()
                    .map(ProductSupportRule::getSupportItem)
                    .orElseThrow(() -> new BusinessException("Support item not found during calculation"));
            PurchaseListItem item = new PurchaseListItem(list, supportItem, calculated.quantity());
            item.setActualUnitPrice(calculated.actualPrice());
            item.setAmount(calculated.amount());
            savedItems.add(itemRepository.save(item));
            total = total.add(calculated.amount());
            for (var source : calculated.sources()) {
                PurchaseListProduct sourceProduct = productLineByProductId.get(source.productId());
                ProductSupportRule sourceRule = rulesById.get(source.ruleId());
                sourceRepository.save(new PurchaseListItemSource(item, sourceProduct, sourceRule, source.calculatedQuantity()));
            }
        }
        list.setTotalAmount(total.setScale(2, java.math.RoundingMode.HALF_UP));
        list.setUpdatedAt(LocalDateTime.now());
        return toResponse(list, productLines, savedItems, warnings);
    }

    @Transactional
    public PurchaseListResponse updatePrice(Long listId, Long itemId, PriceUpdateRequest request) {
        if (request.actualUnitPrice() == null || request.actualUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Actual unit price cannot be negative");
        }
        PurchaseList list = getList(listId);
        requireDraft(list);
        PurchaseListItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("Purchase list item not found"));
        if (!item.getPurchaseList().getId().equals(listId)) {
            throw new BusinessException("Purchase list item does not belong to this list");
        }
        item.setActualUnitPrice(request.actualUnitPrice());
        item.setAmount(item.getQuantity().multiply(request.actualUnitPrice()).setScale(2, java.math.RoundingMode.HALF_UP));
        item.setRemark(request.remark());
        recalculateTotal(list);
        return detail(listId);
    }

    @Transactional
    public PurchaseListResponse confirm(Long id) {
        PurchaseList list = getList(id);
        if (list.getStatus() != PurchaseListStatus.DRAFT) {
            throw new BusinessException("Only draft list can be confirmed");
        }
        list.setStatus(PurchaseListStatus.CONFIRMED);
        list.setConfirmedAt(LocalDateTime.now());
        list.setUpdatedAt(LocalDateTime.now());
        return detail(id);
    }

    @Transactional
    public PurchaseListResponse markPurchased(Long id) {
        PurchaseList list = getList(id);
        if (list.getStatus() != PurchaseListStatus.CONFIRMED) {
            throw new BusinessException("Only confirmed list can be marked purchased");
        }
        list.setStatus(PurchaseListStatus.PURCHASED);
        list.setPurchasedAt(LocalDateTime.now());
        list.setUpdatedAt(LocalDateTime.now());
        return detail(id);
    }

    @Transactional(readOnly = true)
    public List<PurchaseListResponse> list(PurchaseListStatus status) {
        List<PurchaseList> lists = status == null ? listRepository.findAll() : listRepository.findByStatus(status);
        return lists.stream().map(list -> toResponse(list, List.of(), List.of(), List.of())).toList();
    }

    @Transactional(readOnly = true)
    public Page<PurchaseListResponse> search(PurchaseListStatus status, String keyword, LocalDateTime startTime,
                                             LocalDateTime endTime, Pageable pageable) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : "";
        return listRepository.search(status, normalizedKeyword, startTime, endTime, pageable)
                .map(list -> toResponse(list, List.of(), List.of(), List.of()));
    }

    @Transactional(readOnly = true)
    public PurchaseListResponse detail(Long id) {
        PurchaseList list = getList(id);
        return toResponse(list, productLineRepository.findByPurchaseListId(id), itemRepository.findByPurchaseListId(id), List.of());
    }

    private void recalculateTotal(PurchaseList list) {
        BigDecimal total = itemRepository.findByPurchaseListId(list.getId()).stream()
                .map(PurchaseListItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        list.setTotalAmount(total);
        list.setUpdatedAt(LocalDateTime.now());
    }

    private PurchaseList getList(Long id) {
        return listRepository.findById(id).orElseThrow(() -> new BusinessException("Purchase list not found"));
    }

    private void requireDraft(PurchaseList list) {
        if (list.getStatus() != PurchaseListStatus.DRAFT) {
            throw new BusinessException("Only draft list can be edited");
        }
    }

    private String nextListNo() {
        return "PL" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private PurchaseListResponse toResponse(PurchaseList list, List<PurchaseListProduct> products,
                                            List<PurchaseListItem> items, List<String> warnings) {
        return new PurchaseListResponse(
                list.getId(),
                list.getListNo(),
                list.getStatus(),
                list.getTotalAmount(),
                list.getRemark(),
                list.getCreatedAt(),
                products.stream().map(line -> new PurchaseListProductResponse(line.getId(), line.getProduct().getId(),
                        line.getProductCodeSnapshot(), line.getProductNameSnapshot(), line.getUnitSnapshot(),
                        line.getQuantity(), line.getRemark())).toList(),
                items.stream().map(item -> new PurchaseListItemResponse(item.getId(), item.getSupportItem().getId(),
                        item.getSupportItemCodeSnapshot(), item.getSupportItemNameSnapshot(), item.getUnitSnapshot(),
                        item.getQuantity(), item.getDefaultUnitPriceSnapshot(), item.getActualUnitPrice(),
                        item.getAmount(), item.getRemark())).toList(),
                warnings
        );
    }
}
