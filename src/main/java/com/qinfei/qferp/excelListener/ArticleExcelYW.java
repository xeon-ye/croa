package com.qinfei.qferp.excelListener;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleExcelYW extends BaseRowModel {
    @ExcelProperty(value = "标识位", index = 0)
    private String artId;
    @ExcelProperty(value = "客户答应到款时间", index = 1)
    private String promiseDate;
    @ExcelProperty(value = "客户公司", index = 2)
    private String companyName;
    @ExcelProperty(value = "客户对接人", index = 3)
    private String dockingName;
    @ExcelProperty(value = "媒体板块", index = 4)
    private String mediaTypeName;
    @ExcelProperty(value = "业务员", index = 5)
    private String userName;
    @ExcelProperty(value = "媒介", index = 6)
    private String mediaUserName;
    @ExcelProperty(value = "媒体名称", index = 7)
    private String mediaName;
    @ExcelProperty(value = "发布日期", index = 8)
    private String issuedDate;
    @ExcelProperty(value = "标题", index = 9)
    private String title;
    @ExcelProperty(value = "链接", index = 10)
    private String link;
    @ExcelProperty(value = "数量", index = 11)
    private Integer num;
    @ExcelProperty(value = "品牌", index = 12)
    private String brand;
    @ExcelProperty(value = "应收（报价）", index = 13)
    private Double saleAmount;
    @ExcelProperty(value = "回款金额", index = 14)
    private Double incomeAmount;
    @ExcelProperty(value = "成本（请款）", index = 15)
    private Double outgoAmount;
    @ExcelProperty(value = "税金", index = 16)
    private Double taxes;
    @ExcelProperty(value = "退款", index = 17)
    private Double refundAmount;
    @ExcelProperty(value = "其他支出", index = 18)
    private Double otherPay;
    @ExcelProperty(value = "利润", index = 19)
    private Double profit;
    @ExcelProperty(value = "提成", index = 20)
    private Double commission;
    @ExcelProperty(value = "提成年", index = 21)
    private Integer year;
    @ExcelProperty(value = "提成月", index = 22)
    private Integer month;
    @ExcelProperty(value = "公司类型", index = 23)
    private String custCompanyType;
    @ExcelProperty(value = "稿件行业类型", index = 24)
    private String typeName;
//    @ExcelProperty(value = "项目编号", index = 25)
//    private String projectCode;
//    @ExcelProperty(value = "项目名称", index = 26)
//    private String projectName;
    @ExcelProperty(value = "电商商家", index = 25)
    private String electricityBusinesses;
    @ExcelProperty(value = "频道", index = 26)
    private String channel;
    @ExcelProperty(value = "内外部", index = 27)
    private String innerOuter;
    @ExcelProperty(value = "备注", index = 28)
    private String remarks;
    @ExcelProperty(value = "烂账详情",index = 29)
    private String messState;

}
