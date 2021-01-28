package com.qinfei.qferp.entity.sys;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;

/**
 * @CalssName: VersionHintRelate
 * @Description: 系统版本提示关联
 * @Author: Xuxiong
 * @Date: 2020/1/21 0021 8:36
 * @Version: 1.0
 */
@Table(name = "t_version_hint_relate")
@Data
public class VersionHintRelate implements Serializable {
    @Id
    private Integer id; //主键
    private Integer hintId; //提示部门ID
    private Integer userId; //提示用户ID，提示部门下的用户ID，当调岗后，可能导致用户实际部门ID和上面不一致
    private Byte readFlag; //是否阅读：0-否，1-是
    private Byte state; //状态：0-正常、-9-删除
}
