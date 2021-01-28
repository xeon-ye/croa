package com.qinfei.qferp.service.performance;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.performance.PerformanceProportion;
import com.github.pagehelper.PageInfo;

public interface IPerformanceProportionService {
	// 添加绩效考核计划
	ResponseData saveProportion(PerformanceProportion proportion);

	// 删除绩效考核计划（伪删除）
	ResponseData deleteProportion(Integer proportionId);

	// 查看绩效考核计划
	PageInfo<PerformanceProportion> selectProportion(Map<String, Object> params, Pageable pageable);

	// 更新绩效考核计划
	ResponseData updateProportion(PerformanceProportion proportion);

	// 更新启用状态
	ResponseData updateProportionUserState(Integer proportionId, Integer proUsed);

	// 复制绩效考核计划
	ResponseData copyProportion(PerformanceProportion proportion);

	// 通过id获取考核计划
	ResponseData getProportionById(Integer proportionId);

	// 通过类型获取考核计划
	PageInfo<PerformanceProportion> getProportionByType(Integer type);

	// 获取所有的考核计划
	List<PerformanceProportion> getAllProportion();

	/**
	 * 根据主键获取相关的考核计划信息；
	 *
	 * @param proportionId：主键ID；
	 * @return ：考核计划数据；
	 */
	PerformanceProportion getProportion(int proportionId);

	/**
	 * 判断绩效方案是否可用；
	 *
	 * @param proId：主键ID；
	 * @return ：考核计划数据；
	 */
	PerformanceProportion getByProId(int proId);

	//判断绩效方案计划名称是否重复
	PerformanceProportion findProportionByCondition(Map map);
}
