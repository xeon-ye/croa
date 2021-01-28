package com.qinfei.qferp.entity.media1;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @CalssName MediaPrice
 * @Description 媒体价格
 * @Author xuxiong
 * @Date 2019/7/16 0016 15:10
 * @Version 1.0
 */
@Data
public class MediaPrice implements Serializable {
    private Integer mediaId; //媒体Id
    private Integer plateId; //媒体板块ID
    private Integer userId; // 责任人
    private String picPath; //媒体图标
    private Integer discount; //折扣率
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //修改日期
    private String mediaName; //媒体名称
    private String mediaContentId;//唯一标识
    private String companyCode; //媒体对应公司代码
    private Integer state; //媒体状态
    List<MediaPriceCell> mediaPriceCellList; //对应价格列表，有供应商则对应供应商价格

}
