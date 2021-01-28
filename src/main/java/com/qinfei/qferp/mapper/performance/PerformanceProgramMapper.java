package com.qinfei.qferp.mapper.performance;

import com.qinfei.qferp.entity.performance.PerformanceProgram;

import java.util.List;

/**
 * 考核计划关联的考核方案数据库操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0025 15:07；
 */
public interface PerformanceProgramMapper {
    /**
     * 根据主键删除单条记录；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int deleteByPrimaryKey(PerformanceProgram record);

    /**
     * 插入单条记录，全字段插入不做检查；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int insert(PerformanceProgram record);

    /**
     * 插入单条记录，插入前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int insertSelective(PerformanceProgram record);

    /**
     * 更新单条记录，更新前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKeySelective(PerformanceProgram record);

    /**
     * 更新单条记录，全字段插入不做检查；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKey(PerformanceProgram record);

    /**
     * 根据主键查询单条记录；
     *
     * @param graId：主键ID；
     * @return ：查询结果的封装对象；
     */
    PerformanceProgram selectByPrimaryKey(Integer graId);

    //通过计划id获取计划与方案的关系
    List<PerformanceProgram> selectByProId(Integer proId);

    //通过计划id进行伪删除
    int deleteByProId(Integer proId);

}