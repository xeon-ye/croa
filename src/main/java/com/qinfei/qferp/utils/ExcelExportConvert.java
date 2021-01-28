package com.qinfei.qferp.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

public interface ExcelExportConvert {
    /**
     * 单元格转换
     * @param sheet
     * @param rowIndex 行下标
     * @param cellIndex 列下标
     * @param row 行
     * @param cell 列
     * @param field 字段名称
     * @param value 字段值
     */
    void convert(HSSFSheet sheet, int rowIndex, int cellIndex, HSSFRow row, HSSFCell cell, String field, Object value);
}
