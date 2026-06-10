package com.example.labmanage.util;

import org.apache.poi.xwpf.usermodel.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

public class WordExportUtil {

    /**
     * 通用 Word 导出方法
     * 
     * @param title    文档标题
     * @param headers  表头
     * @param data     表格数据（每一行是一个 List<String>）
     * @param response HttpServletResponse
     */
    public static void exportTableToWord(String title,
            List<String> headers,
            List<List<String>> data,
            HttpServletResponse response) throws Exception {

        // 1. 创建 Word 文档
        XWPFDocument document = new XWPFDocument();

        // 2. 添加标题
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(title);
        titleRun.setBold(true);
        titleRun.setFontSize(16);

        // 3. 创建表格
        int rowCount = data.size() + 1; // +1 是表头
        int colCount = headers.size();
        XWPFTable table = document.createTable(rowCount, colCount);

        // 设置表头
        for (int i = 0; i < colCount; i++) {
            table.getRow(0).getCell(i).setText(headers.get(i));
        }

        // 设置数据
        for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
            List<String> rowData = data.get(rowIndex);
            for (int colIndex = 0; colIndex < colCount; colIndex++) {
                String value = rowData.get(colIndex) == null ? "" : rowData.get(colIndex);
                table.getRow(rowIndex + 1).getCell(colIndex).setText(value);
            }
        }

        // 4. 设置响应头，让浏览器下载 Word
        String fileName = URLEncoder.encode(title, "UTF-8") + ".docx";
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // 5. 输出文件
        OutputStream out = response.getOutputStream();
        document.write(out);
        out.flush();
        out.close();
        document.close();
    }

    /**
     * 按照模板格式导出实验教学任务表
     * 包含表头（院、系（中心）20 —20 学年第 学期）和底部签名栏
     */
    public static void exportExpTaskToWord(String title,
            List<String> headers,
            List<List<String>> data,
            String term,
            HttpServletResponse response) throws Exception {

        // 1. 创建 Word 文档
        XWPFDocument document = new XWPFDocument();

        // 2. 添加表头：院、系（中心）20 —20 学年第 学期
        XWPFParagraph headerPara = document.createParagraph();
        XWPFRun headerRun = headerPara.createRun();
        headerRun.setText("________________院、系（中心）20  —20  学年第    学期");
        headerRun.setFontSize(12);

        // 3. 添加标题（居中）
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(title);
        titleRun.setBold(true);
        titleRun.setFontSize(14);

        // 空一行
        document.createParagraph();

        // 4. 创建表格
        int rowCount = data.size() + 1; // +1 是表头
        int colCount = headers.size();
        XWPFTable table = document.createTable(rowCount, colCount);

        // 设置表格宽度
        table.setWidth("100%");

        // 设置表头
        for (int i = 0; i < colCount; i++) {
            XWPFTableCell headerCell = table.getRow(0).getCell(i);
            headerCell.setText(headers.get(i));
            // 设置表头加粗
            for (XWPFParagraph para : headerCell.getParagraphs()) {
                for (XWPFRun run : para.getRuns()) {
                    run.setBold(true);
                }
            }
        }

        // 设置数据
        for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
            List<String> rowData = data.get(rowIndex);
            for (int colIndex = 0; colIndex < colCount; colIndex++) {
                String value = rowData.get(colIndex) == null ? "" : rowData.get(colIndex);
                table.getRow(rowIndex + 1).getCell(colIndex).setText(value);
            }
        }

        // 5. 添加底部签名栏
        document.createParagraph(); // 空一行
        document.createParagraph(); // 空一行

        XWPFParagraph signPara1 = document.createParagraph();
        XWPFRun signRun1 = signPara1.createRun();
        signRun1.setText("填表人签名：________________                    年    月    日");
        signRun1.setFontSize(12);

        XWPFParagraph signPara2 = document.createParagraph();
        XWPFRun signRun2 = signPara2.createRun();
        signRun2.setText("院系（中心）盖章：________________");
        signRun2.setFontSize(12);

        // 6. 设置响应头
        String fileName = URLEncoder.encode(title, "UTF-8") + ".docx";
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // 7. 输出文件
        OutputStream out = response.getOutputStream();
        document.write(out);
        out.flush();
        out.close();
        document.close();
    }
}