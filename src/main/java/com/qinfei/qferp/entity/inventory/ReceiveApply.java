package com.qinfei.qferp.entity.inventory;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 物品领用
 * @author tsf
 */
@Table(name = "t_saves_receive_apply")
@Data
public class ReceiveApply implements Serializable {
    @Id
    private Integer id;//物品领用id
    private String applyCode;//领用编号
    private Integer userId;//领用id
    private String userName;//领用姓名
    private Integer updateUserId;//修改人id
    private String title;//领用标题
    private Integer state;    //状态:0保存，4已提交
    private Double money;//领用金额
    private Integer wareId;//仓库id
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;    //领用日期
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;   //修改时间
    private String purpose;    //物品用途
    private String desc;    //备注
    private Integer itemId;  //待办id
    private String taskId;  //审核id
    private String affixLink;//附件链接
    private String affixName;//附件名称
    private String companyCode;  //公司代码
    @Transient
    private List<ReceiveDetails> details;

    @Override
    public String toString() {
        return "ReceiveApply{" +
                "id=" + id +
                ", applyCode='" + applyCode + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", updateUserId=" + updateUserId +
                ", title='" + title + '\'' +
                ", state=" + state +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", purpose='" + purpose + '\'' +
                ", money='" + money + '\'' +
                ", wareId='" + wareId + '\'' +
                ", desc='" + desc + '\'' +
                ", itemId=" + itemId +
                ", taskId='" + taskId + '\'' +
                ", affixLink='" + affixLink + '\'' +
                ", affixName='" + affixName + '\'' +
                ", companyCode='" + companyCode + '\'' +
                '}';
    }
}
