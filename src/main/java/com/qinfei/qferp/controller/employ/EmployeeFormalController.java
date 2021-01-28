package com.qinfei.qferp.controller.employ;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qinfei.core.annotation.Verify;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.service.employ.IEmployeeFormalService;
import com.github.pagehelper.PageInfo;

import io.swagger.annotations.ApiOperation;

/**
 * 员工转正管理；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/22 0022 13:44；
 */
@Controller
@RequestMapping(value = "/employeeFormal")
class EmployeeFormalController {
	// 员工转正业务接口；
	@Autowired
	private IEmployeeFormalService formalService;

	/**
	 * 分页查询员工转正信息；
	 *
	 * @param params：查询参数集合；
	 * @param pageable：分页信息对象；
	 * @return ：员工转正信息；
	 */
	@RequestMapping("getPageFormal")
	@ApiOperation(value = "查询员工转正信息", notes = "分页查询员工转正信息")
//	@Log(opType = OperateType.QUERY, module = "员工管理", note = "分页查询员工转正信息")
	@Verify(code = "/employeeFormal/getPageFormal", module = "员工转正/转正记录", action = "4")
	@ResponseBody
	public PageInfo<Map<String, Object>> getPageFormal(@RequestParam Map<String, Object> params, @PageableDefault() Pageable pageable) {
		return formalService.selectPageFormal(params, pageable);
	}
}