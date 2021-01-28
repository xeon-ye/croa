package com.qinfei.qferp.entity.sys;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

@Table(name = "sys_update_pwd")
@Getter
@Setter
public class SysUpdatePassword implements Serializable {

	@Id
	private Integer id;

	private Integer creator;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private String oldPassword;
	private String newPassword;





}
