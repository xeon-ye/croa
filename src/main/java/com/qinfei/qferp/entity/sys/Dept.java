package com.qinfei.qferp.entity.sys;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Table(name = "sys_dept")
@Data
public class Dept implements Serializable {
    @Id
    private Integer id;

    private String code;

    private String type;

    private String name;

    private Integer parentId;

    private Integer level;

    private Integer state;

    private Integer creator;

    private Date createTime;

    private Integer updateUserId;

    private Date updateTime;

    private Integer mgrId ;

    private String mgrName ;

    private Integer mgrLeaderId ;

    private String mgrLeaderName ;

    private String companyCode ;

    private String companyCodeName;
    /**
     * 部门下的子部门
     */
    @Transient
    private List<Dept> depts = new ArrayList<Dept>();
    /**
     * 部门下的所有直接或间接下级部门
     */
    @Transient
    private List<Dept> childDepts = new ArrayList<Dept>();

    @Override
    public String toString() {
        return "Dept{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", level=" + level +
                ", state=" + state +
                ", creator=" + creator +
                ", createTime=" + createTime +
                ", updateUserId=" + updateUserId +
                ", updateTime=" + updateTime +
                ", mgrId=" + mgrId +
                ", mgrName='" + mgrName + '\'' +
                ", mgrLeaderId=" + mgrLeaderId +
                ", mgrLeaderName='" + mgrLeaderName + '\'' +
                ", companyCode='" + companyCode + '\'' +
                ", companyCodeName='" + companyCodeName + '\'' +
                '}';
    }
}