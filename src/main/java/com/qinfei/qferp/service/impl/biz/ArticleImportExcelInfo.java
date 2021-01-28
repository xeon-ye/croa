package com.qinfei.qferp.service.impl.biz;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by yanhonghao on 2019/4/29 16:24.
 */
@Setter
@Getter
public class ArticleImportExcelInfo extends BaseRowModel {

    private Integer index;
    @ExcelProperty(index = 0, value = "*媒体板块")
    private String mediaTypeName;
    private Integer mediaTypeId;

    private String mediaUserName;
    private Integer mediaUserId;

    @ExcelProperty(index = 1, value = "供应商公司名称")
    private String supplierName;

    @ExcelProperty(index = 2, value = "供应商联系人")
    private String supplierContactor;

    @ExcelProperty(index = 3, value = "联系人手机号")
    private String phone;
    private Integer SupplierId;

    @ExcelProperty(index = 4, value = "*业务员")
    private String userName;
    private Integer userId;

    @ExcelProperty(index = 5, value = "*发布日期")
    private String issuedDateStr;
    private Date issuedDate;

    @ExcelProperty(index = 6, value = "*媒体名称/唯一标识")
    private String mediaName;
    private Integer mediaId;

    @ExcelProperty(index = 7, value = "*标题")
    private String title;

    @ExcelProperty(index = 8, value = "链接")
    private String link;

    @ExcelProperty(index = 9, value = "*请款金额")
    private String outgoAmountStr;
    private Double outgoAmount;

    @ExcelProperty(index = 10, value = "*价格类型")
    private String priceTypeStr;
    private String priceType ;
    private String priceColumn;

    @ExcelProperty(index = 11, value = "数量")
    private String numStr;
    private Integer num = 1;

    @ExcelProperty(index = 12, value = "其他费用")
    private String otherExpensesStr;
    private Double otherExpenses = 0D;

    @ExcelProperty(index = 13, value = "备注")
    private String remarks;

    @ExcelProperty(index = 14, value = "电商商家")
    private String electricityBusinesses;

    @ExcelProperty(index = 15, value = "内外部(内部/外部)")
    private String innerOuteStr;
    private String innerOuter;

    @ExcelProperty(index = 16, value = "频道")
    private String channel;

    private Integer orderId;
    private Double unitPrice;
    private Date createTime = new Date();
    private Integer state = 1;
    private Integer issueStates = 4;
    private Integer creator;
}
