package com.qinfei.qferp.controller.performance;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.performance.PerformanceProportion;
import com.qinfei.qferp.service.performance.IPerformanceProportionService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.performance.PerformanceScore;
import com.qinfei.qferp.service.performance.IPerformanceScoreService;
import com.github.pagehelper.PageInfo;

import io.swagger.annotations.ApiOperation;

/**
 * 绩效考核的评分信息；
 * 
 * @Author ：Yuan；
 * @Date ：2019/4/16 0022 18:40；
 */
@Controller
@RequestMapping(value = "/performanceScore")
class PerformanceScoreController {
	// 评分业务接口；
	@Autowired
	private IPerformanceScoreService scoreService;
	//绩效计划接口
	@Autowired
	private IPerformanceProportionService proportionService;

	/**
	 * 保存评分信息；
	 *
	 * @param scoreData：提交的评分字符串；
	 * @return ：处理结果；
	 */
	@RequestMapping("saveScore")
	@ApiOperation(value = "提交评分信息", notes = "保存评分信息")
//	@Log(opType = OperateType.ADD, module = "考核评分", note = "保存评分信息")
	@ResponseBody
	public ResponseData saveScore(@RequestParam String scoreData) {
		ResponseData data = ResponseData.ok();
		scoreService.updatePerformance(scoreData);
		data.putDataValue("message", "操作完成。");
		return data;
	}

	/**
	 * 提交考核流程；
	 *
	 * @param scoreId：主键ID；
	 * @return ：处理结果；
	 */
	@RequestMapping("startPerformance")
	@ApiOperation(value = "提交考核流程", notes = "提交考核流程")
//	@Log(opType = OperateType.UPDATE, module = "考核评分", note = "提交考核流程")
	@ResponseBody
	public ResponseData startPerformance(@RequestParam Integer scoreId) {
		ResponseData data = ResponseData.ok();
		data.putDataValue("message", scoreService.startSinglePerformanceProcess(scoreId));
		return data;
	}

	/**
	 * 分页查询绩效考核评分信息；
	 *
	 * @param params：查询参数集合；
	 * @param pageable：分页信息对象；
	 * @return ：考核评分信息；
	 */
	@RequestMapping("getPageScore")
	@ApiOperation(value = "查询绩效考核评分", notes = "分页查询绩效考核评分信息")
//	@Log(opType = OperateType.QUERY, module = "考核评分", note = "分页查询绩效考核评分信息")
	@ResponseBody
	public PageInfo<PerformanceScore> getPageScore(@RequestParam Map<String, Object> params, @PageableDefault() Pageable pageable) {
		return scoreService.selectPagePerformanceScore(params, pageable);
	}

	/**
	 * 分页查询绩效考核评分信息；
	 *
	 * @param params：查询参数集合；
	 * @return ：考核评分信息；
	 */
	@RequestMapping("getAllApproveData")
	@ResponseBody
	public ResponseData getAllApproveData(@RequestParam Map<String, Object> params) {
		try {
			ResponseData data = ResponseData.ok();
			PerformanceProportion proportion=proportionService.getProportion(Integer.valueOf(params.get("proId").toString()));
			if(ObjectUtils.isEmpty(proportion)){
				return ResponseData.customerError(1002,"抱歉，该绩效计划不存在，请刷新页面");
			}
			List<PerformanceScore> list = scoreService.getAllApproveData(params);
			if(CollectionUtils.isNotEmpty(list)){
				//已发起流程
				data.putDataValue("message",String.format("知悉：【%s】此绩效计划已经发起了流程，此流程关联了%s个待办,删除该计划则相应的待办及考核结果都会被删除。",proportion.getProName(),list.size()));
			}else {
				data.putDataValue("message","是否确定删除该计划?");
			}
			return data;
		} catch (Exception e) {
			return ResponseData.customerError(1002,"抱歉，该绩效计划不存在，请刷新页面");
		}
	}

	/**
	 * 根据考核评分ID查询评分详情信息；
	 *
	 * @param scoreId：考核评分ID；
	 * @return ：评分详情信息；
	 */
	@RequestMapping("getScoreInfo")
	@ApiOperation(value = "查询绩效考核评分", notes = "根据考核评分ID查询评分详情信息")
//	@Log(opType = OperateType.QUERY, module = "考核评分", note = "根据考核评分ID查询评分详情信息")
	@ResponseBody
	public ResponseData getScoreInfo(@RequestParam Integer scoreId) {
		ResponseData data = ResponseData.ok();
		scoreService.getScoreInfo(data, scoreId);
		return data;
	}

	/**
	 * 根据绩效考核的查询码查询员工的绩效考核信息，此接口只需登录即可访问，没有权限控制；
	 *
	 * @param code：查询码；
	 * @return ：入职申请的审核信息；
	 */
	@RequestMapping("getApproveInfo")
	@ApiOperation(value = "查询绩效考核评分", notes = "根据绩效考核的查询码查询员工的绩效考核信息")
//	@Log(opType = OperateType.QUERY, module = "考核评分", note = "根据绩效考核的查询码查询员工的绩效考核信息")
	@ResponseBody
	public ResponseData getApproveInfo(@RequestParam String code) {
		ResponseData data = ResponseData.ok();
		scoreService.setPerformanceApproveData(data, code);
		return data;
	}
}