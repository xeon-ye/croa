package com.qinfei.qferp.controller.performance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.performance.PerformancePlate;
import com.qinfei.qferp.service.performance.IPerformancePlateService;

import io.swagger.annotations.ApiOperation;

/**
 * 考核细则管理；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/10 0022 13:44；
 */
@Controller
@RequestMapping(value = "/performancePlate")
class PerformancePlateController {
	// 考核细则业务接口；
	@Autowired
	private IPerformancePlateService performancePlateService;

	/**
	 * 保存或更新考核细则；
	 *
	 * @param plate：提交的考核细则对象；
	 * @return ：处理结果；
	 */
	@RequestMapping("savePlate")
	@ApiOperation(value = "保存或更新考核细则", notes = "保存或更新考核细则")
//	@Log(opType = OperateType.ADD, module = "考核细则管理", note = "保存或更新考核细则")
	@Verify(code = "/performancePlate/savePlate", module = "考核细则管理/保存或更新考核细则", action = "1")
	@ResponseBody
	public ResponseData savePlate(PerformancePlate plate) {
		ResponseData data = ResponseData.ok();
		performancePlateService.saveOrUpdate(plate);
		data.putDataValue("plate", plate);
		return data;
	}

	/**
	 * 删除考核细则数据；
	 *
	 * @param plateId：考核细则ID；
	 * @return ：处理结果；
	 */
	@RequestMapping("deletePlate")
	@ApiOperation(value = "更新考核细则信息", notes = "删除考核细则数据")
//	@Log(opType = OperateType.UPDATE, module = "考核细则管理", note = "删除考核细则数据")
	@Verify(code = "/performancePlate/deletePlate", module = "考核细则管理/删除考核细则", action = "2")
	@ResponseBody
	public ResponseData deletePlate(@RequestParam Integer plateId) {
		ResponseData data = ResponseData.ok();
		performancePlateService.deletePlate(plateId);
		data.putDataValue("message", "操作完成。");
		return data;
	}

	/**
	 * 根据父级ID查询下属的考核细则分数统计；
	 *
	 * @param plateId：考核细则ID；
	 * @param plateParent：父级ID；
	 * @return ：考核细则分数统计；
	 */
	@RequestMapping("getTotal")
	@ApiOperation(value = "查询考核细则分数统计", notes = "根据父级ID查询下属的考核细则分数统计")
//	@Log(opType = OperateType.QUERY, module = "考核细则管理", note = "根据父级ID查询下属的考核细则分数统计")
	@Verify(code = "/performancePlate/getTotal", module = "员工管理/查询考核细则分数统计", action = "4")
	@ResponseBody
	public ResponseData getTotal(Integer plateId, @RequestParam Integer plateParent) {
		ResponseData data = ResponseData.ok();
		data.putDataValue("total", performancePlateService.selectTotalByParentId(plateId, plateParent));
		return data;
	}

	/**
	 * 根据父级ID查询下属的考核细则；
	 *
	 * @param plateId：主键ID；
	 * @param plateParent：父级ID；
	 * @param plateLevel：考核细则层级；
	 * @param plateContent：考核细则内容；
	 * @return ：考核细则集合；
	 */
	@RequestMapping("listPlates")
	@ApiOperation(value = "查询考核细则", notes = "根据父级ID查询下属的考核细则")
//	@Log(opType = OperateType.QUERY, module = "考核细则管理", note = "根据父级ID查询下属的考核细则")
	@Verify(code = "/performancePlate/listPlates", module = "员工管理/查询考核细则", action = "4")
	@ResponseBody
	public ResponseData listPlates(Integer plateId, Integer plateParent, Integer plateLevel, String plateContent) {
		ResponseData data = ResponseData.ok();
		data.putDataValue("plates", performancePlateService.listPlateByParentId(plateId, plateParent, plateLevel, plateContent));
		return data;
	}

	/**
	 * 查询拥有完整子节点的第一级考核板块；
	 *
	 * @param plateLevel：考核细则层级；
	 * @return ：考核细则集合；
	 */
	@RequestMapping("listCompletePlates")
	@ApiOperation(value = "查询考核细则", notes = "根据父级ID查询下属的考核细则")
//	@Log(opType = OperateType.QUERY, module = "考核细则管理", note = "根据父级ID查询下属的考核细则")
	@ResponseBody
	public ResponseData listCompletePlates(@RequestParam Integer plateLevel) {
		ResponseData data = ResponseData.ok();
		data.putDataValue("plates", performancePlateService.listCompletePlates(plateLevel));
		return data;
	}

	/**
	 * 查询当前plate所有子节点集合；
	 *
	 * @return ：考核细则集合；
	 */
	@RequestMapping("child")
	@ApiOperation(value = "查询当前细则所有子节点集合", notes = "查询当前细则所有子节点集合")
//	@Log(opType = OperateType.QUERY, module = "考核细则管理", note = "查询当前细则所有子节点集合")
	@ResponseBody
	public ResponseData child(@RequestParam Integer plateId) {
		ResponseData data = ResponseData.ok();
		data.putDataValue("plates", performancePlateService.listChildByParentId(plateId));
		data.putDataValue("parent", performancePlateService.selectByPrimaryKey(plateId));
		return data;
	}
}