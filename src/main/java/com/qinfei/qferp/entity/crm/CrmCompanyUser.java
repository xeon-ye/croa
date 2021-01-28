package com.qinfei.qferp.entity.crm;

import java.util.Date;
import java.io.Serializable;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

/**
 * 表：对接人信息(TCrmCompanyUser)实体类
 *
 * @author jca
 * @since 2020-07-08 09:05:59
 */
@Data
@Table(name = "t_crm_company_user")
public class CrmCompanyUser implements Serializable {
    /**
    * 对接人主键
    */
    @Id
    private Integer id;
    /**
    * 对接人姓名
    */
    private String name;
    /**
    * 对接人所在的公司 FK
    */
    private Integer companyId;
    /**
    * 手机号
    */
    private String mobile;
    /**
    * 微信号
    */
    private String wechat;
    /**
    * QQ
    */
    private String qq;
    /**
     *  是否规范
     */
    private Integer normalize;
    /**
    * 部门
    */
    private String dept;
    /**
    * 负责项目
    */
    private String project;
    /**
    * 职位
    */
    private String job;
    /**
    * 客户类型 1大型，2中型，3小型
    */
    private Integer custType;
    /**
    * 专业程度
    */
    private String professionLevel;
    /**
    * 性格
    */
    private String personality;
    /**
    * 年龄
    */
    private Integer age;
    /**
    * 家庭
    */
    private String family;
    /**
    * 之前所在的公司
    */
    private String prevCompany;
    /**
    * 文化水平
    */
    private String education;
    /**
    * 爱好
    */
    private String hobby;
    /**
    * 长相
    */
    private String looks;
    /**
    * 成交详情
    */
    private String successDetail;
    /**
    * 意向度
    */
    private String intention;
    
    private String photo;
    /**
    * 客户答应打款天数（发布后多少时间打款）
    */
    private Integer promiseDay;
    /**
    * 税种
    */
    private String taxType;
    /**
    * 发票类型
    */
    private Integer invoiceType;
    /**
    * 发票抬头（发票字段）
    */
    private String invoiceTitle;
    /**
    * 税号（发票字段）
    */
    private String taxCode;
    /**
    * 银行账号（专票字段）
    */
    private String bankNo;
    /**
    * 银行名称（专票字段）
    */
    private String bankName;
    /**
    * 公司地址（专票字段）
    */
    private String address;
    /**
    * 公司电话（专票字段）
    */
    private String phone;
    /**
    * 客户保护状态:0默认，-1不建议合作，1保护
    */
    private Integer state;
    /**
    * 不建议合作理由
    */
    private String noReason;
    /**
    * 1.公海 0.非公海
    */
    private Integer isPublic;
    /**
     * 公海客户状态。默认0无效，1有效
     */
    private Integer isPublicState;
    /**
    * 客户开发状态（0.有效 1.待开发  2.流失 -9删除）
    */
    private Integer devState;
    
    private Integer deleteFlag;
    /**
    * 创建时间
    */
    private Date createTime;
    
    private Integer creator;
    /**
    * 创建人所在公司的代号
    */
    private String createCompanyCode;
    /**
    * 更新时间
    */
    private Date updateTime;
    
    private Integer updateUserId;
    /**
    * 是否是黑名单，1是，0否
    */
    private Integer isBlack;
    /**
    * 审核状态，1已审核，0未审核
    */
    private Integer auditFlag;
    /**
    * 考核时间
    */
    private Date evalTime;
    /**
     * 成交时间
     */
    private Date dealTime;
    /**
     * 保护等级
     */
    private Integer protectLevel;
    /**
     * 业务员部门id，客户公海流转后不能由本部门员工认领，记录上一个业务员的部门id
     */
    private Integer ywDeptId;
    /**
     * 是否强保护，默认0弱保护，对应protect_level=0；1强保护，对应protect_level=1,2,3
     * */
    private Integer protectStrong;

}