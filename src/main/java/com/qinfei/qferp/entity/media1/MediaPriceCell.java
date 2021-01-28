package com.qinfei.qferp.entity.media1;

import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @CalssName MediaPriceCell
 * @Description 价格对象
 * @Author xuxiong
 * @Date 2019/7/16 0016 15:05
 * @Version 1.0
 */
@Getter
@Setter
@ToString
public class MediaPriceCell implements Serializable {
    private Integer supplierId; //供应商Id
    private String supplierCompanyName; //供应商名称
    private String supplierName; //供应商联系人名称
    private Integer supplierCreator; //供应商责任人或媒体供应商关系创建人
    private String supplierPhone;//供应商联系手机号
    private Integer relateId; //媒体供应商关系Id
    private Integer isCopy; //是否拷贝媒体 0-自建、1-拷贝
    private String copyRemarks; //拷贝媒体备注
    private String cell; //价格列名
    private String cellName; //价格列描述
    private String cellValue; //价格列值
    private Integer enabled; //是否启用供应商，0-启用、1-禁用
    private String companyCode; //供应商公司名称
    private Integer supplierRelateState; //供应商媒体关系状态
    private String cellType; //显示类型 radio checkbox select textarea input image file number price date datetime
    private String cellValueText; //列对应的中文值，主要用于扩展字段中单选框、复选框、下拉列表中文描述
    @Transient
    private String plateIds;//供应商绑定媒体对应的板块id
    @Transient
    private Integer deptId;
    @Transient
    private boolean flag;
}
