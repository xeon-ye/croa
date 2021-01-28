package com.qinfei.qferp.entity.media1;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 媒体异动表(TMediaChange)实体类
 *
 * @author xuxiong
 * @since 2020-05-11 09:34:47
 */
@Table(name = "t_media_change")
@Data
public class MediaChange implements Serializable {
    @Id
    private Integer id;//主键
    private Integer plateId;//媒体板块
    private Integer mediaId;//媒体ID
    private String mediaName;//媒体名称，冗余处理，有些查询没必要关联表查询
    private Integer userId;//更新人：本次异常修改人员ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//异常生效时间，取媒体信息审核通过的时间
    private Integer auditUserId;//审批人：本次异常修改人员ID
    private String auditUserName;//审核人名称，冗余处理，有些查询没必要关联表查询
    private String changeContent;//异动内容，缓存json数据，数据格式：{}
    private String companyCode;//公司代码
    private Byte state;//状态：0-有效、1-无效、-9删除

    @Transient
    private String userName;//异动人姓名
}