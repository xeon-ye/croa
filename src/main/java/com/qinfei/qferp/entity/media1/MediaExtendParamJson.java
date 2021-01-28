package com.qinfei.qferp.entity.media1;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @CalssName MediaExtendParamJson
 * @Description 媒体搜索条件中JSON转换对象
 * @Author xuxiong
 * @Date 2019/7/4 0004 14:52
 * @Version 1.0
 */
@Getter
@Setter
public class MediaExtendParamJson implements Serializable {
    private String cell; //t_media_extend中的cell字段
    private String type; //t_media_extend中的type字段
    private String cellValue; //t_media_extend中的cell_value字段
    private BigDecimal cellValueStart; //t_media_extend中的cell_value字段
    private BigDecimal cellValueEnd; //t_media_extend中的cell_value字段
    private Integer wechatIdFlag; //是否微信ID字段: 1-微信ID、其它则不是
    private Integer ksIdFlag; //是否快手ID字段: 1-快手ID、其它则不是

    @Override
    public String toString() {
        return "MediaExtendParamJson{" +
                "cell='" + cell + '\'' +
                ", type='" + type + '\'' +
                ", cellValue='" + cellValue + '\'' +
                ", cellValueStart=" + cellValueStart +
                ", cellValueEnd=" + cellValueEnd +
                ", wechatIdFlag=" + wechatIdFlag +
                ", ksIdFlag=" + ksIdFlag +
                '}';
    }
}
