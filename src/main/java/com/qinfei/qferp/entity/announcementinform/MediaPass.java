package com.qinfei.qferp.entity.announcementinform;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;

import java.io.Serializable;
import java.util.Date;

/**
 * 通知公告
 * @author  dengshenggeng
 */
@Table(name="t_announcement_inform")
public class MediaPass implements Serializable  {
    @Id
    /**
     * 通知公告id
     */
    private Integer id;

    /**
     * 事务类型id
     */

    private Integer transactionType;

    /**
     * 资源类型id
     */

    private Integer resourceType;

    /**
     * 生效部门id
     */

    private Integer operationDeptId;

    /**
     * 发布时间
     */

    private Date releaseTime;

    /**
     * 发布部门id
     */

    private Integer publishDeptId;

    /**
     * 发布人
     */

    private String releaseUser;

    /**
     * 标签
     */

    private String label;

    /**
     * 资源状态
     */

    private Integer resourcesState;

    /**
     * 生效部门
     */

    private String operationDaptName;

    /**
     * 发布部门
     */

    private String publishDeptName;

    /**
     * 标题啊
     */

    private String title;

    /**
     * 内容
     */

    private String content;

    /**
     * 创建者id
     */

    private Integer createId;

    /**
     * 创建人
     */

    private String createName;

    /**
     * 发布人
     */

    private Date createTime;

    private Integer updateId;

    private String updateName;

    private Date updateTime;

    private Integer state;


    private Integer version;

    /**
     * 附件
     */

    private String attachment;

    /**
     * 通知编号
     */

    private String no;

    /**
     * 链接
     */

    private String attachmentLink ;

    /**
     * 发布截止时间
     */

    private Date releaseTimeEnd;

    /**
     * 是否强制
     */

    private Integer mandatory;

    private String companyCode;

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    @Transient
    private  String transactionName;

    @Transient
    private  String resourceName;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public Integer getMandatory() {
        return mandatory;
    }

    public void setMandatory(Integer mandatory) {
        this.mandatory = mandatory;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Integer transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getResourceType() {
        return resourceType;
    }

    public void setResourceType(Integer resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getOperationDeptId() {
        return operationDeptId;
    }

    public void setOperationDeptId(Integer operationDeptId) {
        this.operationDeptId = operationDeptId;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
    }

    public Integer getPublishDeptId() {
        return publishDeptId;
    }

    public void setPublishDeptId(Integer publishDeptId) {
        this.publishDeptId = publishDeptId;
    }

    public String getReleaseUser() {
        return releaseUser;
    }

    public void setReleaseUser(String releaseUser) {
        this.releaseUser = releaseUser;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getResourcesState() {
        return resourcesState;
    }

    public void setResourcesState(Integer resourcesState) {
        this.resourcesState = resourcesState;
    }

    public String getOperationDaptName() {
        return operationDaptName;
    }

    public void setOperationDaptName(String operationDaptName) {
        this.operationDaptName = operationDaptName;
    }

    public String getPublishDeptName() {
        return publishDeptName;
    }

    public void setPublishDeptName(String publishDeptName) {
        this.publishDeptName = publishDeptName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getCreateId() {
        return createId;
    }

    public void setCreateId(Integer createId) {
        this.createId = createId;
    }

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getUpdateId() {
        return updateId;
    }

    public void setUpdateId(Integer updateId) {
        this.updateId = updateId;
    }

    public String getUpdateName() {
        return updateName;
    }

    public void setUpdateName(String updateName) {
        this.updateName = updateName;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getAttachmentLink() {
        return attachmentLink;
    }

    public void setAttachmentLink(String attachmentLink) {
        this.attachmentLink = attachmentLink;
    }

    public Date getReleaseTimeEnd() {
        return releaseTimeEnd;
    }

    public void setReleaseTimeEnd(Date releaseTimeEnd) {
        this.releaseTimeEnd = releaseTimeEnd;
    }

    @Override
    public String toString() {
        return "MediaPass{" +
                "id=" + id +
                ", transactionType=" + transactionType +
                ", resourceType=" + resourceType +
                ", operationDeptId=" + operationDeptId +
                ", releaseTime=" + releaseTime +
                ", publishDeptId=" + publishDeptId +
                ", releaseUser='" + releaseUser + '\'' +
                ", label='" + label + '\'' +
                ", resourcesState=" + resourcesState +
                ", operationDaptName='" + operationDaptName + '\'' +
                ", publishDeptName='" + publishDeptName + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createId=" + createId +
                ", createName='" + createName + '\'' +
                ", createTime=" + createTime +
                ", updateId=" + updateId +
                ", updateName='" + updateName + '\'' +
                ", updateTime=" + updateTime +
                ", state=" + state +
                ", version=" + version +
                ", attachment='" + attachment + '\'' +
                ", no='" + no + '\'' +
                ", attachmentLink='" + attachmentLink + '\'' +
                ", releaseTimeEnd=" + releaseTimeEnd +
                ", mandatory=" + mandatory +
                ", companyCode='" + companyCode + '\'' +
                ", transactionName='" + transactionName + '\'' +
                ", resourceName='" + resourceName + '\'' +
                '}';
    }
}