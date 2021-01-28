package com.qinfei.qferp.entity.biz;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 稿件媒体供应商替换记录表(TBizArticleReplace)实体类
 *
 * @author xuxiong
 * @since 2020-08-07 09:41:54
 */
@Table(name = "t_biz_article_replace")
@Data
public class ArticleReplace implements Serializable {
    @Id
    private Long id;//主键
    private Integer plateId;//媒体板块
    private Integer oldMediaId;//历史媒体ID
    private String oldMediaName;//历史媒体名称
    private Integer oldSupplierId;//历史供应商ID
    private String oldSupplierName;//历史供应商公司名称
    private String oldSupplierContactor;//历史供应商联系人
    private Integer newMediaId;//替换的媒体ID
    private Integer newSupplierId;//替换的供应商ID
    private String articleIdList;//替换的稿件ID集合，逗号分割
    private String outgoIdList;//替换的请款表ID集合，逗号分割
    private String dropIdList;//替换的删稿表ID集合，逗号分割
    private Integer userId;//替换人ID
    private String userName;//替换人名称
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//替换时间
    private Byte state;//状态：0-有效、1-无效、-9删除
}