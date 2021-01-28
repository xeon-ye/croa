package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media1.SupplierChange;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 供应商异动表(TSupplierChange)表数据库访问层
 *
 * @author xuxiong
 * @since 2020-08-05 17:18:13
 */
public interface SupplierChangeMapper extends BaseMapper<SupplierChange, Integer> {
    //新增数据
    int save(SupplierChange supplierChange);

    //批量插入供应商异动表
    int saveBatch(List<SupplierChange> supplierChangeList);

    //根据供应商ID获取异动列表
    List<SupplierChange> listSupplierChangeByParam(@Param("supplierIds") List<Integer> supplierIds);

    //获取指定异动信息
    SupplierChange getSupplierChangeById(@Param("id") int id);

}