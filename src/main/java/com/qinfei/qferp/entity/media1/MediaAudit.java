package com.qinfei.qferp.entity.media1;


import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.media.Supplier;
import com.qinfei.qferp.entity.sys.District;
import com.qinfei.qferp.entity.sys.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaAudit
 * @Description 媒体审核表
 * @Author xuxiong
 * @Date 2019/6/25 0025 18:25
 * @Version 1.0
 */
@Table(name = "t_media_audit")
@Getter
@Setter
@ToString
public class MediaAudit implements Serializable {
    @Id
    private Integer id; //主键ID

    private String name; //媒体名称

    private String no; //媒体账号ID,方便后续数据抓取

    private String link; //媒体链接

    private String mediaContentId; //媒体内容ID，针对含有ID的媒体，使用ID不使用link，例如：微信、快手、抖音等板块

    private Integer commStart; //评分星级

    private String picPath; //媒体图片

    private String dataSource; //数据来源：0-手工导入、1-自动爬取

    private BigDecimal price; //媒体价格 底价

    private String profitRate; //利润率

    private BigDecimal outPrice; //媒体对外价格=媒体价格*利润率

    private Integer plateId; //媒体板块

    private Integer regionId; //地区

    private Integer typeId; //媒体类型

    private String contact; //联系方式

    private String contactor; //联系人

    private String phone; //联系电话

    private Integer creatorId; //创建人

    @Transient
    private String creatorName;//创建人名称

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间

    private Integer updatedId; //更新人

    private Integer userId; //责任人

    private Integer discount; //折扣率

    private Integer supplierId; //供应商ID

    private String supplierName; //供应商名称

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间

    private String remarks; //备注

    private Integer state; //媒体审核状态 0 待审核 1 已审核通过

    private Integer enabled; //是否可用 0-可用、1-停用

    private Integer isCopy; //是否拷贝媒体 0-自建、1-拷贝

    private String copyRemarks; //拷贝媒体备注

    private String companyCode; //公司代码:祥和XH，华越HY，波动BD，第一事业部DY

    private String companyCodeName; //公司代码名称

    private Integer isDelete; //逻辑删除 0 false 正常 ,1 true 删除

    private Integer versions; //版本号

    @Transient
    private List<MediaExtendAudit> mediaExtends; //媒体扩展表单数据
    @Transient
    private Supplier supplier; //单个供应商，供应商关系拷贝时创建
    @Transient
    private List<Supplier> suppliers; //媒体供应商列表
    @Transient
    private List<MediaSupplierPriceExtend> supplierList; //媒体对应供应商列表，媒体登记多个供应商时使用
    @Transient
    private List<Integer> deleteSupplierRelateIds; // 删除的供应商关系ID列表
    @Transient
    private User user; //媒体责任人
    @Transient
    private boolean auditsFlag; //是否需要审核标识，在媒体修改中使用
    @Transient
    private List<MediaPriceCell> mediaPriceCellList; //媒体一个供应商字段，供应商关系审核管理使用
    @Transient
    private Integer relateId;
    @Transient
    private Map<String, Integer> ftRecord; //媒体复投数据
    @Transient
    private Integer standarPlatformFlag;//是否标准平台：0-非标准平台、1-标准平台

}
