package com.qinfei.qferp.mapper.performance;

import com.qinfei.qferp.entity.performance.PerformanceProportion;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 考核计划数据库操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0025 15:07；
 */
public interface PerformanceProportionMapper {
    /**
     * 根据主键删除单条记录；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int deleteByPrimaryKey(PerformanceProportion record);

    /**
     * 插入单条记录，全字段插入不做检查；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int insert(PerformanceProportion record);

    /**
     * 插入单条记录，插入前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int insertSelective(PerformanceProportion record);
    /**
     * 更新单条记录，更新前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKeySelective(PerformanceProportion record);
    /**
     * 更新单条记录，全字段插入不做检查；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKey(PerformanceProportion record);
    /**
     * 根据主键查询单条记录；
     *
     * @param proId：主键ID；
     * @return ：查询结果的封装对象；
     */
    PerformanceProportion selectByPrimaryKey(Integer proId);
    //判断绩效计划是否可用
    PerformanceProportion getByProId(Integer proId);
    //判断绩效方案计划名称是否重复
    PerformanceProportion findProportionByCondition(Map map);
    //伪删除
    int deleteById(Integer proportionId);
    //获取所有绩效考核计划
    List<PerformanceProportion> getList(@Param("type")Integer type,@Param("companyCode")String companyCode);
    //根据id更新启用状态
    int updateById(@Param("proportionId") Integer proportionId,@Param("proUsed") Integer proUsed);
    //根据条件获取所有的数据
    List<PerformanceProportion> getProportionList(Map<String, Object> params);
    //获取所有考核计划
    List<PerformanceProportion> getAllProportion(String companyCode);
    //获取当天的所有数据
    List<PerformanceProportion> getTodayData();
    //获取已开始为结束的考核计划
    List<PerformanceProportion> getData();
}