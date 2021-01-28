package com.qinfei.qferp.entity.inventory;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 库存表
 * @author tsf
 */
@Table(name = "t_saves_goods")
@Data
public class Goods implements Serializable{
    @Id
    //产品id
    private Integer id;
    //产品编码
    private String code;
    //状态:0入库，-1出库，1报修，2、报废3、归还
    private Integer state;
    //产品分类id
    private Integer typeId;
    //产品id
    private Integer goodsId;
    //产品数量
    private Integer number;
    //物品采购id
    private Integer purchaseId;
    //物品领用id
    private Integer applyId;
    //创建人id
    private Integer createId;
    //创建人姓名
    private String createName;
    //使用人id
    private Integer userId;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    //创建时间
    private Date createTime;
    //修改人id
    private Integer updateUserId;
    //修改时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    //归还时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date returnTime;
    //公司代码
    private String companyCode;
    //仓库id
    private Integer warehouseId;

    //产品分类名称
    @Transient
    private String typeName;

    @Override
    public String toString() {
        return "Goods{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", state=" + state +
                ", typeId=" + typeId +
                ", number=" + number +
                ", createId=" + createId +
                ", createTime=" + createTime +
                ", updateUserId=" + updateUserId +
                ", updateTime=" + updateTime +
                ", returnTime=" + returnTime +
                ", companyCode='" + companyCode + '\'' +
                '}';
    }
}
