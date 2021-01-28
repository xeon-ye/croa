package com.qinfei.qferp.controller.performance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.performance.PerformanceScore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.performance.PerformanceProportion;
import com.qinfei.qferp.service.performance.IPerformanceProportionService;
import com.qinfei.qferp.service.performance.IPerformanceScoreService;
import com.github.pagehelper.PageInfo;

import io.swagger.annotations.ApiOperation;

/**
 * 绩效考核计划
 */
@Controller
@RequestMapping("/proportion")
class PerformanceProportionController {
	@Autowired
	private IPerformanceProportionService proportionService;
	@Autowired
	private IPerformanceScoreService scoreService;

	/**
	 * 添加绩效考核计划
	 * 
	 * @param
	 * @return
	 */
	@RequestMapping("saveProportion")
	@ApiOperation(value = "提交绩效考核计划", notes = "保存绩效考核计划信息")
//	@Log(opType = OperateType.ADD, module = "绩效考核", note = "保存绩效考核计划信息")
	@ResponseBody
	public ResponseData saveProportion(PerformanceProportion proportion) {
		ResponseData data = null;
		data = proportionService.saveProportion(proportion);
		return data;
	}

	/**
	 * 删除绩效考核计划
	 * 
	 * @param
	 * @return
	 */
	@RequestMapping("deleteProportion")
	@ApiOperation(value = "删除绩效考核计划", notes = "删除绩效考核计划信息")
//	@Log(opType = OperateType.DELETE, module = "绩效考核", note = "删除绩效考核计划信息")
	@ResponseBody
	public ResponseData deleteProportion(Integer proId) {
		ResponseData data = null;
		data = proportionService.deleteProportion(proId);
		return data;
	}

	/**
	 * 查看绩效考核计划，默认加载全部考核计划
	 * 
	 * @param
	 * @return
	 */
	@RequestMapping("selectProportion")
	@ApiOperation(value = "查看绩效考核计划", notes = "查看绩效考核计划信息")
//	@Log(opType = OperateType.QUERY, module = "绩效考核", note = "查看绩效考核计划信息")
	@ResponseBody
	public PageInfo<PerformanceProportion> selectProportion(@RequestParam Map<String, Object> params, @PageableDefault() Pageable pageable) {
		return proportionService.selectProportion(params, pageable);
	}

	/**
	 * 更新绩效考核计划
	 * 
	 * @param
	 * @return
	 */
	@RequestMapping("updateProportion")
	@ApiOperation(value = "更新绩效考核计划", notes = "更新绩效考核计划信息")
//	@Log(opType = OperateType.UPDATE, module = "绩效考核", note = "更新绩效考核计划信息")
	@ResponseBody
	public ResponseData updateProportion(PerformanceProportion proportion) {
		ResponseData data = null;
		data = proportionService.updateProportion(proportion);
		return data;
	}

	/**
	 * 拷贝绩效考核计划
	 * 
	 * @param
	 * @return
	 */
	@RequestMapping("copyProportion")
	@ApiOperation(value = "复制绩效考核计划", notes = "复制绩效考核计划")
//	@Log(opType = OperateType.ADD, module = "绩效考核", note = "复制更新绩效考核计划")
	@ResponseBody
	public ResponseData copyProportion(PerformanceProportion proportion) {
		ResponseData data = null;
		data = proportionService.copyProportion(proportion);
		return data;
	}

	/**
	 * 更新绩效考核计划的启用状态
	 * 
	 * @param
	 * @return
	 */
	@RequestMapping("updateProportionUserState")
	@ApiOperation(value = "更新绩效考核计划启用状态", notes = "更新绩效考核计划启用状态")
//	@Log(opType = OperateType.UPDATE, module = "绩效考核", note = "更新绩效考核计划启用状态")
	@ResponseBody
	public ResponseData updateProportionUserState(Integer proId, Integer proUsed) {
		ResponseData data = null;
		data = proportionService.updateProportionUserState(proId, proUsed);
		return data;
	}

	/**
	 * 按id查询考核计划
	 * 
	 * @param proId
	 * @return
	 */
	@RequestMapping("getProportionById")
	@ApiOperation(value = "获取绩效考核计划详情", notes = "获取绩效考核计划详情")
//	@Log(opType = OperateType.QUERY, module = "绩效考核", note = "获取绩效考核计划详情")
	@ResponseBody
	public ResponseData getProportionById(Integer proId) {
		return proportionService.getProportionById(proId);
	}

	/**
	 * 按类型获取考核计划
	 * 
	 * @param type
	 * @return
	 */
	@RequestMapping("getProportionByType")
	@ApiOperation(value = "获取绩效考核计划", notes = "通过类型获取绩效考核计划")
//	@Log(opType = OperateType.QUERY, module = "绩效考核", note = "通过类型获取绩效考核计划")
	@ResponseBody
	public PageInfo<PerformanceProportion> getProportionByType(Integer type) {
		return proportionService.getProportionByType(type);
	}

	/**
	 * 获取全部考核计划
	 * 
	 * @param
	 * @return
	 */
	@RequestMapping("getAllProportion")
	@ApiOperation(value = "获取全部绩效考核计划", notes = "获取全部绩效考核计划")
//	@Log(opType = OperateType.QUERY, module = "绩效考核", note = "获取全部绩效考核计划")
	@ResponseBody
	public ResponseData getAllProportion() {
		ResponseData data = ResponseData.ok();
		data.putDataValue("pro", proportionService.getAllProportion());
		return data;
	}

	// 启动流程
	@RequestMapping("startPerformanceProcess")
	@ApiOperation(value = "启动绩效审批流程", notes = "启动绩效审批流程")
//	@Log(opType = OperateType.QUERY, module = "绩效考核", note = "启动绩效审批流程")
	@ResponseBody
	public ResponseData startPerformanceProcess(Integer proId) {
		ResponseData data = ResponseData.ok();
		String message = scoreService.startPerformanceProcess(proId);
		data.putDataValue("message",message);
		return data;
	}

	// 检查绩效考核是否可用
	@RequestMapping("checkUsed")
//	@ApiOperation(value = "启动绩效审批流程", notes = "启动绩效审批流程")
//	@Log(opType = OperateType.QUERY, module = "绩效考核", note = "启动绩效审批流程")
	@ResponseBody
	public ResponseData checkUsed(Integer proId) {
		ResponseData data = ResponseData.ok();
		PerformanceProportion proportion = proportionService.getByProId(proId);
		Map map = new HashMap();
		map.put("proId",proId);
		List<PerformanceScore> oldData = scoreService.getAllApproveData(map);
		if(!CollectionUtils.isEmpty(oldData)){
			data.putDataValue("flag",false);
			data.putDataValue("message","抱歉，该计划已经发起流程了，请新建计划或拷贝该计划再重新发起");
		}else{
			data.putDataValue("flag",true);
		}
		data.putDataValue("entity",proportion);
		return data;
	}

	// 检查绩效考核名称是否重复
	@RequestMapping("findProportionByCondition")
//	@ApiOperation(value = "启动绩效审批流程", notes = "启动绩效审批流程")
//	@Log(opType = OperateType.QUERY, module = "绩效考核", note = "启动绩效审批流程")
	@ResponseBody
	public ResponseData findProportionByCondition(@RequestParam("proId") Integer proId,@RequestParam("proName") String proName) {
		ResponseData data = ResponseData.ok();
		Map map = new HashMap();
		map.put("proId",proId);
		map.put("proName",proName);
		PerformanceProportion proportion = proportionService.findProportionByCondition(map);
		data.putDataValue("entity",proportion);
		return data;
	}

}
