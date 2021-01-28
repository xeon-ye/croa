package com.qinfei.qferp.entity.sys;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName UserGroup
 * @Description 工作日表
 * @Author xuxiong
 * @Date 2019/10/15 11:06
 * @Version 1.0
 */
@Table(name = "t_work_date")
@Getter
@Setter
@ToString
public class WorkDate implements Serializable {
    private Integer id; //主键ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date workDate; //日期
    private Integer dateType; //日期类型： 0-工作日、1-休息日、2-法定节假日
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer createId; //创建人
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //修改时间
    private Integer updateId; //修改人
    private String companyCode; //公司代码:XH-祥和，HY-华越，BD-波动，DY-第一事业部，JT-集团，每个公司自己设置
    private String remarks; //备注
    private Integer state; //状态：0-正常、-9-删除

    @Transient
    private String startDate; //开始时间
    @Transient
    private String endDate; //结束时间
}
