package com.qinfei.qferp.entity.inventory;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 物品供应商
 * @author tsf
 */
@EqualsAndHashCode(callSuper = true)
@Table(name = "t_saves_product_supplier")
@Data
public class PurchaseSupplier extends BaseRowModel implements Serializable{
    //供应商id
    @Id
    private Integer id;
    //供应商编号
    private String code;
    //供应商名称
    @ExcelProperty(index = 0,value = "供应商名称")
    private String name;

    @ExcelProperty(index = 1,value = "供应商资质")
    @Transient
    private String levelStr;
    //供应商资质0普通1中等2优质
    private Integer level;

    @ExcelProperty(index = 2,value = "产品分类")
    @Transient
    private String typeStr;
    //产品分类
    private Integer type;
    //状态
    private Integer state;
    //供应商地址
    @ExcelProperty(index = 3,value = "供应商地址")
    private String address;
    //备注：评价
    @ExcelProperty(index = 4,value = "备注")
    private String desc;
    //联系人姓名
    @ExcelProperty(index = 5,value = "联系人姓名")
    private String contactName;
    //联系方式
    @ExcelProperty(index = 6,value = "联系方式")
    private String contactPhone;

    @ExcelProperty(index = 7,value = "性别")
    @Transient
    private String contactSexStr;
    //联系人性别
    private Integer contactSex;
    //联系人部门
    @ExcelProperty(index = 8,value = "部门")
    private String deptName;
    //联系人职位
    @ExcelProperty(index = 9,value = "职位")
    private String position;
    //联系人邮箱
    @ExcelProperty(index = 10,value = "邮箱")
    private String email;

    @ExcelProperty(index = 11,value = "结算方式")
    @Transient
    private String payMethodStr;
    //结算方式：0微信1支付宝2银行卡
    private Integer payMethod;
    //微信账号
    @ExcelProperty(index = 12,value = "微信")
    private String weixin;
    //支付宝账号
    @ExcelProperty(index = 13,value = "支付宝")
    private String zhifubao;
    //银行账号
    @ExcelProperty(index = 14,value = "银行账号")
    private String bankNo;
    //账户名称
    private String accountName;
    //开户银行
    private String bankName;
    //开户地
    private String bankPlace;
    //创建时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    //公司代码
    private String companyCode;
    //修改人id
    private Integer updateUserId;
    //修改人时间
    private Date updateTime;

    @Override
    public String toString() {
        return "PurchaseSupplier{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", type=" + type +
                ", state=" + state +
                ", address='" + address + '\'' +
                ", desc='" + desc + '\'' +
                ", contactName='" + contactName + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", contactSexStr='" + contactSexStr + '\'' +
                ", contactSex=" + contactSex +
                ", deptName='" + deptName + '\'' +
                ", position='" + position + '\'' +
                ", email='" + email + '\'' +
                ", payMethod=" + payMethod +
                ", weixin='" + weixin + '\'' +
                ", zhifubao='" + zhifubao + '\'' +
                ", bankNo='" + bankNo + '\'' +
                ", accountName='" + accountName + '\'' +
                ", bankName='" + bankName + '\'' +
                ", bankPlace='" + bankPlace + '\'' +
                ", createTime=" + createTime +
                ", companyCode='" + companyCode + '\'' +
                ", updateUserId=" + updateUserId +
                ", updateTime=" + updateTime +
                '}';
    }
}
