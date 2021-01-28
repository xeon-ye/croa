package com.qinfei.qferp.entity.propose;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 建议提示实体类
 *
 * @author tsf
 * @since 2020-07-07 14:55:45
 */
@Table(name = "t_propose_tips")
@Data
public class ProposeTips implements Serializable {
    @Id
    private Integer id;//提示id
    private Integer type;//1提示内容，2制度连接
    private Integer state;//状态0正常，-9删除
    private String content;//提示内容
    private String documentId;//制度id
    private Integer createId;//创建人id
    private String createName;//创建人姓名
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;//创建时间
    private Integer updateUserId;//修改人id
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;//修改时间
    private String remark;
    private String companyCode;//公司代码
    @Transient
    private String documentName;
}