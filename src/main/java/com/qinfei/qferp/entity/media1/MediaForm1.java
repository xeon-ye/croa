package com.qinfei.qferp.entity.media1;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaForm1
 * @Description  媒体表单
 * @Author xuxiong
 * @Date 2019/6/25 0025 18:25
 * @Version 1.0
 */
@Table(name = "t_media_form1")
@Setter
@Getter
@ToString
public class MediaForm1 implements Serializable {
    private Integer id; //主键
    private String cellCode; //列名称
    private String cellName; //列描述
    private String dataType; //数据类型
    private String dbHtml; //页面显示内容
    private String dbJson; //JSON取值
    private String dbSql; //SQL取值
    private Integer disabled; //是否禁用
    private String fieldName; //字段名称 对应java类属性
    private Integer max; //字段最小值
    private Integer maxlength; //字段最大长度
    private Integer min; //字段最大值
    private Integer minlength; //字段最小长度
    private String remark; //描述
    private Integer required; //是否必填
    private String rule; //规则
    private Integer size; //字段文本长度
    private Integer sortNo; //排序
    private String type; //显示类型 radio checkbox select textarea input image file number price date datetime
    private Integer mediaPlateId; //媒体板块
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer isDelete; //逻辑删除 0 false 正常 ,1 true 删除
    private Integer versions; //版本号
    private Integer extendFlag; //扩展字段标识：0-仅媒体用、1-仅供应商用，对于媒体价格字段，默认供应商也可使用
    private Integer climbFlag; //是否爬取标识：0-仅手工填写、1-仅脚本爬取、2-手工+爬取

    @Transient
    private List<Map<String,Object>> datas; //db_sql字段执行后的数据
    @Transient
    private Map<String,String> cellValueMap; //db_sql、db_json字段执行后的数据
}
