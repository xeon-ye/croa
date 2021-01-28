package com.qinfei.qferp.entity.fee;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;

import java.io.Serializable;
import java.util.Date;

@Table(name = "fee_outgo_article")
public class OutgoArticle implements Serializable {
    @Id
    private Integer id;
    private Integer outgoId ;
    private Integer articleId ;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOutgoId() {
        return outgoId;
    }

    public void setOutgoId(Integer outgoId) {
        this.outgoId = outgoId;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    @Override
    public String toString() {
        return "OutgoArticle{" +
                "id=" + id +
                ", outgoId=" + outgoId +
                ", articleId=" + articleId +
                '}';
    }
}
