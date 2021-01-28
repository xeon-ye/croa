package com.qinfei.qferp.utils;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class ExcelUtil {
    public static void exportExcel(String title, String[] headers, String[] fields, List mapList, OutputStream out, String pattern, ExcelExportConvert excelConvert) {
        //声明一个工作簿
        int size = mapList.size();
        final int sheetNum = (int) Math.ceil(size / 60000f);
        HSSFWorkbook workbook = new HSSFWorkbook();
        for (int n = 0; n < sheetNum; n++) {
            //生成一个表格
            HSSFSheet sheet = workbook.createSheet(title + n);
            //设置表格默认列宽度为15个字符
            sheet.setDefaultColumnWidth(20);
            //生成一个样式，用来设置标题样式
            HSSFCellStyle style = workbook.createCellStyle();
            //设置这些样式
            style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.SKY_BLUE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setAlignment(HorizontalAlignment.CENTER);
            //生成一个字体
            HSSFFont font = workbook.createFont();
            font.setColor(HSSFColor.HSSFColorPredefined.VIOLET.getIndex());
//        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font.setBold(true);
            //把字体应用到当前的样式
            style.setFont(font);
            // 生成并设置另一个样式,用于设置内容样式
            HSSFCellStyle style2 = workbook.createCellStyle();
//        style2.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
            style2.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_YELLOW.getIndex());
            style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style2.setBorderBottom(BorderStyle.THIN);
            style2.setBorderLeft(BorderStyle.THIN);
            style2.setBorderRight(BorderStyle.THIN);
            style2.setBorderTop(BorderStyle.THIN);
            style2.setAlignment(HorizontalAlignment.CENTER);
            style2.setVerticalAlignment(VerticalAlignment.CENTER);
            // 生成另一个字体
            HSSFFont font2 = workbook.createFont();
            font2.setBold(true);
            // 把字体应用到当前的样式
            style2.setFont(font2);
            //产生表格标题行
            HSSFRow row = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                HSSFCell cell = row.createCell(i);
                cell.setCellStyle(style);
                HSSFRichTextString text = new HSSFRichTextString(headers[i]);
                cell.setCellValue(text);
            }
            for (int i = n * 60000; i < size && i < (n + 1) * 60000; i++) {
                int index=i - 60000 * n;
                Map<String, Object> map = (Map<String, Object>) mapList.get(i);
                try {
                    row = sheet.createRow(index + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                for (int j = 0; j < fields.length; j++) {
                    HSSFCell cell = row.createCell(j);
                    excelConvert.convert(sheet, index, j, row, cell, fields[j], map.get(fields[j]));
                }
            }
        }
        try {
            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportExcel1(String title, String[] headers, String[] fields, List<?> mapList, OutputStream out, String pattern, ExcelExportConvert excelConvert) {
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格
        HSSFSheet sheet = workbook.createSheet(title);
        //设置表格默认列宽度为15个字符
        sheet.setDefaultColumnWidth(20);
        //生成一个样式，用来设置标题样式
        HSSFCellStyle style = workbook.createCellStyle();
        //设置这些样式
//        style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
        style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.SKY_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        //生成一个字体
        HSSFFont font = workbook.createFont();
        font.setColor(HSSFColor.HSSFColorPredefined.VIOLET.getIndex());
        font.setBold(true);
        //把字体应用到当前的样式
        style.setFont(font);
        // 生成并设置另一个样式,用于设置内容样式
        HSSFCellStyle style2 = workbook.createCellStyle();
        style2.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_YELLOW.getIndex());
        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style2.setBorderBottom(BorderStyle.THIN);
        style2.setBorderLeft(BorderStyle.THIN);
        style2.setBorderRight(BorderStyle.THIN);
        style2.setBorderTop(BorderStyle.THIN);
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setVerticalAlignment(VerticalAlignment.CENTER);
        // 生成另一个字体
        HSSFFont font2 = workbook.createFont();
//        font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        font2.setBold(false);
        // 把字体应用到当前的样式
        style2.setFont(font2);
        //产生表格标题行
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellStyle(style);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            cell.setCellValue(text);
        }
        Class<?> cls = mapList.get(0).getClass();
        for (int i = 0; i < mapList.size(); i++) {
            Object object = mapList.get(i);
            row = sheet.createRow(i + 1);
            for (int j = 0; j < fields.length; j++) {
                HSSFCell cell = row.createCell(j);
                String field = fields[j];

                Object value = null;
                try {
                    Field field1 = cls.getDeclaredField(field);
                    field1.setAccessible(true);
                    value = field1.get(object);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                excelConvert.convert(sheet, i, j, row, cell, field, value);
            }
        }
        try {
            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void importExcel(InputStream fis, ExcelImportConvert convert) {
        Workbook wookbook = null;
        try {
            //2003版本的excel，用.xls结尾
            wookbook = new HSSFWorkbook(fis);//得到工作簿
        } catch (Exception ex) {
            //ex.printStackTrace();
            try {
                //2007版本的excel，用.xlsx结尾
                wookbook = new XSSFWorkbook(fis);//得到工作簿
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //得到一个工作表
        Sheet sheet = wookbook.getSheetAt(0);
        //获得表头
        Row rowHead = sheet.getRow(0);
        //获得数据的总行数
        int totalRowNum = sheet.getLastRowNum();

        //要获得属性
        String name = "";
        int latitude = 0;

        //获得所有数据
        for (int i = 1; i <= totalRowNum; i++) {
            //获得第i行对象
            Row row = sheet.getRow(i);
            convert.convert(i, row, rowHead);
        }
    }
}
