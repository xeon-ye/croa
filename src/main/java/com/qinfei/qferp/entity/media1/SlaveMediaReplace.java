package com.qinfei.qferp.entity.media1;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;

/**
 * 稿件、媒体供应商定时调度更新记录表
 */
@Table(name = "t_slave_media_replace")
@Data
public class SlaveMediaReplace implements Serializable {
    @Id
    private Integer id;
    private String artContent;
    private String mediaRelateAuditContent;
    private String mediaRelateContent;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Data createDate;

}
