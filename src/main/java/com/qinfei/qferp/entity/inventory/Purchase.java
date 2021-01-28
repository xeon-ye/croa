package com.qinfei.qferp.entity.inventory;
import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 采购订单表
 * @author tsf
 */
@Table(name="t_saves_product_buying")
@Data
public class Purchase implements Serializable {
    //采购id
    @Id
    private Integer id;
    //采购编号(防止与订单明细code重复)
    private String purchaseCode;
    //采购标题
    private String title;
    //创建人id
    private Integer userId;
    //创建人姓名
    private String userName;
    //状态:0已保存,1已完成，4部长审核，-9删除,-5失效，6入库暂存，7为已入库，14行政部长审核
    private Integer state;
    //创建人部门id
    private Integer deptId;
    //采购员id
    private Integer buyerId;
    //修改人id
    private Integer updateUserId;
    //采购日期
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    //采购日期
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date buyTime;
    //修改时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    //备注
    private String desc;
    //待办id
    private Integer itemId;
    //审核id
    private String taskId;
    //拒绝原因
    private String rejectReason;
    //附件名称
    private String affixName;
    //附件链接
    private String affixLink;
    //申请金额
    private Double money;
    //公司代码
    private String companyCode;
    //报销id
    private Integer reimbursementId;
    //产品明细信息
    @Transient
    private List<PurchaseDetails> purchaseDetails;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Purchase purchase = (Purchase) o;
        return Objects.equals(purchaseCode, purchase.purchaseCode) &&
                Objects.equals(title, purchase.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(purchaseCode, title);
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "id=" + id +
                ", purchaseCode='" + purchaseCode + '\'' +
                ", title='" + title + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", state=" + state +
                ", deptId=" + deptId +
                ", buyerId=" + buyerId +
                ", createTime=" + createTime +
                ", buyTime=" + buyTime +
                ", updateUserId=" + updateUserId +
                ", updateTime=" + updateTime +
                ", desc='" + desc + '\'' +
                ", itemId=" + itemId +
                ", taskId='" + taskId + '\'' +
                ", rejectReason='" + rejectReason + '\'' +
                ", affixName='" + affixName + '\'' +
                ", affixLink='" + affixLink + '\'' +
                ", money=" + money +
                ", companyCode='" + companyCode + '\'' +
                ", reimbursementId=" + reimbursementId +
                '}';
    }
}
