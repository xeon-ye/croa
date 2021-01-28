package com.qinfei.qferp.entity.fee;

import com.alibaba.fastjson.annotation.JSONField;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Table(name = "fee_invoice")
public class Invoice implements Serializable {
    @Id
    private Integer id ;
    private String code ;
    private String no ;
    private Integer custCompanyId ;
    private String custCompanyName ;
    private Integer custId ;
    private String custName ;
    private Integer type ;
    private Integer invoiceType ;
    private String title ;
    private String taxCode ;
    private String bankNo ;
    private String bankName ;
    private String address ;
    private String phone ;
    private Double amount ;
    private Double taxPoint ;
    private Double taxAmount ;
    private String invoiceDesc ;
    private String labourService;
    private Integer state ;
    private Integer creator ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date invoiceTime ;
    private String taxType ;
    private Integer applyId ;
    private String applyName ;
    private Integer deptId ;
    private String deptName ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime ;
    private String taskId ;
    private Integer itemId ;
    private Double invoiceAmount ;
    private String companyCode ;
    private String name ;
    private Double ratio ;//换算比

    public Double getRatio() {
        return ratio;
    }

    public void setRatio(Double ratio) {
        this.ratio = ratio;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }
    public Double getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(Double invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getCustCompanyId() {
        return custCompanyId;
    }

    public void setCustCompanyId(Integer custCompanyId) {
        this.custCompanyId = custCompanyId;
    }

    public String getCustCompanyName() {
        return custCompanyName;
    }

    public void setCustCompanyName(String custCompanyName) {
        this.custCompanyName = custCompanyName;
    }

    public Integer getCustId() {
        return custId;
    }

    public void setCustId(Integer custId) {
        this.custId = custId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(Integer invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getTaxPoint() {
        return taxPoint;
    }

    public void setTaxPoint(Double taxPoint) {
        this.taxPoint = taxPoint;
    }

    public Double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getInvoiceDesc() {
        return invoiceDesc;
    }

    public void setInvoiceDesc(String invoiceDesc) {
        this.invoiceDesc = invoiceDesc;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getCreator() {
        return creator;
    }

    public void setCreator(Integer creator) {
        this.creator = creator;
    }

    public Date getInvoiceTime() {
        return invoiceTime;
    }

    public void setInvoiceTime(Date invoiceTime) {
        this.invoiceTime = invoiceTime;
    }

    public String getTaxType() {
        return taxType;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    public Integer getApplyId() {
        return applyId;
    }

    public void setApplyId(Integer applyId) {
        this.applyId = applyId;
    }

    public String getApplyName() {
        return applyName;
    }

    public void setApplyName(String applyName) {
        this.applyName = applyName;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public String getLabourService() {
        return labourService;
    }

    public void setLabourService(String labourService) {
        this.labourService = labourService;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", no='" + no + '\'' +
                ", custCompanyId=" + custCompanyId +
                ", custCompanyName='" + custCompanyName + '\'' +
                ", custId=" + custId +
                ", custName='" + custName + '\'' +
                ", type=" + type +
                ", invoiceType=" + invoiceType +
                ", title='" + title + '\'' +
                ", taxCode='" + taxCode + '\'' +
                ", bankNo='" + bankNo + '\'' +
                ", bankName='" + bankName + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", amount=" + amount +
                ", taxPoint=" + taxPoint +
                ", taxAmount=" + taxAmount +
                ", invoiceDesc='" + invoiceDesc + '\'' +
                ", labourService='" + labourService + '\'' +
                ", state=" + state +
                ", creator=" + creator +
                ", invoiceTime=" + invoiceTime +
                ", taxType='" + taxType + '\'' +
                ", applyId=" + applyId +
                ", applyName='" + applyName + '\'' +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", applyTime=" + applyTime +
                ", taskId='" + taskId + '\'' +
                ", itemId=" + itemId +
                ", invoiceAmount=" + invoiceAmount +
                ", companyCode='" + companyCode + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
