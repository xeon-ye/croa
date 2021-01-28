package com.qinfei.qferp.entity.biz;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.media1.MediaPlate;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

@Table(name = "t_biz_article_history")
@Alias("articleHistory")
@Data
public class ArticleHistory implements Serializable {
   @Id
    private Integer id;

    private Integer artId;

    private Integer mediaId;

    private String mediaName;

    private Integer supplierId;

    private String supplierName;

    private String supplierContactor;

    private Integer mediaUserId;

    private String mediaUserName;

    private String brand;
    @JSONField(format = "yyyy-MM-dd")
    private Date issuedDate;

    private String title;

    private String link;

    private Double saleAmount = 0.0;

    private Double incomeAmount = 0.0;

    private Double taxes = 0.0;

    private Integer taxType;

    private String priceColumn;

    private String priceType;

    private Double payAmount = 0.0;

    private Double outgoAmount = 0.0;

    @JSONField(format = "yyyy-MM-dd")
    private Date promiseDate;

    @JSONField(format = "yyyy-MM-dd")
    private Date incomeDate;

    private Double otherPay = 0.0 ;

    private Double refundAmount = 0.0;

    private String remarks;

    private Double commission = 0.0;

    private Date commissionDate;

    private Integer outgoStates = 0;

    private Integer commissionStates = 0;

    private Integer invoiceStates = 0;

    private Integer incomeStates = 0;

    private Integer issueStates = 0;
    //    @Transient
//    private Media media;
    @Transient
    private MediaPlate mediaType;

    private Integer num;

    private String filePath;

    private Integer refundStates;

    private Integer otherPayStates;

    private Integer year;

    private Integer month;

    private Double profit= 0.0 ;

    private Integer state ;

    private Integer creator ;
    @JSONField(format = "yyyy-MM-dd")
    private Date createTime ;

    private Integer updateUserId ;
    @JSONField(format = "yyyy-MM-dd")
    private Date updateTime ;

    private Integer mediaTypeId ;

    private String mediaTypeName ;

    private Double unitPrice = 0.0;

    private Integer incomeId ;

    private String incomeCode ;

    private String incomeAccount ;

    private String incomeMan ;

    private Double incomeTotalAmount ;

    private Integer outgoId ;

    private String outgoCode ;

    private Double outgoTotalAmount ;
    /**
     * 内外部
     */
    private String innerOuter;
    /**
     * 频道
     */
    private String channel;
    /**
     * 电商商家
     */
    private String electricityBusinesses;
    /**
     * 其他费用，媒介专用，不计入成本
     */
    private Double otherExpenses;
    /**
     * 稿件类型code
     */
    private String typeCode;
    /**
     * 稿件类型c
     */
    private String typeName;

    private String editDesc;

    private Double alterSale = 0.0;

    private Double alterIncome = 0.0;

    private Double alterOutgo = 0.0;

    private Double alterTax = 0.0;

    private Double alterRefund = 0.0;

    private Double alterOtherPay = 0.0;

    private Double alterProfit = 0.0;

    private Double alterComm = 0.0;

    private Integer alterLabel = 0;

    @JSONField(format = "yyyy-MM-dd")
    private Date assignDate;

    private String companyCode ;

    private Integer userId;

    private Integer deptId;

    private Integer supplierPersonId;//媒体供应商责任人

    private Integer mediaPersonId;//媒体责任人
}