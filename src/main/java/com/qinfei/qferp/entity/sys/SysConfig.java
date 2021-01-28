package com.qinfei.qferp.entity.sys;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @CalssName: SysConfig
 * @Description: 系统配置参数
 * @Author: Xuxiong
 * @Date: 2020/03/13 0020 15:54
 * @Version: 1.0
 */
@Table(name = "t_sys_config")
@Data
public class SysConfig implements Serializable {
    @Id
    private Integer id;//主键ID
    private String configTitle;//功能名称
    private String configType;//配置类型：company_code-公司代码、user_id-用户ID、dept_id-部门ID、dept_type-部门类型、dept_code-部门编码、role_type-角色类型、role_code-角色编码、media_plate-媒体板块、media_parent_plate-媒体父板块、other-其他
    private String dataType;//数据类型：string、list、json、int、float、double、date
    private String configKey;//配置项键，有效配置参数唯一区分
    private String configPattern;//数据规则：数据分割符(,)/日期格式(yyyy-MM-dd HH:mm:ss)，对应数据类型为List或Date时使用
    private String configValue;//配置项值
    private String configDesc; //配置项描述，描述配置作用
    private Integer createId;//创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//创建时间
    private Integer updateId;//更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;//更新时间
    private Byte state;//状态：0-启用、1-禁用、-9-删除

    @Transient
    private User user;
}
