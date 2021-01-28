package com.qinfei.qferp.entity.sys;

import com.qinfei.core.annotation.Column;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yanhonghao on 2019/10/8 14:36.
 */
@Table(name = "sys_user_home_modal")
@Getter
@Setter
public class UserHomeModal implements Serializable {
    @Id
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "home_modal")
    private String homeModal;
}
