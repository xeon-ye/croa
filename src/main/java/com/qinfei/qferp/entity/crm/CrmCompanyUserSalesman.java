package com.qinfei.qferp.entity.crm;

import java.util.Date;
import java.io.Serializable;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

/**
 * (TCrmCompanyUserSalesman)实体类
 *
 * @author jca
 * @since 2020-07-08 09:06:40
 */
@Data
@Table(name = "t_crm_company_user_salesman")
public class CrmCompanyUserSalesman implements Serializable {
    @Id
    private Integer id;
    /**
    * 对接人表id crm_company_user表的FK
    */
    private Integer companyUserId;
    private Integer typeIn;
    private String typeOut;
    /**
    * 业务员id FK
    */
    private Integer userId;
    private String userName;
    private Integer deptId;
    private String deptName;

    private Date startTime;
    
    private Date endTime;
    /**
    * 生效标志位，1生效，0失效
    */
    private Integer state;
    private String remark;
    /**
    * 删除标志位，1删除，0不删除
    */
    private Integer deleteFlag;
    
    private Date createTime;
    
    private Integer creator;
    
    private Date updateTime;
    
    private Integer updateUserId;

}