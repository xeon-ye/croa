package com.qinfei.qferp.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * excel入转换
 */
interface ExcelImportConvert {
    /**
     *
     * @param rowIndex 当前行下标
     * @param row 当前行
     * @param rowHead 表头
     */
    void convert(int rowIndex, Row row, Row rowHead);
}
