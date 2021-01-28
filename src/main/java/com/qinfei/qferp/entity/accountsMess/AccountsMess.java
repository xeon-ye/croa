package com.qinfei.qferp.entity.accountsMess;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 烂账管理
 * dsg
 */
@Table(name = "t_accounts_mess")
@Data
public class AccountsMess implements Serializable {
    private Integer id;

    private String code;

    private String title;

    private String applyName;

    private Integer applyId;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    private Integer state;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private  Date createTime;

    private Integer createUser;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private Integer updateUser;

    private String taskId;

    private Integer itemId;

    private String companyCode;

    private Double costSum;

    private Double sessionSum;

    private Double offerSum;

    private Double messSum;

    private Integer custCompanyId;

    private String custCompanyName;

    private Integer custId;

    private String custName;

    private String articleTime;

    private String note;

    @Transient
    private String articleIds;


}
