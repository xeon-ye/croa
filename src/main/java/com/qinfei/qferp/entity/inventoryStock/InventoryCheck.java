package com.qinfei.qferp.entity.inventoryStock;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * (SavesCheck)实体类
 *
 * @author tsf
 * @since 2020-06-05 11:09:35
 */
@Table(name = "t_saves_check")
@Data
public class InventoryCheck implements Serializable {
        @Id
        private Integer id;//盘点id
        private String code;//盘点编号
        private Integer state;//状态：0保存，提交
        private String title;//盘点标题
        private Integer wareId;//仓库id
        private Integer createId;//盘点人id
        private String createName;//盘点人姓名
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date createTime;//创建时间
        private String remark;//备注
        private Integer updateUserId;//修改人id
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date updateTime;//修改时间
        private String affixName;//附件名称
        private String affixLink;//附件链接
        private String companyCode;//公司代码
        @Transient
        private List<InventoryCheckDetails> details;

        @Override
        public String toString() {
            return "SavesCheck{" +
                    "id=" + id +
                    ", code='" + code + '\'' +
                    ", state=" + state +
                    ", title='" + title + '\'' +
                    ", wareId=" + wareId +
                    ", createId=" + createId +
                    ", createName='" + createName + '\'' +
                    ", createTime=" + createTime +
                    ", remark='" + remark + '\'' +
                    ", updateUserId=" + updateUserId +
                    ", updateTime=" + updateTime +
                    ", affixName='" + affixName + '\'' +
                    ", affixLink='" + affixLink + '\'' +
                    ", companyCode='" + companyCode + '\'' +
                    '}';
        }
}