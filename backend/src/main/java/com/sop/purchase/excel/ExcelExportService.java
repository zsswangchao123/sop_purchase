package com.sop.purchase.excel;

import com.sop.purchase.purchaselist.PurchaseListService;
import com.sop.purchase.purchaselist.dto.PurchaseListDtos.PurchaseListResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExcelExportService {
    private static final String[] PRODUCT_HEADERS = {"商品编码", "商品名称", "数量", "备注"};
    private final PurchaseListService purchaseListService;

    public ExcelExportService(PurchaseListService purchaseListService) {
        this.purchaseListService = purchaseListService;
    }

    public byte[] exportPurchaseList(Long id) {
        PurchaseListResponse detail = purchaseListService.detail(id);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var itemSheet = workbook.createSheet("配套商品");
            Row itemHeader = itemSheet.createRow(0);
            writeCells(itemHeader, "配套商品编码", "配套商品名称", "单位", "数量", "实际单价", "金额");
            for (int i = 0; i < detail.items().size(); i++) {
                var item = detail.items().get(i);
                writeCells(itemSheet.createRow(i + 1), item.supportItemCode(), item.supportItemName(), item.unit(),
                        item.quantity().toPlainString(), item.actualUnitPrice().toPlainString(), item.amount().toPlainString());
            }

            var productSheet = workbook.createSheet("商品");
            Row productHeader = productSheet.createRow(0);
            writeCells(productHeader, PRODUCT_HEADERS);
            for (int i = 0; i < detail.products().size(); i++) {
                var product = detail.products().get(i);
                writeCells(productSheet.createRow(i + 1), product.productCode(), product.productName(),
                        product.quantity().toPlainString(), product.remark());
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to export purchase list", ex);
        }
    }

    public byte[] template() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("商品");
            writeCells(sheet.createRow(0), PRODUCT_HEADERS);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create template", ex);
        }
    }

    private void writeCells(Row row, String... values) {
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i] == null ? "" : values[i]);
        }
    }
}
