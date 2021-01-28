package com.qinfei.qferp.entity.biz;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.utils.DateUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Table(name = "t_biz_project_node")
@Data
public class ProjectNode implements Serializable {
    @Id
    private Integer id;

    private Integer projectId;

    private String code;

    private String name;

    private String type;

    private Integer userId;

    private String userName;

    private Integer deptId;

    private String deptName;

    private Integer createId;
    @JSONField(format = DateUtils.DEFAULT)
    private Date createTime;

    private Integer updateUserId;
    @JSONField(format = DateUtils.DEFAULT)
    private Date updateTime;

    private Double ratio;

    private Integer state;

    private String companyCode;

    private Integer index;
}