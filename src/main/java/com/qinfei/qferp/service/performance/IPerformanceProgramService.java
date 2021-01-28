package com.qinfei.qferp.service.performance;

import java.util.List;

import com.qinfei.qferp.entity.performance.PerformanceProgram;

/**
 * 绩效考核计划关联的考核方案业务接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/16 0028 22:03；
 */
public interface IPerformanceProgramService {
	/**
	 * 查询考核计划关联的考核方案关系映射；
	 * 
	 * @param proId：考核计划ID；
	 * @return ：查询结果集合；
	 */
	List<PerformanceProgram> selectByProId(int proId);
}