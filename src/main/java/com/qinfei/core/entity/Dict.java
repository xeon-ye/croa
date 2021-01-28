package com.qinfei.core.entity;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;

import com.qinfei.core.annotation.Column;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.utils.DataImportUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Table(name = "sys_dict")
public class Dict implements Serializable {

    @Id
    @Column(name = "id")
    private Integer id;
    private String typeCode;
    private String typeName;
    private String code;
    private String name;
    @Transient
    Integer[] deptIds;

    private Integer parentId;
    private byte disabled;
    private String type;
    private Integer createId;
    private Integer updateId;
    private Date createTime;
    private String desc;
    private String companyCode;
    private String createUser;
    private String updateUser;
    @Transient
    private String deptName;
    private Date updateTime;
    private int state;
    @Transient
    private Dict parent;
    @Transient
    private Set<Dict> childs;
    @Transient
    private List<Integer> inputUserId;
    @Transient
    private String assistantName;

    @Override
    public String toString() {
        return "Dict{" +
                "id=" + id +
                ", typeCode='" + typeCode + '\'' +
                ", typeName='" + typeName + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", deptIds=" + Arrays.toString(deptIds) +
                ", parentId=" + parentId +
                ", disabled=" + disabled +
                ", type='" + type + '\'' +
                ", createId=" + createId +
                ", updateId=" + updateId +
                ", createTime=" + createTime +
                ", desc='" + desc + '\'' +
                ", companyCode='" + companyCode + '\'' +
                ", createUser='" + createUser + '\'' +
                ", updateUser='" + updateUser + '\'' +
                ", deptName='" + deptName + '\'' +
                ", updateTime=" + updateTime +
                ", state=" + state +
                ", parent=" + parent +
                ", childs=" + childs +
                ", inputUserId=" + inputUserId +
                ", assistantName='" + assistantName + '\'' +
                '}';
    }
}
