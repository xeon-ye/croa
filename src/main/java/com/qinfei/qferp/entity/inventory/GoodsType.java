package com.qinfei.qferp.entity.inventory;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 产品分类表
 * @author tsf
 */
@Table(name = "t_saves_goods_type")
@Data
public class GoodsType implements Serializable {
   @Id
   //产品分类id
   private Integer id;
   //产品分类编码
   private String code;
   //产品分类名称
   private String name;
   //父级id
   private Integer parentId;
   //状态
   private Integer state;
   //产品规格
   private String specs;
   //产品单位
   private String unit;
   //产品单价
   private Double price;
   //创建人
   private Integer createId;
   //创建人姓名
   private String createName;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    //创建时间
   private Date createTime;
    //修改人id
   private Integer updateUserId;
   //修改时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
   private Date updateTime;
   //描述
   private String description;
   //库存最大值
   private Integer stockMaxAmount;
   //库存最小值
   private Integer stockMinAmount;
   //公司代码
   private String companyCode;

   @Override
   public String toString() {
      return "GoodsType{" +
              "id=" + id +
              ", code='" + code + '\'' +
              ", name='" + name + '\'' +
              ", parentId=" + parentId +
              ", state=" + state +
              ", createId=" + createId +
              ", createName='" + createName + '\'' +
              ", createTime=" + createTime +
              ", updateUserId=" + updateUserId +
              ", updateTime=" + updateTime +
              ", description='" + description + '\'' +
              ", companyCode='" + companyCode + '\'' +
              '}';
   }
}
