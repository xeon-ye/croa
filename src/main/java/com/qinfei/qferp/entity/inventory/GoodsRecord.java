package com.qinfei.qferp.entity.inventory;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (GoodsRecord)实体类
 *
 * @author tsf
 * @since 2020-05-21 09:59:19
 */
@Table(name = "t_saves_goods_record")
@Data
public class GoodsRecord implements Serializable {
    @Id
    private Integer id;//物品库存记录
    private Integer type;//库存操作：1报修，2、报废3、归还
    private Integer state;//状态
    private Integer foreignId;//报修，报废，归还idFK
    private Integer inventoryId;//库存id
    private Integer userId;//创建人id
    private String userName;//创建人姓名
    private String taskId;//流程Id
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;//创建时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;//修改时间
    private Integer updateUserId;//修改人id
    private String companyCode;//公司代码
}