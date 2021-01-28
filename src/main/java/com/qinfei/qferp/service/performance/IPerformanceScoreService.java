package com.qinfei.qferp.service.performance;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.performance.PerformanceScore;
import com.github.pagehelper.PageInfo;

/**
 * 绩效考核的评分业务接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/14 0028 22:03；
 */
public interface IPerformanceScoreService {
	/**
	 * 更新绩效考核评分相关的数据；
	 * 
	 * @param scoreData：考核评分数据的字符串数据；
	 */
	void updatePerformance(String scoreData);

	/**
	 * 启动绩效考核流程；
	 *
	 * @param scoreId：考核评分ID；
	 * @return ：处理结果提示信息；
	 */
	String startSinglePerformanceProcess(int scoreId);

	/**
	 * 启动绩效考核流程；
	 *
	 * @param proId：考核计划ID；
	 * @return ：处理结果提示信息；
	 */
	String startPerformanceProcess(int proId);

	/**
	 * 绩效考核的流程更新状态；
	 *
	 * @param scoreId：主键ID；
	 * @param code：当前使用的查询码；
	 * @param state：当前状态；
	 * @param taskId：任务ID；
	 * @param itemId：待办事项ID；
	 */
	void processPerformance(int scoreId, String code, int state, String taskId, Integer itemId);

	/**
	 * 获取绩效考核流程所需的审核数据；
	 *
	 * @param data：返回给前端的数据；
	 * @param code：权限访问码；
	 */
	void setPerformanceApproveData(ResponseData data, String code);

	/**
	 * 获取绩效考核的详情记录；
	 * 
	 * @param scoreId：主键ID；
	 */
	void getScoreInfo(ResponseData data, int scoreId);

	/**
	 * 分页查询绩效考核评分信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的评分信息集合；
	 */
	PageInfo<PerformanceScore> selectPagePerformanceScore(Map<String, Object> params, Pageable pageable);

	/**
	 * 获取计划启动的所有考核数据
	 * @param map
	 * @return
	 */
	List<PerformanceScore> getAllApproveData(Map<String,Object> map);
}