package com.sop.purchase.excel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sop.purchase.common.BusinessException;
import com.sop.purchase.product.ProductService;
import com.sop.purchase.product.dto.ProductDtos.CreateProductRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ExcelImportServiceTest {
    @Autowired
    private ProductService productService;
    @Autowired
    private ExcelImportService excelImportService;

    @Test
    void codeMatchSucceeds() {
        Long productId = productService.create(new CreateProductRequest("EX-1", "Excel Product 1", null, "box", null)).id();

        var result = excelImportService.parseProducts(workbook(row("EX-1", "", "2", "")));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).productId()).isEqualTo(productId);
        assertThat(result.get(0).quantity()).isEqualByComparingTo("2");
    }

    @Test
    void blankCodeFallsBackToUniqueProductName() {
        Long productId = productService.create(new CreateProductRequest("EX-2", "Unique Name", null, "box", null)).id();

        var result = excelImportService.parseProducts(workbook(row("", "Unique Name", "3", "")));

        assertThat(result.get(0).productId()).isEqualTo(productId);
    }

    @Test
    void duplicateNameFailsAndAsksForProductCode() {
        productService.create(new CreateProductRequest("EX-3A", "Duplicate Name", null, "box", null));
        productService.create(new CreateProductRequest("EX-3B", "Duplicate Name", null, "box", null));

        assertThatThrownBy(() -> excelImportService.parseProducts(workbook(row("", "Duplicate Name", "3", ""))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("duplicate product name");
    }

    @Test
    void unknownCodeFailsWithRowNumber() {
        assertThatThrownBy(() -> excelImportService.parseProducts(workbook(row("UNKNOWN", "", "3", ""))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Row 2")
                .hasMessageContaining("unknown product code");
    }

    @Test
    void negativeQuantityFailsWithRowNumber() {
        productService.create(new CreateProductRequest("EX-5", "Excel Product 5", null, "box", null));

        assertThatThrownBy(() -> excelImportService.parseProducts(workbook(row("EX-5", "", "-1", ""))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Row 2")
                .hasMessageContaining("quantity must be greater than zero");
    }

    private String[] row(String code, String name, String quantity, String remark) {
        return new String[]{code, name, quantity, remark};
    }

    private ByteArrayInputStream workbook(String[] rowValues) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("商品");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("商品编码");
            header.createCell(1).setCellValue("商品名称");
            header.createCell(2).setCellValue("数量");
            header.createCell(3).setCellValue("备注");
            Row row = sheet.createRow(1);
            for (int i = 0; i < rowValues.length; i++) {
                row.createCell(i).setCellValue(rowValues[i]);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
