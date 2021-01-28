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
 * @Date 2019/8/27
 * @Version 1.0
 */
@Table(name = "t_propose_relation")
@Getter
@Setter
@ToString
public class ProposeRelation implements Serializable {
    private Integer id;  //建议类型id
    private Integer userId;  //负责人id
    private Integer creator;  //创建人Id
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;  //创建时间
}

