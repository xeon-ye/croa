package com.qinfei.qferp.entity.crm;

import java.util.Date;
import java.io.Serializable;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

/**
 * (TCrmCompanyProtect)实体类
 *
 * @author jca
 * @since 2020-08-14 16:04:55
 */
@Data
@Table(name = "t_crm_company_protect")
public class CrmCompanyProtect implements Serializable {
    @Id
    private Integer id;
    /**
    * 公司id
    */
    private Integer companyId;
    
    private String companyName;
    
    private Integer protectLevel;
    
    private Integer applyId;
    
    private String applyName;
    /**
    * 申请时间
    */
    private Date applyTime;
    
    private Integer applyDeptId;
    
    private String applyDeptName;
    
    private Integer deleteFlag;
    /**
    * -1审批驳回，1审批成功，2客户管理员审批，3公司领导审批，4集团领导审批
    */
    private Integer state;
    
    private Integer updateUserId;
    
    private Date updateTime;
    
    private String taskId;
    
    private Integer itemId;

}