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
 * @CalssName DeptZw
 * @Description 部门政委表
 * @Author xuxiong
 * @Date 2019/12/19 0019 11:43
 * @Version 1.0
 */
@Table(name = "t_dept_zw")
@Data
public class DeptZw implements Serializable {
    @Id
    private Integer id; //主键
    private Integer deptId; //部门ID
    private Integer userId; //政委用户ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer createId; //创建者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer updateId; //更新人
    private Integer state; //状态：0-正常、-9-删除

    @Transient
    private Boolean deptAsync; //是否部门政委同步
    @Transient
    private List<Integer> addUserList; //新绑定的政委
    @Transient
    private List<Integer> delUserList; //删除已绑定的政委
}
