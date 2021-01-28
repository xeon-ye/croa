package com.qinfei.qferp.entity.inventory;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * 报废表(ReceiveScrap)实体类
 *
 * @author tsf
 * @since 2020-05-07 09:50:44
 */
@Table(name="t_save_receive_scrap")
@Data
public class ReceiveScrap implements Serializable {
    private static final long serialVersionUID = 967940610096872556L;
    /**
    * 报废id
    */
    @Id
    private Integer id;
    /**
    * 出入库id
    */
    private Integer inventoryId;
    /**
    * 报废编号
    */
    private String code;
    /**
    * 报废标题
    */
    private String title;
    /**
    * 报废人id
    */
    private Integer userId;
    /**
    * 报废人名称
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
    //判断是库存报废还是使用报废
    @Transient
    private Integer htmlFlag;

    @Override
    public String toString() {
        return "ReceiveScrap{" +
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