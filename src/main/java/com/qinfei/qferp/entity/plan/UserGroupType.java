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
 * @CalssName UserGroupType
 * @Description 用户群组类型
 * @Author xuxiong
 * @Date 2019/8/9 0009 17:51
 * @Version 1.0
 */
@Table(name = "t_user_group_type")
@Setter
@Getter
public class UserGroupType implements Serializable {
    private Integer id; //主键
    private String name; //群组类型名称
    private Integer state; //状态：0-正常、-9-删除
    private Integer isSysDefault; //是否系统默认：0-否、1-是，当前期号是系统默认拥有的群组类型，不支持删除
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer createId; //创建者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer updateId; //更新者ID
    private String companyCode; //公司代码
}
