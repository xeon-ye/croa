package com.qinfei.qferp.entity.inventoryStock;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.inventory.PurchaseDetails;
import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/*进销存入库表*/
@Table(name = "t_saves_storage")
@Data
public class InventoryStock  implements Serializable {
    @Id
    private Integer id;

    //入库编号
    private String storageCode;

    //关联采购订单id
    private Integer purchaseOrder;

    //总计金额
    private Double aggregateAmount;

    //入库状态
    private Integer state;

    //创建者id
    private Integer createrUser;

    //
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    //
    private Integer updateUser;

    private Integer createDeptId;

    private String createDept;

    @Transient
    private List<PurchaseDetails> purchaseDetailsList;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}
