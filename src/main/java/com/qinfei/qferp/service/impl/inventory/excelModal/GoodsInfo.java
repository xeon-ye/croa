package com.qinfei.qferp.service.impl.inventory.excelModal;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

import java.io.Serializable;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
//物品导入模板
public class GoodsInfo extends BaseRowModel implements Serializable {

    private Integer id;
    //产品分类名称
    @ExcelProperty(index = 0,value = "产品分类")
    private String typeName;
    @ExcelProperty(index = 1,value = "产品名称")
    private String name;
    //产品编码
    @ExcelProperty(index = 2,value = "产品编码")
    private String code;
    //状态
    private Integer state;
    //产品分类id
    private Integer typeId;
    //产品数量
    private Integer number;
    //单位
    @ExcelProperty(index =3,value = "产品单位")
    private String unit;
    //单价
    private Double price;
    @ExcelProperty(index = 4,value = "产品单价")
    private String priceStr;
    //规格
    @ExcelProperty(index = 5,value = "产品规格")
    private String specs;
    //创建人
    private Integer createId;
    //创建人
    private String createName;
    //创建时间
    private Date createTime;
    //修改人id
    private Integer updateUserId;
    //修改时间
    private Date updateTime;
    //描述
    private String description;
    //公司代码
    private String companyCode;
}
