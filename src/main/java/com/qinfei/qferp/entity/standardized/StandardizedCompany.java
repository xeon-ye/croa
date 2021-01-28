package com.qinfei.qferp.entity.standardized;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Column;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.util.Date;

/**
 * @author: 66
 * @Date: 2020/11/13 10:10
 * @Description: 标准化公司申请 bean
 */
@Data
@Table(name = "t_standardized_company_application")
public class StandardizedCompany {
    @Id
    @Column(name = "id")
    private Integer id;
    /**
     * 申请编号
     */
    private String no;
    /**
     * 公司名称
     */
    private String companyName;
    /**
     * 申请人id
     */
    private Integer applyId;
    /**
     * 申请人姓名
     */
    private String applyName;
    /**
     * 申请人部门id
     */
    private Integer deptId;
    /**
     * 申请人部门名称
     */
    private String deptName;
    /**
     * 申请时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;
    /**
     * 状态 -9 删除 ； 0为正常； 1为已完成 ； 2保存； -1为驳回；
     */
    private Integer state;
    /**
     * 创建人
     */
    private Integer createUserId;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 修改人
     */
    private Integer updateUserId;
    /**
     * 修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    /**
     * 流程id
     */
    private String taskId;
    /**
     * 待办id
     */
    private Integer itemId;
    /**
     * 备注
     */
    private String remarks;
    /**
     * 公司代码
     */
    private String companyCode;

}
