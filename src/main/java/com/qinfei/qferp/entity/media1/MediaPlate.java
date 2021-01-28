package com.qinfei.qferp.entity.media1;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 * @CalssName MediaPlate
 * @Description  媒体板块
 * @Author xuxiong
 * @Date 2019/6/25 0025 18:25
 * @Version 1.0
 */
@Table(name = "t_media_plate")
@Data
public class MediaPlate implements Serializable {
    private Integer id; //主键
    private String name; //板块名称
    private Integer plateTypeId; //板块类型ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer isDelete; //逻辑删除 0 false 正常 1 true 删除
    private Integer versions; //版本号
    private Double percent;//提成百分
    private Integer parentType; //父级板块类型：1-网络、2-新媒体
    private Byte standarPlatformFlag; //是否标准平台：0-非标准平台、1-标准平台
    private Boolean isStation; //是否站内： 1 true 是 0 false 否
}
