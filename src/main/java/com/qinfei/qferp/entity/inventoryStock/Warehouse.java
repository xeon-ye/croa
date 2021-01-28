package com.qinfei.qferp.entity.inventoryStock;

import java.util.Date;
import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 进销存仓库表(TSavesWarehouse)实体类
 *
 * @author makejava
 * @since 2020-05-08 16:32:15
 */
@Table(name = "t_saves_warehouse")
@Data
public class Warehouse implements Serializable {
        @Id
        private Integer id;//仓库id
        private String code;//仓库编号
        private String name;//仓库名称
        private String address;//地址
        private Integer state;//仓库状态
        private Integer userId;//仓库负责人id
        private Integer createId;//创建人id
        private String createName;//创建人姓名
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date createTime;//创建时间
        private Integer updateUserId;//更新用户
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date updateTime;//更新时间
        private String companyCode;//所属公司

        @Override
        public String toString() {
                return "Warehouse{" +
                        "id=" + id +
                        ", code='" + code + '\'' +
                        ", name='" + name + '\'' +
                        ", address='" + address + '\'' +
                        ", state=" + state +
                        ", userId=" + userId +
                        ", createId=" + createId +
                        ", createName='" + createName + '\'' +
                        ", createTime=" + createTime +
                        ", updateUserId=" + updateUserId +
                        ", updateTime=" + updateTime +
                        ", companyCode='" + companyCode + '\'' +
                        '}';
        }
}