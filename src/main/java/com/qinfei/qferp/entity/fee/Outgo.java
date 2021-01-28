package com.qinfei.qferp.entity.fee;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.core.entity.FileEntity;
import com.qinfei.qferp.entity.workup.WorkupRequestParam;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Table(name="fee_outgo")
@Data
public class Outgo extends FileEntity implements Serializable {
    @Id
    private Integer id ;
    private String code ;
    private String title ;
    private Integer processType ;
    private Integer supplierId ;
    private String supplierName;
    private String supplierContactor ;
    private Integer mediaTypeId;
    private Integer accountId ;
    private String accountName ;
    private String accountBankNo ;
    private String accountBankName ;
    private Integer applyId ;
    private String applyName ;
    private Integer deptId ;
    private String deptName ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime ;
    private Double applyAmount ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date expertPayTime ;
    private Integer outAccountId ;
    private String outAccountName ;
    private Double payAmount ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date payTime ;
    private Integer payUserId ;
    private String remark ;
    private String affix ;
    private Integer state ;
    private Integer creator ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime ;
    private Integer updateUserId ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime ;
    private Double fundAmount ;
    private String taskId ;
    private Integer itemId ;
    private String companyCode ;
    private String timeScale ;
    private String editJson; //请款唤醒修改的数据json字符串，唤醒成功后将覆盖对应值
    private String workupTaskId; //唤醒流程ID
    private Integer invoiceFlag;
    private Integer invoiceType;
    private Double actualCost;
    private Double outgoTax;
    private Double taxAmount;
    private Double outgoEraseAmount;
    private Double costEraseAmount;
    private Double invoiceTax;
    private String invoiceRise;
    private String invoiceCode;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date backfillTime;

    @Transient
    private Integer mediaGroupLeader;//审核组长id
    @Transient
    private String mediaGroupLeaderName;//审核组长nmae
    @Transient
    private Integer mediaGroupLeaderDept;//审核组长部门Id
    @Transient
    private WorkupRequestParam workupRequestParam;
    @Transient
    private Integer parentType; //父级板块
    @Transient
    private String creatorcompanyCode; //创建流程者公司code

    @Transient
    private String supplierCompanyCode;

    @Transient
    private Integer supplierNature; //供应商性质：0-企业供应商、1-个体供应商

    @Transient
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date supplierCreateTime; //供应商创建时间

    private Integer accountBinding; //请款完成后，是否绑定该供应商账户，默认为不绑定0,绑定为1

    @Transient
    private String supplierPhone;

    @Transient
    private Integer supplierCreator;
 }
