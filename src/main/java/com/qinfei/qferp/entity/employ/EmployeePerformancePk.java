package com.qinfei.qferp.entity.employ;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.core.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by yanhonghao on 2019/4/23 11:47.
 */
@Table(name = "e_employee_performance_pk")
public class EmployeePerformancePk extends EmployCommon {

    /**
     * 主键id primary key
     */
    @Id
    private Integer id;

    /**
     * pk开始时间
     */
    @JSONField(format = "yyyy-MM-dd")
    private Date startDate;

    /**
     * pk结束时间
     */
    @JSONField(format = "yyyy-MM-dd")
    private Date endDate;

    /**
     * 本次pk名称
     */
    private String name;

    /**
     * 背景设置
     */
    private String backgroundImg;

    private int pkType;
    @Transient
    private List<String> leftPeopleIds = new ArrayList<>();
    @Transient
    private List<String> rightPeopleIds = new ArrayList<>();
    @Transient
    private List<String> leftPeopleNames = new ArrayList<>();
    @Transient
    private List<String> rightPeopleNames = new ArrayList<>();
    @Transient
    private List<EmployeePerformancePKEmployeeRelate> relates = new ArrayList<>();

    @Transient
    private String createYear;

    //查询条件
    @Transient
    private String companyCode;

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getCreateYear() {
        if (Objects.isNull(startDate)) return createYear;
        return DateUtils.format(startDate, "yyyy");
    }

    public void setCreateYear(String createYear) {
        this.createYear = createYear;
    }

    public List<EmployeePerformancePKEmployeeRelate> getRelates() {
        return relates;
    }

    public void setRelates(List<EmployeePerformancePKEmployeeRelate> relates) {
        this.relates = relates;
    }

    public List<String> getLeftPeopleNames() {
        return leftPeopleNames;
    }

    public void setLeftPeopleNames(List<String> leftPeopleNames) {
        this.leftPeopleNames = leftPeopleNames;
    }

    public List<String> getRightPeopleNames() {
        return rightPeopleNames;
    }

    public void setRightPeopleNames(List<String> rightPeopleNames) {
        this.rightPeopleNames = rightPeopleNames;
    }

    public int getPkType() {
        return pkType;
    }

    public void setPkType(int pkType) {
        this.pkType = pkType;
    }

    /**
     * 创建人ID；
     */
    private Integer createId;

    /**
     * 创建人名称；
     */
    private String createName;

    /**
     * 创建时间；
     */
    private Date createTime;

    /**
     * 更新人ID；
     */
    private Integer updateId;

    /**
     * 更新人名称；
     */
    private String updateName;

    /**
     * 更新时间；
     */
    private Date updateTime;

    /**
     * 状态，-1为删除；
     */
    private Integer state;

    /**
     * 版本号，每次更新会+1；
     */
    private Integer version;

    /**
     * 创建人ID；
     *
     * @return ：createId，创建人ID；
     */
    public Integer getCreateId() {
        return createId;
    }

    /**
     * 创建人ID；
     *
     * @param createId： 创建人ID；
     */
    public void setCreateId(Integer createId) {
        this.createId = createId;
    }

    /**
     * 创建人名称；
     *
     * @return ：createName，创建人名称；
     */
    public String getCreateName() {
        return createName;
    }

    /**
     * 创建人名称；
     *
     * @param createName： 创建人名称；
     */
    public void setCreateName(String createName) {
        this.createName = createName == null ? null : createName.trim();
    }

    /**
     * 创建时间；
     *
     * @return ：createTime，创建时间；
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间；
     *
     * @param createTime： 创建时间；
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 更新人ID；
     *
     * @return ：updateId，更新人ID；
     */
    public Integer getUpdateId() {
        return updateId;
    }

    /**
     * 更新人ID；
     *
     * @param updateId： 更新人ID；
     */
    public void setUpdateId(Integer updateId) {
        this.updateId = updateId;
    }

    /**
     * 更新人名称；
     *
     * @return ：updateName，更新人名称；
     */
    public String getUpdateName() {
        return updateName;
    }

    /**
     * 更新人名称；
     *
     * @param updateName： 更新人名称；
     */
    public void setUpdateName(String updateName) {
        this.updateName = updateName == null ? null : updateName.trim();
    }

    /**
     * 更新时间；
     *
     * @return ：updateTime，更新时间；
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 更新时间；
     *
     * @param updateTime： 更新时间；
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 状态，-1为删除；
     *
     * @return ：state，状态，-1为删除；
     */
    public Integer getState() {
        return state;
    }

    /**
     * 状态，-1为删除；
     *
     * @param state： 状态，-1为删除；
     */
    public void setState(Integer state) {
        this.state = state;
    }

    /**
     * 版本号，每次更新会+1；
     *
     * @return ：version，版本号，每次更新会+1；
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * 版本号，每次更新会+1；
     *
     * @param version： 版本号，每次更新会+1；
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<String> getLeftPeopleIds() {
        return leftPeopleIds;
    }

    public void setLeftPeopleIds(List<String> leftPeopleIds) {
        this.leftPeopleIds = leftPeopleIds;
    }

    public List<String> getRightPeopleIds() {
        return rightPeopleIds;
    }

    public void setRightPeopleIds(List<String> rightPeopleIds) {
        this.rightPeopleIds = rightPeopleIds;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBackgroundImg() {
        return backgroundImg;
    }

    public void setBackgroundImg(String backgroundImg) {
        this.backgroundImg = backgroundImg;
    }

    @Override
    public String toString() {
        return "EmployeePerformancePk{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", name='" + name + '\'' +
                ", backgroundImg='" + backgroundImg + '\'' +
                ", leftPeopleIds=" + leftPeopleIds +
                ", rightPeopleIds=" + rightPeopleIds +
                ", leftPeopleNames=" + leftPeopleNames +
                ", rightPeopleNames=" + rightPeopleNames +
                ", relates=" + relates +
                ", createYear='" + createYear + '\'' +
                ", companyCode='" + companyCode + '\'' +
                ", createId=" + createId +
                ", createName='" + createName + '\'' +
                ", createTime=" + createTime +
                ", updateId=" + updateId +
                ", updateName='" + updateName + '\'' +
                ", updateTime=" + updateTime +
                ", state=" + state +
                ", version=" + version +
                '}';
    }
}
