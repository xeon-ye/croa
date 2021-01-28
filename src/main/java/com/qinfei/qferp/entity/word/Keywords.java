package com.qinfei.qferp.entity.word;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName keyword
 * @Description 关键字字典表
 * @Author tsf
 * @Date 2019/9/23
 */
@Table(name = "t_permission_word")
@Setter
@Getter
@ToString
public class Keywords implements Serializable {
    @Id
    private Integer id; //主键
    private String name; //关键字名称
    private Integer groupId;//功能菜单id
    private String companyCode; //公司代码
    private Integer state;//状态0有效，-9删除
    private Integer createId;//创建人
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer updateId;//修改人
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //修改时间
}
