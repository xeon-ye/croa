package com.qinfei.qferp.entity.inventory;
import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.io.Serializable;

/**
 * 报修表(ReceiveRepair)实体类
 *
 * @author tsf
 * @since 2020-05-07 09:50:40
 */
@Table(name = "t_save_receive_repair")
@Data
public class ReceiveRepair implements Serializable {
    private static final long serialVersionUID = -70182760284318180L;
    /**
    * 报修id
    */
    @Id
    private Integer id;
    /**
    * 库存id
    */
    private Integer inventoryId;
    /**
    * 报修编号
    */
    private String code;
    /**
    * 报修标题
    */
    private String title;
    
    private Integer userId;
    
    private String userName;
    
    private Integer updateUserId;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
    * 修改时间
    */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    /**
    * 0正常-9删除
    */
    private Integer state;
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
    private String remark;
    /**
    * 公司代码
    */
    private String companyCode;
    @Transient
    private String deptName;
    //判断是库存报修还是使用报修
    @Transient
    private Integer htmlFlag;

    @Override
    public String toString() {
        return "ReceiveRepair{" +
                "id=" + id +
                ", inventoryId=" + inventoryId +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", updateUserId=" + updateUserId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", state=" + state +
                ", taskId='" + taskId + '\'' +
                ", itemId=" + itemId +
                ", remark='" + remark + '\'' +
                ", companyCode='" + companyCode + '\'' +
                '}';
    }
}