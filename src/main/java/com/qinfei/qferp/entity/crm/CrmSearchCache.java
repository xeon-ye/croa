package com.qinfei.qferp.entity.crm;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 企查查客户缓存表(TCrmSearchCache)实体类
 * @author xuxiong
 * @since 2020-06-29 14:23:36
 */
@Table(name = "t_crm_search_cache")
@Data
public class CrmSearchCache implements Serializable {
    @Id
    private Integer id;//主键ID
    private String searchKeyword;//搜索关键字（公司名、注册号或社会统一信用代码）
    private String companyName;//公司名称
    private String companyLegal;//法人名称
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date establishDate;//成立日期
    private String companyStatus;//企业状态
    private String registerNum;//注册号
    private String creditCode;//社会统一信用代码
    private String originalName;//公司曾用名，逗号分隔
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date cacheDate;//缓存时间
}