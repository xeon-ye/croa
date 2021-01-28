package com.qinfei.qferp.entity.workbench;

import com.qinfei.core.annotation.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 消息
 */
@Table(name = "t_index_message")
public class Message implements Serializable{
    private Integer id;
    //图标
    private String pic;
    //内容
    private String content;
    //发起部门
    private Integer initiatorDept;
    //发起人
    private Integer initiatorWorker;
    //创建时间
    private Date createTime;
    //状态1未读2已读
    private Integer state;
    //接收部门
    private Integer acceptDept;
    //接收人
    private Integer acceptWorker;
    //父级消息类型（1流程，2待办，3通知，4其它）
    private Integer parentType;
    //子级消息类型
    // （1开票，2借款，3请款，4退款，5费用报销，6删稿流程，
    //  7请假，8加班，9外出，10出差，16绩效，
    //  11媒体审核，12媒体查询，13论坛，14公告通知，15日程，
    //  16绩效，17员工转正，18员工调岗，19员工交接，
    //  20员工离职21员工录用，22流程唤醒,23会议,24业务下单,26物品报修,27物品报废,28物品归还 ,29烂账申请）
    private Integer type;
    //消息跳转链接
    private String url;
    //链接名称
    private String urlName;
    //待办id
    private Integer itemId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
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

    public Integer getParentType() {
        return parentType;
    }

    public void setParentType(Integer parentType) {
        this.parentType = parentType;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlName() {
        return urlName;
    }

    public void setUrlName(String urlName) {
        this.urlName = urlName;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (id != null ? !id.equals(message.id) : message.id != null) return false;
        if (pic != null ? !pic.equals(message.pic) : message.pic != null) return false;
        if (content != null ? !content.equals(message.content) : message.content != null) return false;
        if (initiatorDept != null ? !initiatorDept.equals(message.initiatorDept) : message.initiatorDept != null)
            return false;
        if (initiatorWorker != null ? !initiatorWorker.equals(message.initiatorWorker) : message.initiatorWorker != null)
            return false;
        if (createTime != null ? !createTime.equals(message.createTime) : message.createTime != null) return false;
        if (state != null ? !state.equals(message.state) : message.state != null) return false;
        if (acceptDept != null ? !acceptDept.equals(message.acceptDept) : message.acceptDept != null) return false;
        return acceptWorker != null ? acceptWorker.equals(message.acceptWorker) : message.acceptWorker == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (pic != null ? pic.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (initiatorDept != null ? initiatorDept.hashCode() : 0);
        result = 31 * result + (initiatorWorker != null ? initiatorWorker.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (acceptDept != null ? acceptDept.hashCode() : 0);
        result = 31 * result + (acceptWorker != null ? acceptWorker.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", pic='" + pic + '\'' +
                ", content='" + content + '\'' +
                ", initiatorDept=" + initiatorDept +
                ", initiatorWorker=" + initiatorWorker +
                ", createTime=" + createTime +
                ", state=" + state +
                ", acceptDept=" + acceptDept +
                ", acceptWorker=" + acceptWorker +
                '}';
    }
}
