package com.qinfei.qferp.excelListener;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleExcelCommissionUnRegister extends BaseRowModel {
    @ExcelProperty(value = "媒体板块", index = 0)
    private String mediaTypeName;
    @ExcelProperty(value = "业务员", index = 1)
    private String userName;
    @ExcelProperty(value = "媒介", index = 2)
    private String mediaUserName;
    @ExcelProperty(value = "客户公司", index = 3)
    private String companyName;
    @ExcelProperty(value = "发布日期", index = 4)
    private String issuedDate;
    @ExcelProperty(value = "媒体名称", index = 5)
    private String mediaName;
    @ExcelProperty(value = "标题", index = 6)
    private String title;
    @ExcelProperty(value = "内外部", index = 7)
    private String innerOuter;
    @ExcelProperty(value = "频道", index = 8)
    private String channel;
    @ExcelProperty(value = "电商商家", index = 9)
    private String electricityBusinesses;
    @ExcelProperty(value = "应收（报价）", index = 10)
    private Double saleAmount;
    @ExcelProperty(value = "回款金额", index = 11)
    private Double incomeAmount;
    @ExcelProperty(value = "回款状态", index = 12)
    private String incomeStates;
    @ExcelProperty(value = "成本（请款）", index = 13)
    private Double outgoAmount;
    @ExcelProperty(value = "请款状态", index = 14)
    private String outgoStates;
    @ExcelProperty(value = "税金", index = 15)
    private Double taxes;
    @ExcelProperty(value = "开票状态", index = 16)
    private String invoiceStates;
    @ExcelProperty(value = "退款金额", index = 17)
    private Double refundAmount;
    @ExcelProperty(value = "退款状态", index = 18)
    private String refundStates;
    @ExcelProperty(value = "其他支出", index = 19)
    private Double otherPay;
    @ExcelProperty(value = "其他支出状态", index = 20)
    private String otherPayStates;
    @ExcelProperty(value = "利润", index = 21)
    private Double profit;
    @ExcelProperty(value = "提成", index = 22)
    private Double commission;
    @ExcelProperty(value = "进账编号", index = 23)
    private String incomeCode;
    @ExcelProperty(value = "回款明细", index = 24)
    private String amountDetail;
    @ExcelProperty(value = "账户名称", index = 25)
    private String incomeAccount;
    @ExcelProperty(value = "进账人", index = 26)
    private String incomeMan;
    @ExcelProperty(value = "进账时间", index = 27)
    private String incomeDate;
    @ExcelProperty(value = "进账总金额", index = 28)
    private Double incomeTotalAmount;
    @ExcelProperty(value = "请款编号", index = 29)
    private String outgoCode;
    @ExcelProperty(value = "请款金额合计", index = 30)
    private Double outgoTotalAmount;
}
