package com.qinfei.qferp.entity.biz;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;

@Table(name = "t_biz_article_extend")
@Data
public class ArticleExtend implements Serializable {
    @Id
    private Integer id;

    private Integer articleId;

    private Integer projectId;

    private String projectCode;

    private String projectName;

}