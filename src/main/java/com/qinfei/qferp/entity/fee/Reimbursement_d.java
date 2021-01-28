package com.qinfei.qferp.entity.fee;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;

@Table(name="fee_reimbursement_d")
public class Reimbursement_d {

    @Id
    private Integer id;
    //报销主表id
    private Integer remId;
    //费用类型
    private String costType;
    //用途
    private String purpose;
    //金额
    private Double money;
    //单据张数
    private Integer numberOfDocument;
    //当前行总计
    private Double currentTotalPrice;
    //状态
    private Integer state;
    //修改者的id
    private Integer updateUserId;

    public String getCostType() {
        return costType;
    }

    public void setCostType(String costType) {
        this.costType = costType;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    public Integer getNumberOfDocument() {
        return numberOfDocument;
    }

    public void setNumberOfDocument(Integer numberOfDocument) {
        this.numberOfDocument = numberOfDocument;
    }

    public Double getCurrentTotalPrice() {
        return currentTotalPrice;
    }

    public void setCurrentTotalPrice(Double currentTotalPrice) {
        this.currentTotalPrice = currentTotalPrice;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRemId() {
        return remId;
    }

    public void setRemId(Integer remId) {
        this.remId = remId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(Integer updateUserId) {
        this.updateUserId = updateUserId;
    }

    @Override
    public String toString() {
        return "Reimbursement_d{" +
                "id=" + id +
                ", remId=" + remId +
                ", costType='" + costType + '\'' +
                ", purpose='" + purpose + '\'' +
                ", money=" + money +
                ", numberOfDocument=" + numberOfDocument +
                ", currentTotalPrice=" + currentTotalPrice +
                ", state=" + state +
                ", updateUserId=" + updateUserId +
                '}';
    }
}
