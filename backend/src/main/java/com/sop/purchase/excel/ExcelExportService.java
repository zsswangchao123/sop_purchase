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
    private final PurchaseListService purchaseListService;

    public ExcelExportService(PurchaseListService purchaseListService) {
        this.purchaseListService = purchaseListService;
    }

    public byte[] exportPurchaseList(Long id) {
        PurchaseListResponse detail = purchaseListService.detail(id);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var itemSheet = workbook.createSheet("Support Items");
            Row itemHeader = itemSheet.createRow(0);
            writeCells(itemHeader, "Code", "Name", "Unit", "Quantity", "Actual Price", "Amount");
            for (int i = 0; i < detail.items().size(); i++) {
                var item = detail.items().get(i);
                writeCells(itemSheet.createRow(i + 1), item.supportItemCode(), item.supportItemName(), item.unit(),
                        item.quantity().toPlainString(), item.actualUnitPrice().toPlainString(), item.amount().toPlainString());
            }

            var productSheet = workbook.createSheet("Products");
            Row productHeader = productSheet.createRow(0);
            writeCells(productHeader, "Code", "Name", "Unit", "Quantity", "Remark");
            for (int i = 0; i < detail.products().size(); i++) {
                var product = detail.products().get(i);
                writeCells(productSheet.createRow(i + 1), product.productCode(), product.productName(),
                        product.unit(), product.quantity().toPlainString(), product.remark());
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to export purchase list", ex);
        }
    }

    public byte[] template() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Products");
            writeCells(sheet.createRow(0), "Product Code", "Product Name", "Quantity", "Remark");
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
