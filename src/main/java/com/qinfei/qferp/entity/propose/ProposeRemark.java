package com.qinfei.qferp.entity.propose;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import java.util.Date;

/**
 * @Description 建议类型-负责人关系表
 * @Author tsf
 * @Date 2019/9/23
 * @Version 1.0
 */
@Table(name = "t_propose_remark")
@Getter
@Setter
@ToString
public class ProposeRemark implements Serializable {
    private Integer id;  //主键id
    private Integer adviceId;  //建议类型id
    private String remark;  //处理意见
    private Integer createId;//处理人id
    private String createName;//处理人名称
    private String handleResult;//处理结果
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;  //创建时间
    private Integer updateId;//处理人id
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;  //创建时间
}

