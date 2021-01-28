package com.qinfei.qferp.entity.media1;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.media.Supplier;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @CalssName MediaSupplierRelateAudit
 * @Description  媒体板块关系表
 * @Author xuxiong
 * @Date 2019/6/25 0025 18:25
 * @Version 1.0
 */
@Table(name = "t_media_supplier_relate_audit")
@Getter
@Setter
@ToString
public class MediaSupplierRelateAudit implements Serializable {
    private Integer id; //主键
    private Integer mediaId; //媒体ID
    private Integer supplierId; //供应商ID
    private Integer enabled; //是否可用，0-可用、1-禁用
    private Integer isCopy; //是否拷贝媒体 0-自建、1-拷贝
    private String copyRemarks; //拷贝媒体备注
    private Integer state; //状态：0-正常、-9-删除
    private Integer createId; //创建者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer updateId; //更新者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer versions; //版本号
    private Integer isDelete; //逻辑删除 0 false 正常 ,1 true 删除

    @Transient
    private Supplier supplier; // 对应供应商信息
    @Transient
    private List<MediaSupplierPriceAudit> mediaSupplierPriceAuditList; //媒体供应商价格

}
