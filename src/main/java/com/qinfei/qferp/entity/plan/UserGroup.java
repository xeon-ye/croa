package com.qinfei.qferp.entity.plan;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @CalssName UserGroup
 * @Description 用户群组表
 * @Author xuxiong
 * @Date 2019/8/9 0009 17:51
 * @Version 1.0
 */
@Table(name = "t_user_group")
@Setter
@Getter
public class UserGroup implements Serializable {
    private Integer id; //主键
    private String name; //群组名称
    private Integer state; //状态：0-正常、-9-删除
    private String remarks; //描述
    private Integer createId; //创建者ID
    private String companyCode; //公司代码
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer updateId; //更新者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer groupTypeId; //群组类型ID

    @Transient
    private List<String> inputUserId;
    @Transient
    private String userName;
}
