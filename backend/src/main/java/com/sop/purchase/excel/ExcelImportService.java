package com.sop.purchase.excel;

import com.sop.purchase.common.BusinessException;
import com.sop.purchase.product.Product;
import com.sop.purchase.product.ProductRepository;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.GenerateProductInput;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ExcelImportService {
    private final ProductRepository productRepository;
    private final DataFormatter formatter = new DataFormatter();

    public ExcelImportService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<GenerateProductInput> parseProducts(InputStream inputStream) {
        try (var workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<GenerateProductInput> result = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isBlankRow(row)) {
                    continue;
                }
                int rowNo = i + 1;
                String code = text(row.getCell(0));
                String name = text(row.getCell(1));
                String quantityText = text(row.getCell(2));
                String remark = text(row.getCell(3));
                Product product = resolveProduct(rowNo, code, name, errors);
                BigDecimal quantity = resolveQuantity(rowNo, quantityText, errors);
                if (product != null && quantity != null) {
                    result.add(new GenerateProductInput(product.getId(), product.getCode(), product.getName(),
                            product.getUnit(), quantity, remark));
                }
            }
            if (!errors.isEmpty()) {
                throw new BusinessException(String.join("; ", errors));
            }
            return result;
        } catch (IOException ex) {
            throw new BusinessException("Failed to read Excel file");
        }
    }

    private Product resolveProduct(int rowNo, String code, String name, List<String> errors) {
        if (StringUtils.hasText(code)) {
            return productRepository.findByCode(code).map(product -> {
                if (!Boolean.TRUE.equals(product.getEnabled())) {
                    errors.add("Row " + rowNo + ": product is disabled");
                    return null;
                }
                return product;
            }).orElseGet(() -> {
                errors.add("Row " + rowNo + ": unknown product code " + code);
                return null;
            });
        }
        if (!StringUtils.hasText(name)) {
            errors.add("Row " + rowNo + ": product code or name is required");
            return null;
        }
        List<Product> matches = productRepository.findByName(name);
        if (matches.isEmpty()) {
            errors.add("Row " + rowNo + ": unknown product name " + name);
            return null;
        }
        if (matches.size() > 1) {
            errors.add("Row " + rowNo + ": duplicate product name, please use product code");
            return null;
        }
        Product product = matches.get(0);
        if (!Boolean.TRUE.equals(product.getEnabled())) {
            errors.add("Row " + rowNo + ": product is disabled");
            return null;
        }
        return product;
    }

    private BigDecimal resolveQuantity(int rowNo, String value, List<String> errors) {
        try {
            BigDecimal quantity = new BigDecimal(value);
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                errors.add("Row " + rowNo + ": quantity must be greater than zero");
                return null;
            }
            return quantity;
        } catch (RuntimeException ex) {
            errors.add("Row " + rowNo + ": quantity must be a number");
            return null;
        }
    }

    private boolean isBlankRow(Row row) {
        return !StringUtils.hasText(text(row.getCell(0)))
                && !StringUtils.hasText(text(row.getCell(1)))
                && !StringUtils.hasText(text(row.getCell(2)));
    }

    private String text(Cell cell) {
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }
}
