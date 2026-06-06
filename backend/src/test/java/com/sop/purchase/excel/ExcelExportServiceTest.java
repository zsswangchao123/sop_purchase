package com.sop.purchase.excel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sop.purchase.purchaselist.PurchaseListService;
import com.sop.purchase.purchaselist.PurchaseListStatus;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.PurchaseListItemResponse;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.PurchaseListProductResponse;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.PurchaseListResponse;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

class ExcelExportServiceTest {
    private final PurchaseListService purchaseListService = mock(PurchaseListService.class);
    private final ExcelExportService excelExportService = new ExcelExportService(purchaseListService);

    @Test
    void templateUsesChineseProductHeaders() throws Exception {
        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(excelExportService.template()))) {
            assertThat(workbook.getSheetName(0)).isEqualTo("商品");
            assertThat(values(workbook.getSheetAt(0).getRow(0)))
                    .containsExactly("商品编码", "商品名称", "数量", "备注");
        }
    }

    @Test
    void purchaseListExportUsesChineseHeadersAndMatchesTemplateProductHeaders() throws Exception {
        when(purchaseListService.detail(1L)).thenReturn(detail());

        try (var exportWorkbook = WorkbookFactory.create(
                new ByteArrayInputStream(excelExportService.exportPurchaseList(1L)));
             var templateWorkbook = WorkbookFactory.create(new ByteArrayInputStream(excelExportService.template()))) {
            assertThat(exportWorkbook.getSheetName(0)).isEqualTo("配套商品");
            assertThat(exportWorkbook.getSheetName(1)).isEqualTo("商品");
            assertThat(values(exportWorkbook.getSheetAt(0).getRow(0)))
                    .containsExactly("配套商品编码", "配套商品名称", "单位", "数量", "实际单价", "金额");
            assertThat(values(exportWorkbook.getSheetAt(1).getRow(0)))
                    .containsExactlyElementsOf(values(templateWorkbook.getSheetAt(0).getRow(0)));
            assertThat(values(exportWorkbook.getSheetAt(1).getRow(1)))
                    .containsExactly("P-001", "西红柿", "2", "测试");
        }
    }

    private PurchaseListResponse detail() {
        var product = new PurchaseListProductResponse(1L, 1L, "P-001", "西红柿", "斤",
                new BigDecimal("2"), "测试");
        var item = new PurchaseListItemResponse(1L, 1L, "S-001", "包装袋", "个",
                new BigDecimal("2"), BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("2"), "");
        return new PurchaseListResponse(1L, "PL-001", PurchaseListStatus.DRAFT, new BigDecimal("2"),
                "", LocalDateTime.now(), List.of(product), List.of(item), List.of());
    }

    private List<String> values(Row row) {
        return java.util.stream.IntStream.range(0, row.getLastCellNum())
                .mapToObj(index -> row.getCell(index).getStringCellValue())
                .toList();
    }
}
