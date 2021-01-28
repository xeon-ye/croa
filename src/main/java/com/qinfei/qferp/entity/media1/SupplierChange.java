package com.qinfei.qferp.entity.media1;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 供应商异动表(TSupplierChange)实体类
 *
 * @author xuxiong
 * @since 2020-08-05 17:18:13
 */
@Table(name = "t_supplier_change")
@Data
public class SupplierChange implements Serializable {
    @Id
    private Long id;//主键
    private Integer supplierId;//供应商ID
    private String supplierName;//供应商公司名称
    private String supplierContactor;//供应商联系人
    private Integer userId;//更新人：本次异常修改人员ID
    private String userName;//更新人：本次异常修改人员名称
    private String changeContent;//异动内容，缓存json数据，数据格式：参考数据库
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//异常时间
    private Byte state;//状态：0-有效、1-无效、-9删除
}