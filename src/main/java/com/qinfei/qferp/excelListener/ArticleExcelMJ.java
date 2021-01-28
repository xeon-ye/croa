package com.qinfei.qferp.excelListener;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleExcelMJ extends BaseRowModel {
    @ExcelProperty(value = "媒体板块", index = 0)
    private String mediaTypeName;
    @ExcelProperty(value = "业务员", index = 1)
    private String userName;
    @ExcelProperty(value = "业务员所在公司", index = 2)
    private String companyCodeName;
    @ExcelProperty(value = "媒体名称", index = 3)
    private String mediaName;
    @ExcelProperty(value = "媒介员", index = 4)
    private String mediaUserName;
    @ExcelProperty(value = "供应商名称", index = 5)
    private String supplierName;
    @ExcelProperty(value = "供应商联系人", index = 6)
    private String supplierContactor;
    @ExcelProperty(value = "供应商电话", index = 7)
    private String supplierPhone;
    @ExcelProperty(value = "电商商家", index = 8)
    private String electricityBusinesses;
    @ExcelProperty(value = "频道", index = 9)
    private String channel;
    @ExcelProperty(value = "内外部", index = 10)
    private String innerOuter;
    @ExcelProperty(value = "标题", index = 11)
    private String title;
    @ExcelProperty(value = "链接", index = 12)
    private String link;
    @ExcelProperty(value = "发布日期", index = 13)
    private String issuedDate;
    @ExcelProperty(value = "数量", index = 14)
    private Integer num;
    @ExcelProperty(value = "价格类型", index = 15)
    private String priceType;
    @ExcelProperty(value = "其他费用", index = 16)
    private Double otherExpense;
    @ExcelProperty(value = "请款状态", index = 17)
    private String outgoStatus;
    @ExcelProperty(value = "应收（报价）", index = 18)
    private Double saleAmount;
    @ExcelProperty(value = "执行后价格（请款）", index = 19)
    private Double outgoAmount;
    @ExcelProperty(value = "单价", index = 20)
    private Double unitPrice;
    @ExcelProperty(value = "备注", index = 21)
    private String remarks;
    @ExcelProperty(value = "稿件编号", index = 22)
    private Integer artId;
}
