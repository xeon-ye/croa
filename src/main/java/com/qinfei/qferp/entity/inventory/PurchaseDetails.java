package com.qinfei.qferp.entity.inventory;
import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;

/**
 * 采购明细实体类
 * @author tsf
 */
@Table(name = "t_saves_product_details")
@Data
public class PurchaseDetails implements Serializable {
    //产品明细编号
    @Id
    private Integer detailId;
    //物品采购id
    private Integer purchaseId;
    //产品分类
    private Integer type;
    //产品id
    private Integer goodsId;
    //供应商id
    private Integer supplierId;
    //状态
    private Integer state;
    //数量
    private Integer amount;
    //单价
    private Double price;
    //总额
    private Double totalMoney;
    //支付方式
    private Integer payMethod;
    //仓库id
    private Integer warehouseId;
    //创建id
    private Integer createId;
    //创建时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    //更新时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    //更新人
    private Integer updateUserId;
    @Transient
    private Purchase purchase;
    //产品名称
    @Transient
    private String goodsName;
    //产品规格
    @Transient
    private String specs;
    //产品单位
    @Transient
    private String unit;

    @Override
    public String toString() {
        return "PurchaseDetails{" +
                "detailId=" + detailId +
                ", purchaseId=" + purchaseId +
                ", type=" + type +
                ", goodsId=" + goodsId +
                ", supplierId=" + supplierId +
                ", state=" + state +
                ", amount=" + amount +
                ", price=" + price +
                ", totalMoney=" + totalMoney +
                ", payMethod=" + payMethod +
                ", purchase=" + purchase +
                ", warehouseId=" + warehouseId +
                ", createId=" + createId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", updateUserId=" + updateUserId +
                '}';
    }
}
