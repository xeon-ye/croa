package com.qinfei.qferp.excelListener;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleExcel extends BaseRowModel {
    @ExcelProperty(value = "创建时间", index = 0)
    private String createTime;
    @ExcelProperty(value = "业务部门", index = 1)
    private String deptName;
    @ExcelProperty(value = "业务员", index = 2)
    private String userName;
    @ExcelProperty(value = "媒介", index = 3)
    private String mediaUserName;
    @ExcelProperty(value = "客户对接人", index = 4)
    private String dockingName;
    @ExcelProperty(value = "品牌", index = 5)
    private String brand;
    @ExcelProperty(value = "客户公司", index = 6)
    private String companyName;
    @ExcelProperty(value = "公司类型", index = 7)
    private String custCompanyType;
    @ExcelProperty(value = "媒体板块", index = 8)
    private String mediaTypeName;
    @ExcelProperty(value = "媒体名称", index = 9)
    private String mediaName;
    @ExcelProperty(value = "标题", index = 10)
    private String title;
    @ExcelProperty(value = "链接", index = 11)
    private String link;
    @ExcelProperty(value = "稿件行业类型", index = 12)
    private String typeName;
    @ExcelProperty(value = "发布日期", index = 13)
    private String issuedDate;
    @ExcelProperty(value = "数量", index = 14)
    private Integer num;
    @ExcelProperty(value = "应收（报价）", index = 15)
    private Double saleAmount;
    @ExcelProperty(value = "回款金额", index = 16)
    private Double incomeAmount;
    @ExcelProperty(value = "客户答应到款日期", index = 17)
    private String promiseDate;
    @ExcelProperty(value = "进账编号", index = 18)
    private String incomeCode;
    @ExcelProperty(value = "进账账户", index = 19)
    private String incomeAccount;
    @ExcelProperty(value = "进账人", index = 20)
    private String incomeMan;
    @ExcelProperty(value = "进账时间", index = 21)
    private String incomeDate;
    @ExcelProperty(value = "进账金额合计", index = 22)
    private Double incomeTotalAmount;
    @ExcelProperty(value = "分款日期", index = 23)
    private String assignDate;
    @ExcelProperty(value = "税金", index = 24)
    private Double taxes;
    @ExcelProperty(value = "退款金额", index = 25)
    private Double refundAmount;
    @ExcelProperty(value = "其他支出", index = 26)
    private Double otherPay;
    @ExcelProperty(value = "成本（请款）", index = 27)
    private Double outgoAmount;
    @ExcelProperty(value = "请款编号", index = 28)
    private String outgoCode;
    @ExcelProperty(value = "请款金额合计", index = 29)
    private Double outgoTotalAmount;
    @ExcelProperty(value = "利润", index = 30)
    private Double profit;
    @ExcelProperty(value = "提成", index = 31)
    private Double commission;
    @ExcelProperty(value = "提成年", index = 32)
    private Integer year;
    @ExcelProperty(value = "提成月", index = 33)
    private Integer month;
    @ExcelProperty(value = "供应商名称", index = 34)
    private String supplierName;
    @ExcelProperty(value = "供应商对接人", index = 35)
    private String supplierContactor;
    @ExcelProperty(value = "供应商电话", index = 36)
    private String supplierPhone;
//    @ExcelProperty(value = "项目编号", index = 36)
//    private String projectCode;
//    @ExcelProperty(value = "项目名称", index = 37)
//    private String projectName;
    @ExcelProperty(value = "电商商家", index = 37)
    private String electricityBusinesses;
    @ExcelProperty(value = "频道", index = 38)
    private String channel;
    @ExcelProperty(value = "内外部", index = 39)
    private String innerOuter;
    @ExcelProperty(value = "备注", index = 40)
    private String remarks;
    @ExcelProperty(value = "烂账详情",index = 41)
    private String messState;
}
