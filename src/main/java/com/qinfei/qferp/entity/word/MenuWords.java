package com.qinfei.qferp.entity.word;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @CalssName MenuWords
 * @Description 关键字菜单表
 * @Author tsf
 * @Date 2019/9/23
 */
@Table(name = "t_permission_word_group")
@Setter
@Getter
public class MenuWords implements Serializable {
    private Integer id; //主键id
    private Integer menuId;//功能菜单编码
    private String menuName; //功能菜单名称
    private Integer permissionType; //关键字类型0允许1屏蔽
    private Integer state; //状态：0-正常、-9-删除
    private String companyCode; //公司代码
    private String remarks; //描述
    private Integer createId;//创建人
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer updateId;//修改人
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //修改时间
    @Transient
    private List<Integer> inputKeyId;
    @Transient
    private  List<String> inputKeyName;
}
