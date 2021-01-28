package com.qinfei.qferp.entity.inventoryStock;
import java.util.Date;
import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 进销存--出入库记录表（记录采购订单，领用单记录）(TSavesOutbound)实体类
 *
 * @author tsf
 * @since 2020-05-08 15:31:01
 */
@Table(name = "t_saves_outbound")
@Data
public class Outbound implements Serializable {
     @Id
     private Integer id;//出入库id
     private String code;//type=1:入库编号,2出款编号
     private Integer foreignId;//type=1:采购id,2领用id(根据外键id查询相应订单编号)
     private Integer type;//出入库类型:1为入库,2为出库
     private Integer state;//状态：1为入库，-1为出库
     private Integer wareId;//仓库id
     private Integer createId;//创建人id
     private String createName;//创建人姓名
     private Integer deptId;//创建人id
     private String deptName;//创建人id
     @JSONField(format = "yyyy-MM-dd HH:mm:ss")
     private Date createTime;//创建时间
     private Integer updateUserId;//修改人id
     @JSONField(format = "yyyy-MM-dd HH:mm:ss")
     private Date updateTime;//更新时间
     private String remark;//备注
     private String companyCode;//所属公司
     @Transient
     private String outBoundCode;//采购订单，领用单编号
     @Transient
     private String wareHouseName;//仓库名称
     @Transient
     private Integer purchaseId;//之前采购订单的id
     @Transient
     private String rejectReason;
}