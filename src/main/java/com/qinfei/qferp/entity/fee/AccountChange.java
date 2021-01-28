package com.qinfei.qferp.entity.fee;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 供应商账户异动表(FeeAccountChange)实体类
 *
 * @author xuxiong
 * @since 2020-08-05 17:17:16
 */
@Table(name = "fee_account_change")
@Data
public class AccountChange implements Serializable {
    @Id
    private Object id;//主键
    private Integer accountId;//账户ID
    private String accountName;//账号名称
    private String accountOwner;//开户人
    private Integer userId;//更新人：本次异常修改人员ID
    private String userName;//更新人：本次异常修改人员名称
    private String changeContent;//异动内容，缓存json数据，数据格式：参考数据库
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//异常时间
    private Byte state;//状态：0-有效、1-无效、-9删除
}