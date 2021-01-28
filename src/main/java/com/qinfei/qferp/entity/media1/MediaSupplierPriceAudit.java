package com.qinfei.qferp.entity.media1;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @CalssName MediaSupplierPriceAudit
 * @Description  供应商价格表
 * @Author xuxiong
 * @Date 2019/6/25 0025 18:25
 * @Version 1.0
 */
@Table(name = "t_media_supplier_price_audit")
@Getter
@Setter
@ToString
public class MediaSupplierPriceAudit implements Serializable {
    private Integer id; //主键
    private Integer mediaSupplierRelateId; //媒体供应商关系ID
    private String cell; //列名称
    private String cellName; //列描述
    private String cellValue; // 列值
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer versions; //版本号
    private String cellType; //显示类型 radio checkbox select textarea input image file number price date datetime
    private String cellValueText; //列对应的中文值，主要用于扩展字段中单选框、复选框、下拉列表中文描述

    @Transient
    private boolean editAddStatus; //判断该供应商价格分组是新增还是修改


}
