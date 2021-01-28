package com.qinfei.qferp.mapper.performance;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.qinfei.qferp.entity.performance.PerformanceHistory;

/**
 * 方案关联的考核细则数据库操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0025 15:07；
 */
public interface PerformanceHistoryMapper {

	@Update("update e_performance_history set state = -1 where sch_id = #{schId}")
	void delSchmeById(@Param("schId") Integer schId);

	@Select("select DISTINCT sch_id from e_performance_history where state <> -1 and dept_id in (select id from sys_dept where company_code = #{companyCode} and state <> -1)")
	List<Integer> selectSchId(@Param("companyCode") String companyCode);

	@Select("select * from e_performance_history where plate_parent=#{parentId} and sch_id = #{schId}")
	List<PerformanceHistory> listPlateBySchIdAndParentId(@Param("schId") Integer schId, @Param("parentId") Integer parentId);

	@Select("select * from e_performance_history where plate_id=#{plateId} " + "and sch_id = #{schId} and plate_level = #{plateLevel}")
	PerformanceHistory listPlateByPlateIdAndSchId(@Param("schId") Integer schId, @Param("plateId") Integer plateId, @Param("plateLevel") Integer plateLevel);

	@Select("select * from e_performance_history where sch_id=#{schId} and state <> -1")
	List<PerformanceHistory> selectBySchId(@Param("schId") Integer schId);

	@Select("select * from e_performance_history where sch_id=#{schId} and plate_level=#{plateLv} and state <> -1")
	List<PerformanceHistory> selectBySchIdAndPlateLv(@Param("schId") Integer schId, @Param("plateLv") Integer plateLv);

	@Delete("delete from e_performance_history where sch_id = #{schId}")
	int deleteBySchId(@Param("schId") Integer schId);

	int insertAll(List<PerformanceHistory> historyList);

	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(PerformanceHistory record);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(PerformanceHistory record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(PerformanceHistory record);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(PerformanceHistory record);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(PerformanceHistory record);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param plateId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	PerformanceHistory selectByPrimaryKey(Integer plateId);
}