package com.qinfei.qferp.entity.biz;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.utils.DateUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Table(name = "t_biz_project_node_config")
@Data
public class ProjectNodeConfig implements Serializable {
    @Id
    private Integer id;

    private String code;

    private String name;

    private Double ratio;

    private Integer state;

    private String companyCode;

}