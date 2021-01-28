package com.qinfei.qferp.entity.biz;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.media.MediaType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Table(name = "t_biz_article_return_down")
@Data
public class ArticleReturnDown implements Serializable {
    @Id
    private Integer id;

    private Integer orderId;

    private Integer mediaId;

    private String mediaName;

    private Integer supplierId;

    private String supplierName;

    private Integer mediaUserId;

    private String mediaUserName;

    private String brand;

    private Date issuedDate;

    private String title;

    private String link;

    private Double saleAmount;

    private Double incomeAmount;

    private Double taxes;

    private String priceColumn;

    private String priceType;

    private Double payAmount;

    private Double outgoAmount;

    private Date promiseDate;

    private Date incomeDate;

    private Double otherPay;

    private Double refundAmount;

    private String remarks;

    private Double commission;

    private Date commissionDate;

    private Integer outgoStates;

    private Integer commissionStates;

    private Integer invoiceStates;

    private Integer incomeStates;

    private Integer issueStates;

    private Integer num;

    private String filePath;

    private Integer refundStates ;

    private String typeCode;

    private String typeName;

    private Date returnDownDate;

    private Integer returnDownUser;

    @Transient
    private MediaType mediaType;
}