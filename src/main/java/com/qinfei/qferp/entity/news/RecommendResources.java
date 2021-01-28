package com.qinfei.qferp.entity.news;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 会议室管理
 *
 * @author  dengshenggeng
 */
@Table(name="t_recommend_resources")
@Data
public class RecommendResources implements Serializable {
    @Id
    private Integer id;

    private String companyCode;

    private String title;

    private String resourceTitle;

    private String href;

    private Integer plateId;

    private Integer createId;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private Integer updateId;

    private Integer isDelete;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String content;

    @Transient
    private String queryValue;
    @Transient
    private String plateName;
    @Transient
    List<Integer> mediaPlateIdList;

}