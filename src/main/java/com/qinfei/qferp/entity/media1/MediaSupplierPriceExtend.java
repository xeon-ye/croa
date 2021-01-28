package com.qinfei.qferp.entity.media1;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @CalssName MediaSupplierPriceExtend
 * @Description 媒体供应商价格分组对象：媒体对应多个供应商使用
 * @Author xuxiong
 * @Date 2019/7/10 0010 12:32
 * @Version 1.0
 */
@Setter
@Getter
@ToString
public class MediaSupplierPriceExtend implements Serializable {
    private Integer id;
    private Integer relateUserId; //媒体供应商关系创建人
    private Integer supplierId; //供应商ID
    private String supplierName; //供应商名称
    private Integer enabled; //是否启用供应商
    private List<MediaSupplierPriceAudit> mediaPriceExtends; //供应商对象扩展价格

    private boolean editAddStatus; //判断该供应商价格分组是新增还是修改
    private boolean editEnableStatus; //判断供应商媒体关系启用状态是否修改
    private boolean auditsFlag; //是否需要审核标识，在媒体修改中使用
}
