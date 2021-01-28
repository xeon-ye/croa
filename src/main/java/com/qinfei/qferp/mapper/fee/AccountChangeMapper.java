package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.fee.AccountChange;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 供应商异动表(TSupplierChange)表数据库访问层
 *
 * @author xuxiong
 * @since 2020-08-05 17:18:13
 */
public interface AccountChangeMapper extends BaseMapper<AccountChange, Integer> {
    //新增数据
    int save(AccountChange supplierChange);

    //批量插入供应商账户异动表
    int saveBatch(List<AccountChange> accountChangeList);

    //根据账户ID获取异动列表
    List<AccountChange> listAccountChangeByParam(@Param("accountIds") List<Integer> accountIds);

    //获取指定异动信息
    AccountChange getAccountChangeById(@Param("id") int id);

}