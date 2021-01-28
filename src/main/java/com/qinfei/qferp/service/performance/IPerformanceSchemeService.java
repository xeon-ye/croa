package com.qinfei.qferp.service.performance;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.qinfei.qferp.entity.performance.PerformanceHistory;
import com.qinfei.qferp.entity.performance.PerformanceScheme;
import com.github.pagehelper.PageInfo;

public interface IPerformanceSchemeService {

	PerformanceScheme save(PerformanceScheme scheme);

	PageInfo<PerformanceScheme> listPg(Pageable pageable, Map map);

	PerformanceScheme selectById(Integer id);

	/**
	 * 根据条件查询排除人员
	 * @param map
	 * @return
	 */
	List<Map> listUserByParam(Map map);

	/**
	 * 根据主键获取相关的考核方案信息；
	 * 
	 * @param schemeId：主键ID；
	 * @return ：考核方案数据；
	 */
	PerformanceScheme getScheme(int schemeId);

	/**
	 * 获取考核方案关联的考核细则；
	 * 
	 * @param schemeId：考核方案ID；
	 * @return ：考核细则集合；
	 */
	List<PerformanceHistory> getSchemeHistory(int schemeId);

	List<PerformanceScheme> postInfo(Integer schemeType);

	void copy(Integer schId);
}
