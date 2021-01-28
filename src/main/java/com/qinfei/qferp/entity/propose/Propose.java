package com.qinfei.qferp.entity.propose;
import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 建议管理实体类
 * @Author ：tsf;
 */
@Table(name = "t_propose")
public class Propose implements Serializable {
    @Id
    /**
     * 建议id
     */
    private Integer id;
    /**
     * 录入人id
     */
    private Integer userId;
    /**
     * 录入人姓名
     */
    private String name;
    /**
     * 录入部门id
     */
    private Integer deptId;
    /**
     * 录入部门名称
     */
    private String deptName;
    /**
     * 状态：0未处理，1已处理，2处理中,3已确认，4，已驳回，-9删除
     */
    private Integer state;
    /**
     * 录入年
     */
    private Integer year;
    /**
     * 录入月
     */
    private Integer month;
    /**
     * 录入时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date entryTime;
    /**
     * 建议类型
     */
    private Integer proposeType;
    /**
     * 问题描述
     */
    private String problemDescription;
    /**
     * 期待的解决方案
     */
    private String expectSolution;
    /**
     * 处理人
     */
    private String handlePerson;
    /**
     * 处理时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;
    /**
     * 处理结果:0同意,1不同意，2待议，3指定给其他人，4其它
     */
    private Integer handleResult;
    /**
     * 指定人
     */
    private Integer appointPerson;
    /**
     * 处理意见
     */
    private String handleAdvice;
    /**
     * 公司编码
     */
    private String companyCode;
    /**
     * 待办标志
     */
    private String itemId;
    /**
     * update_time
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    /**
     * 用来储存建议类型的字段
     */
    @Transient
    private String adviceType;
    @Transient
    private String title;
    @Transient
    private String userName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Date getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Date entryTime) {
        this.entryTime = entryTime;
    }

    public Integer getProposeType() {
        return proposeType;
    }

    public void setProposeType(Integer proposeType) {
        this.proposeType = proposeType;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getExpectSolution() {
        return expectSolution;
    }

    public void setExpectSolution(String expectSolution) {
        this.expectSolution = expectSolution;
    }

    public String getHandlePerson() {
        return handlePerson;
    }

    public void setHandlePerson(String handlePerson) {
        this.handlePerson = handlePerson;
    }

    public Date getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(Date handleTime) {
        this.handleTime = handleTime;
    }

    public Integer getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(Integer handleResult) {
        this.handleResult = handleResult;
    }

    public Integer getAppointPerson() {
        return appointPerson;
    }

    public void setAppointPerson(Integer appointPerson) {
        this.appointPerson = appointPerson;
    }

    public String getHandleAdvice() {
        return handleAdvice;
    }

    public void setHandleAdvice(String handleAdvice) {
        this.handleAdvice = handleAdvice;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getAdviceType() {
        return adviceType;
    }

    public void setAdviceType(String adviceType) {
        this.adviceType = adviceType;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Propose{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", state=" + state +
                ", year=" + year +
                ", month=" + month +
                ", entryTime=" + entryTime +
                ", proposeType=" + proposeType +
                ", problemDescription='" + problemDescription + '\'' +
                ", expectSolution='" + expectSolution + '\'' +
                ", handlePerson='" + handlePerson + '\'' +
                ", handleTime=" + handleTime +
                ", handleResult=" + handleResult +
                ", appointPerson=" + appointPerson +
                ", handleAdvice='" + handleAdvice + '\'' +
                ", companyCode='" + companyCode + '\'' +
                ", itemId=" + itemId +
                ", updateTime=" + updateTime +
                ", adviceType='" + adviceType + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Propose propose = (Propose) o;
        return Objects.equals(id, propose.id) &&
                Objects.equals(userId, propose.userId) &&
                Objects.equals(name, propose.name) &&
                Objects.equals(deptId, propose.deptId) &&
                Objects.equals(deptName, propose.deptName) &&
                Objects.equals(state, propose.state) &&
                Objects.equals(year, propose.year) &&
                Objects.equals(month, propose.month) &&
                Objects.equals(entryTime, propose.entryTime) &&
                Objects.equals(proposeType, propose.proposeType) &&
                Objects.equals(problemDescription, propose.problemDescription) &&
                Objects.equals(expectSolution, propose.expectSolution) &&
                Objects.equals(handlePerson, propose.handlePerson) &&
                Objects.equals(handleTime, propose.handleTime) &&
                Objects.equals(handleResult, propose.handleResult) &&
                Objects.equals(appointPerson, propose.appointPerson) &&
                Objects.equals(handleAdvice, propose.handleAdvice) &&
                Objects.equals(companyCode, propose.companyCode) &&
                Objects.equals(itemId, propose.itemId) &&
                Objects.equals(updateTime, propose.updateTime) &&
                Objects.equals(adviceType, propose.adviceType) &&
                Objects.equals(title, propose.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, name, deptId, deptName, state, year, month, entryTime, proposeType, problemDescription, expectSolution, handlePerson, handleTime, handleResult, appointPerson, handleAdvice, companyCode, itemId, updateTime, adviceType, title);
    }
}
