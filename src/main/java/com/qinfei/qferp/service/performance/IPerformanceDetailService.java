package com.qinfei.qferp.service.performance;

import java.util.List;

import com.qinfei.qferp.entity.performance.PerformanceDetail;

/**
 * 绩效考核的评分明细业务接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/15 0028 17:03；
 */
public interface IPerformanceDetailService {
	/**
	 * 批量新增或更新相关的评分明细；
	 * 
	 * @param details：评分明细；
	 */
	void saveOrUpdate(List<PerformanceDetail> details);

	/**
	 * 根据主表ID获取关联的评分明细；
	 * 
	 * @param scoreId：评分ID；
	 * @return ：评分明细集合；
	 */
	List<PerformanceDetail> getDetailInfo(int scoreId);
}