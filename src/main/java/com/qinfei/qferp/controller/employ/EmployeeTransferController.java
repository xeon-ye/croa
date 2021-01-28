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
import com.qinfei.qferp.service.employ.IEmployeeTransferService;
import com.github.pagehelper.PageInfo;

import io.swagger.annotations.ApiOperation;

/**
 * 员工调岗管理；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/22 0022 13:48；
 */
@Controller
@RequestMapping(value = "/employeeTransfer")
class EmployeeTransferController {
	// 调岗管理业务接口；
	@Autowired
	private IEmployeeTransferService transferService;

	/**
	 * 分页查询员工调岗信息；
	 *
	 * @param params：查询参数集合；
	 * @param pageable：分页信息对象；
	 * @return ：员工调岗信息；
	 */
	@RequestMapping("getPageTransfer")
	@ApiOperation(value = "查询员工调岗信息", notes = "分页查询员工调岗信息")
//	@Log(opType = OperateType.QUERY, module = "员工管理", note = "分页查询员工调岗信息")
	@Verify(code = "/employeeTransfer/getPageTransfer", module = "员工调岗/调岗记录", action = "4")
	@ResponseBody
	public PageInfo<Map<String, Object>> getPageTransfer(@RequestParam Map<String, Object> params, @PageableDefault() Pageable pageable) {
		return transferService.selectPageTransfer(params, pageable);
	}
}