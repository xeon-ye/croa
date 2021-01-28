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
 * @CalssName: VersionHint
 * @Description: 系统版本提示表
 * @Author: Xuxiong
 * @Date: 2020/1/20 0020 15:54
 * @Version: 1.0
 */
@Table(name = "t_version_hint")
@Data
public class VersionHint implements Serializable {
    @Id
    private Integer id; //主键
    private String title; //标题
    private String deptId; //提示部门ID
    private String deptName; //提示部门名称
    private String content; //提示内容
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer createId; //创建者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer updateId; //更新人
    private Byte state; //状态：0-正常、-9-删除
    private String companyCode; //公司代码

    @Transient
    private User user; //创建用户信息
    @Transient
    private List<Integer> deptIds; //部门ID集合，前台多选
}
