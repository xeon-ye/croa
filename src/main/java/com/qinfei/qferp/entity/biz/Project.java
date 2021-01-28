package com.qinfei.qferp.entity.biz;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.utils.DateUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Table(name = "t_biz_project")
@Data
public class Project implements Serializable {
    @Id
    private Integer id;

    private String code;

    private String name;

    private Integer state;

    private Integer disabled;

    private Integer applyId;

    private String applyName;

    private Integer applyDeptId;

    private String applyDeptName;

    @JSONField(format = DateUtils.DEFAULT)
    private Date applyTime;

    private Integer updateUserId;
    @JSONField(format = DateUtils.DATE_SMALL)
    private Date updateTime;

    private String companyCode;

    private Integer orderNum;

    private String taskId;

    private Integer itemId;
}