package com.qinfei.qferp.entity.inventory;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 物品领用明细
 * @author tsf
 */
@Table(name = "t_saves_receive_details")
@Data
public class ReceiveDetails implements Serializable {
      @Id
      private Integer id;//物品领用明细id
      private Integer applyId;//物品领用id
      private Integer type;//产品分类id
      private Integer goodsId;//产品id
      private String unit;//单位
      private Integer amount;//领用数量
      private Integer outAmount;//出库数量
      private Double price;//单价
      private Double totalMoney;//总金额
      private Integer state;    //状态:0保存，1全部出库,2部分出库,-9删除
      private Integer userId;//使用人id
      private Integer updateUserId;//修改人id
      @JSONField(format = "yyyy-MM-dd HH:mm:ss")
      private Date createTime;    //创建日期
      @JSONField(format = "yyyy-MM-dd HH:mm:ss")
      private Date updateTime;   //修改时间
      @JSONField(format = "yyyy-MM-dd HH:mm:ss")
      private Date returnTime;   //归还时间
      private String companyCode;  //公司代码
      @Transient
      private Integer stockAmount; //当前库存

      @Override
      public String toString() {
            return "ReceiveDetails{" +
                    "id=" + id +
                    ", applyId=" + applyId +
                    ", type=" + type +
                    ", goodsId=" + goodsId +
                    ", unit='" + unit + '\'' +
                    ", amount=" + amount +
                    ", state=" + state +
                    ", userId=" + userId +
                    ", updateUserId=" + updateUserId +
                    ", createTime=" + createTime +
                    ", updateTime=" + updateTime +
                    ", returnTime=" + returnTime +
                    ", companyCode='" + companyCode + '\'' +
                    '}';
      }
}
