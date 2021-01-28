package com.qinfei.qferp.entity.media1;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 媒体品牌实体类
 *
 * @author tsf
 * @since 2020-10-21 09:30:20
 */
@Table(name = "t_media_brand")
@Data
public class MediaBrand implements Serializable {
        @Id
        private Integer id;//主键ID
        private String brandName;//品牌名称
        private Integer brandType;//微博提取方式
        private String userName;//媒体用户名称
        private String userId;//媒体用户ID
        private Integer platform;//平台：1-抖音，2-小红书，3-微博
        private String relateId;//关联ID
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date createDate;//添加时间
}