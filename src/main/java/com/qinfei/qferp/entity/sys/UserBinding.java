package com.qinfei.qferp.entity.sys;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName UserBinding
 * @Description 用户绑定表
 * @Author xuxiong
 * @Date 2019/11/23 0023 10:13
 * @Version 1.0
 */
@Table(name = "t_user_binding")
@Data
public class UserBinding implements Serializable {
    @Id
    private Integer id; //主键
    private Integer userId; //绑定用户ID
    private String unionId; //关联码，唯一
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer createId; //创建者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer updateId; //更新人
    private Integer state; //状态：0-正常、-9-删除

    @Transient
    private User user;
    @Transient
    private String bindingUserName; //进行绑定输入的用户名
    @Transient
    private String bindingPassword; //进行绑定输入的用户密码
    @Transient
    private Boolean editFlag; //是否编辑
}
