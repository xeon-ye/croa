package com.qinfei.qferp.entity.workbench;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "t_index_items")
public class Items implements Serializable{
    @Id
    private Integer id;
    //事项名称
    private String itemName;
    //工作内容
    private String itemContent;
    //工作类型
    private String workType;
    //发起部门
    private Integer initiatorDept;
    //发起人
    private Integer initiatorWorker;
    //开始时间
    private Date startTime;
    //结束时间
    private Date endTime;
    //紧急程度
    private Integer urgencyLevel;
    //事项办理地址
    private String transactionAddress;
    //事项完成查看详情页面
    private String finishAddress;
    //事项状态
    private Integer transactionState;
    //接收时间
    private Date createTime;
    //接收部门
    private Integer acceptDept;
    //接收人
    private Integer acceptWorker;
    //事项类型
    private Integer itemType;
    //图标
    private String pic;
    //事项完成时间
    private Date finishTime;
    //事项完成人
    private Integer finishWorker;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemContent() {
        return itemContent;
    }

    public void setItemContent(String itemContent) {
        this.itemContent = itemContent;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public Integer getInitiatorDept() {
        return initiatorDept;
    }

    public void setInitiatorDept(Integer initiatorDept) {
        this.initiatorDept = initiatorDept;
    }

    public Integer getInitiatorWorker() {
        return initiatorWorker;
    }

    public void setInitiatorWorker(Integer initiatorWorker) {
        this.initiatorWorker = initiatorWorker;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(Integer urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getTransactionAddress() {
        return transactionAddress;
    }

    public void setTransactionAddress(String transactionAddress) {
        this.transactionAddress = transactionAddress;
    }

    public String getFinishAddress() {
        return finishAddress;
    }

    public void setFinishAddress(String finishAddress) {
        this.finishAddress = finishAddress;
    }

    public Integer getTransactionState() {
        return transactionState;
    }

    public void setTransactionState(Integer transactionState) {
        this.transactionState = transactionState;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getAcceptDept() {
        return acceptDept;
    }

    public void setAcceptDept(Integer acceptDept) {
        this.acceptDept = acceptDept;
    }

    public Integer getAcceptWorker() {
        return acceptWorker;
    }

    public void setAcceptWorker(Integer acceptWorker) {
        this.acceptWorker = acceptWorker;
    }

    public Integer getItemType() {
        return itemType;
    }

    public void setItemType(Integer itemType) {
        this.itemType = itemType;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Integer getFinishWorker() {
        return finishWorker;
    }

    public void setFinishWorker(Integer finishWorker) {
        this.finishWorker = finishWorker;
    }

    @Override
    public String toString() {
        return "Items{" +
                "id=" + id +
                ", itemName='" + itemName + '\'' +
                ", itemContent='" + itemContent + '\'' +
                ", workType='" + workType + '\'' +
                ", initiatorDept=" + initiatorDept +
                ", initiatorWorker=" + initiatorWorker +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", urgencyLevel=" + urgencyLevel +
                ", transactionAddress='" + transactionAddress + '\'' +
                ", finishAddress='" + finishAddress + '\'' +
                ", transactionState=" + transactionState +
                ", createTime=" + createTime +
                ", acceptDept=" + acceptDept +
                ", acceptWorker=" + acceptWorker +
                ", itemType=" + itemType +
                ", pic='" + pic + '\'' +
                ", finishTime=" + finishTime +
                ", finishWorker=" + finishWorker +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Items items = (Items) o;

        if (id != null ? !id.equals(items.id) : items.id != null) return false;
        if (itemName != null ? !itemName.equals(items.itemName) : items.itemName != null) return false;
        if (itemContent != null ? !itemContent.equals(items.itemContent) : items.itemContent != null) return false;
        if (workType != null ? !workType.equals(items.workType) : items.workType != null) return false;
        if (initiatorDept != null ? !initiatorDept.equals(items.initiatorDept) : items.initiatorDept != null)
            return false;
        if (initiatorWorker != null ? !initiatorWorker.equals(items.initiatorWorker) : items.initiatorWorker != null)
            return false;
        if (startTime != null ? !startTime.equals(items.startTime) : items.startTime != null) return false;
        if (endTime != null ? !endTime.equals(items.endTime) : items.endTime != null) return false;
        if (urgencyLevel != null ? !urgencyLevel.equals(items.urgencyLevel) : items.urgencyLevel != null) return false;
        if (transactionAddress != null ? !transactionAddress.equals(items.transactionAddress) : items.transactionAddress != null)
            return false;
        if (finishAddress != null ? !finishAddress.equals(items.finishAddress) : items.finishAddress != null)
            return false;
        if (transactionState != null ? !transactionState.equals(items.transactionState) : items.transactionState != null)
            return false;
        if (createTime != null ? !createTime.equals(items.createTime) : items.createTime != null) return false;
        if (acceptDept != null ? !acceptDept.equals(items.acceptDept) : items.acceptDept != null) return false;
        if (acceptWorker != null ? !acceptWorker.equals(items.acceptWorker) : items.acceptWorker != null) return false;
        if (itemType != null ? !itemType.equals(items.itemType) : items.itemType != null) return false;
        if (pic != null ? !pic.equals(items.pic) : items.pic != null) return false;
        if (finishTime != null ? !finishTime.equals(items.finishTime) : items.finishTime != null) return false;
        return finishWorker != null ? finishWorker.equals(items.finishWorker) : items.finishWorker == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (itemName != null ? itemName.hashCode() : 0);
        result = 31 * result + (itemContent != null ? itemContent.hashCode() : 0);
        result = 31 * result + (workType != null ? workType.hashCode() : 0);
        result = 31 * result + (initiatorDept != null ? initiatorDept.hashCode() : 0);
        result = 31 * result + (initiatorWorker != null ? initiatorWorker.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (urgencyLevel != null ? urgencyLevel.hashCode() : 0);
        result = 31 * result + (transactionAddress != null ? transactionAddress.hashCode() : 0);
        result = 31 * result + (finishAddress != null ? finishAddress.hashCode() : 0);
        result = 31 * result + (transactionState != null ? transactionState.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (acceptDept != null ? acceptDept.hashCode() : 0);
        result = 31 * result + (acceptWorker != null ? acceptWorker.hashCode() : 0);
        result = 31 * result + (itemType != null ? itemType.hashCode() : 0);
        result = 31 * result + (pic != null ? pic.hashCode() : 0);
        result = 31 * result + (finishTime != null ? finishTime.hashCode() : 0);
        result = 31 * result + (finishWorker != null ? finishWorker.hashCode() : 0);
        return result;
    }
}
