package com.example.labmanage.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.servlet.http.HttpServletResponse;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExcelExportUtil {

    public static void exportToExcel(String title,
                                     List<String> headers,
                                     List<List<String>> data,
                                     HttpServletResponse response) throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(title.length() > 31 ? title.substring(0, 31) : title);

        // 表头样式
        CellStyle headerStyle = wb.createCellStyle();
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // 数据样式
        CellStyle dataStyle = wb.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // 表头行
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        // 数据行
        for (int r = 0; r < data.size(); r++) {
            Row row = sheet.createRow(r + 1);
            List<String> rowData = data.get(r);
            for (int c = 0; c < rowData.size(); c++) {
                Cell cell = row.createCell(c);
                cell.setCellValue(rowData.get(c) != null ? rowData.get(c) : "");
                cell.setCellStyle(dataStyle);
            }
        }

        // 自动列宽
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // 响应头
        String fileName = URLEncoder.encode(title, StandardCharsets.UTF_8).replace("+", "%20") + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try (OutputStream out = response.getOutputStream()) {
            wb.write(out);
        }
        wb.close();
    }
}
