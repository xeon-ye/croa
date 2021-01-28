package com.qinfei.qferp.entity.inventoryStock;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;

/**
 * InventoryCheckRecord实体类
 *
 * @author tsf
 * @since 2020-06-02 16:18:49
 */
@Table(name = "t_saves_check_details")
@Data
public class InventoryCheckDetails implements Serializable {
        @Id
        private Integer id;//库存盘点明细id
        private Integer checkId;//库存盘点id
        private Integer goodsId;//产品id
        private Integer state;//状态
        private Integer stockAmount;//库存数量
        private Integer checkAmount;//盘点数量
        private Integer profitAmount;//盘盈数量
        private Integer lossAmount;//盘亏数量
        private Integer createId;//创建人id
        private String createName;//创建人姓名
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date createTime;//创建时间
        private Integer updateUserId;//修改人id
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date updateTime;//修改时间
        private String remark;
        @Transient
        private String goodsCode;
        @Transient
        private String goodsName;
        @Transient
        private String specs;
        @Transient
        private Double price;

         @Override
         public String toString() {
             return "InventoryCheckRecord{" +
                     "id=" + id +
                     ", checkId=" + checkId +
                     ", goodsId=" + goodsId +
                     ", state=" + state +
                     ", stockAmount=" + stockAmount +
                     ", checkAmount=" + checkAmount +
                     ", profitAmount=" + profitAmount +
                     ", lossAmount=" + lossAmount +
                     ", createId=" + createId +
                     ", createName='" + createName + '\'' +
                     ", createTime=" + createTime +
                     ", updateUserId=" + updateUserId +
                     ", updateTime=" + updateTime +
                     ", remark='" + remark + '\'' +
                     '}';
         }
}