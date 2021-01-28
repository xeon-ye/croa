package com.qinfei.qferp.entity.news;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 会议室管理
 *
 * @author  dengshenggeng
 */
@Table(name="t_news")
@Data
public class News  implements Serializable {
    @Id
    private Integer id;

    private Integer type; //1-集团新闻 2-公司新闻 3 - 图文新闻 4 - 优秀事迹

    private String companyCode;

    private String title;

    private String content;

    private Integer createId;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
   private Date createTime;

    private Integer updateId;

    private Integer isDelete;

    private Date updateTime;

    private String imageUrl;

    @Transient
    private String queryValue;

}