package com.qinfei.core.excel;

import com.alibaba.excel.event.WriteHandler;
import com.qinfei.core.utils.DateUtils;
import org.apache.poi.ss.usermodel.*;

/**
 * @author: 66
 * @Date: 2020/11/14 10:38
 * @Description:
 */
public class StyleExcelHandler implements WriteHandler {

    private static CellStyle cellStyle;

    // 日期转化格式
    private final String format = "yyyy-MM-dd";

    @Override
    public void sheet(int i, Sheet sheet) {
    }

    @Override
    public void row(int i, Row row) {

    }

    @Override
    public void cell(int i, Cell cell) {
        Workbook workbook = cell.getSheet().getWorkbook();
        if (cell.getRowIndex() > 0) {
            if (i == 13 || i == 0) {
                if(cellStyle == null){
                    cellStyle = workbook.createCellStyle();
                    CreationHelper createHelper = workbook.getCreationHelper();
                    cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(format));
                }
                // 需要先设置格式再赋值，不然会出现格式没有应用的问题
                cell.setCellStyle(cellStyle);
                cell.setCellValue(DateUtils.parse(cell.getStringCellValue(), format));
            }
        }
    }
}
