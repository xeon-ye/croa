package com.qinfei.qferp.entity.inventory;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * 归还表(ReceiveReturn)实体类
 *
 * @author tsf
 * @since 2020-05-07 09:50:44
 */
@Table(name = "t_save_receive_return")
@Data
public class ReceiveReturn implements Serializable {
    private static final long serialVersionUID = -94040654443412217L;
    /**
    * 归还id
    */
    @Id
    private Integer id;
    /**
    * 出入库id
    */
    private Integer inventoryId;
    /**
    * 归还编号
    */
    private String code;
    /**
    * 归还标题
    */
    private String title;
    /**
    * 归还人id
    */
    private Integer userId;
    /**
    * 归还人姓名
    */
    private String userName;
    /**
    * 修改人id
    */
    private Integer updateUserId;
    /**
    * 创建时间
    */
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
    //判断是库存归还还是使用归还
    @Transient
    private Integer htmlFlag;

    @Override
    public String toString() {
        return "ReceiveReturn{" +
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