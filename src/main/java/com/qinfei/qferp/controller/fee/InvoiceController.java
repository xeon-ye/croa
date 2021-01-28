package com.qinfei.qferp.controller.fee;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.serivce.IDictService;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.fee.Invoice;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.fee.IInvoiceService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/invoice")
@Api(description = "开票流水接口")
class InvoiceController {

	@Autowired
	private IInvoiceService invoiceService;
	@Autowired
	private IDictService dictService;

	@RequestMapping("/listPg")
	@ResponseBody
	public PageInfo<Map> listPg(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
		PageInfo<Map> list = null;
		try {
			User user = AppUtil.getUser();
			List<Role> roles = user.getRoles();
			if (roles == null || roles.size() == 0) {
				throw new Exception("未查询到角色信息");
			} else {
				map.put("roleType", roles.get(0).getType());
				map.put("roleCode", roles.get(0).getCode());
				map.put("user", user);
				list = invoiceService.listPg(pageable.getPageNumber(), pageable.getPageSize(), map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@RequestMapping(value = "/view")
	@ResponseBody
	public ResponseData view(@RequestParam("id") Integer id) {
		try {
			ResponseData data = ResponseData.ok();
			Invoice entity = invoiceService.getById(id);
			if(entity==null){
				return ResponseData.customerError(1002, "该开票流程已删除！");
			}
			List<Dict> taxes = dictService.listDict("tax");
			//财务没有和税种部门关联，导致查看页面的抬头取不到数据，添加一个查看全部税种的方法
			List<Dict> taxesAll = dictService.listByTypeCodeAndCompanyCode("tax",AppUtil.getUser().getCompanyCode());
			entity.setPhone(entity.getPhone());
			data.putDataValue("entity", entity);
			data.putDataValue("taxes", taxes);
			data.putDataValue("taxesAll", taxesAll);
			//统计信息
			Map map = invoiceService.querySumAmount(id);
			data.putDataValue("saleSum", map.get("saleSum") == null ? 0 : map.get("saleSum"));
			data.putDataValue("incomeSum", map.get("incomeSum") == null ? 0 : map.get("incomeSum"));
			data.putDataValue("paySum", map.get("paySum") == null ? 0 : map.get("paySum"));
			data.putDataValue("outgoSum", map.get("outgoSum") == null ? 0 : map.get("outgoSum"));
			data.putDataValue("taxSum", map.get("taxSum") == null ? 0 : map.get("taxSum"));
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1001, e.getMessage());
		}
	}

	@RequestMapping(value = "/del")
	@ResponseBody
	@Log(opType = OperateType.DELETE, module = "财务管理|发票管理", note = "删除发票")
	@Verify(code = "/invoice/del", module = "财务管理/删除发票")
	public ResponseData del(@RequestParam("id") Integer id) {
		try {
			Invoice entity = invoiceService.getById(id);
			if(entity==null){
				return ResponseData.customerError(1001, "该流程已删除，请刷新后重试！");
			}
			//state=0||state=1才能删除
			if (entity.getState() == IConst.STATE_REJECT || entity.getState() == IConst.STATE_SAVE) {
				invoiceService.delById(entity);
				ResponseData data = ResponseData.ok();
				data.putDataValue("message", "操作成功");
				return data;
			} else {
				return ResponseData.customerError(1002, "当前状态不支持删除！");
			}
		} catch (QinFeiException e) {
//			e.printStackTrace();
			return ResponseData.customerError(e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1001, e.getMessage());
		}
	}

	/**
	 * 暂不启用 ，saveStepOne替代
	 *
	 * @param entity
	 * @return
	 */
	@RequestMapping("/add")
	@ResponseBody
//	@Log(opType = OperateType.ADD, module = "财务管理|发票管理", note = "新增发票")
	@Verify(code = "/invoice/add", module = "财务管理/新增发票")
	public ResponseData add(Invoice entity) {
		try {
			User user = AppUtil.getUser();
			entity.setCreator(user.getId());
			invoiceService.add(entity);
			ResponseData data = ResponseData.ok();
			data.putDataValue("message", "操作成功");
			data.putDataValue("entity", entity);

			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1001, e.getMessage());
		}

	}

	@RequestMapping("/edit")
	@ResponseBody
	@Log(opType = OperateType.UPDATE, module = "财务管理|发票管理", note = "修改发票")
	@Verify(code = "/invoice/edit", module = "财务管理/修改发票")
	public ResponseData edit(Invoice entity) {
		try {
			Invoice old = invoiceService.getById(entity.getId());
			//state=0||state=1才能编辑
			if (old.getState() == IConst.STATE_SAVE || old.getState() == IConst.STATE_REJECT) {
				ResponseData data = ResponseData.ok();
				entity.setItemId(old.getItemId());
				invoiceService.edit(entity,old.getInvoiceAmount());
				data.putDataValue("message", "操作成功");
				data.putDataValue("entity", entity);
				return data;
			} else {
				return ResponseData.customerError(1002, "当前状态不支持修改");
			}
		} catch (QinFeiException e) {
//			e.printStackTrace();
			return ResponseData.customerError(e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1001, e.getMessage());
		}

	}

	@RequestMapping("/confirm")
	@ResponseBody
//	@Log(opType = OperateType.UPDATE, module = "财务管理|发票管理", note = "确认开票")
	@Verify(code = "/invoice/confirm", module = "财务管理/确认开票")
	public ResponseData confirm(@RequestParam("id") Integer id) {
		try {
			User user = AppUtil.getUser();
			Boolean flag = false;

			if (user.getRoles() != null && user.getRoles().size() > 0) {
				for (Role role : user.getRoles()) { //会计或部长部长确认
					if (IConst.ROLE_TYPE_CW.equals(role.getType()) && (IConst.ROLE_CODE_KJ.equals(role.getCode()) || IConst.ROLE_CODE_BZ.equals(role.getCode()))) {
						flag = true;
					}
				}
			}
			if (flag) {
				ResponseData data = ResponseData.ok();
				Invoice entity = invoiceService.getById(id);
				if (entity != null && entity.getState() == IConst.STATE_PASS) {
					invoiceService.confirm(entity);
				} else {
					return ResponseData.customerError(1002, "当前状态不支持该操作！");
				}
				data.putDataValue("message", "操作成功");
				data.putDataValue("entity", entity);
				return data;
			} else {
				return ResponseData.customerError(1002, "会计或财务部长才能确认，当前用户没有操作权限！");
			}
		}catch (QinFeiException e){
//			e.printStackTrace();
			return ResponseData.customerError(e.getCode(),e.getMessage()) ;
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1001, e.getMessage());
		}

	}

	@RequestMapping("/invoice")
	@ResponseBody
//	@Log(opType = OperateType.UPDATE, module = "财务管理|发票管理", note = "财务助理开票")
	@Verify(code = "/invoice/invoice", module = "财务管理/财务助理开票")
	public ResponseData invoice(@RequestParam Map map) {
		try {
			User user = AppUtil.getUser();
			Boolean flag = false;

			if (user.getRoles() != null && user.getRoles().size() > 0) {
				for (Role role : user.getRoles()) { //会计或部长部长确认
					if (IConst.ROLE_TYPE_CW.equals(role.getType()) && IConst.ROLE_CODE_ZL.equals(role.getCode())) {
						flag = true;
					}
				}
			}
			ResponseData data = ResponseData.ok();
			if (flag) {
				// 先处理invocie的发票信息
				Integer id = Integer.parseInt((String) map.get("id"));
				Double taxPoint = Double.parseDouble((String) map.get("taxPoint"));
				Double taxAmount = Double.parseDouble((String) map.get("taxAmount"));
				String no = (String) map.get("no");
				Invoice entity = invoiceService.getById(id);
				if (entity != null) {
					if (entity.getState() == IConst.STATE_CWKP) {
						entity.setTaxPoint(taxPoint);
						entity.setTaxAmount(taxAmount);
						entity.setNo(no);
						entity.setInvoiceTime(new Date());
						invoiceService.invoice(entity,map.get("desc").toString());
					} else {
						return ResponseData.customerError(1002, "当前状态不支持该操作！");
					}
				}
				data.putDataValue("message", "操作成功");
				data.putDataValue("entity", entity);
				return data;
			} else {
				return ResponseData.customerError(1002, "财务助理才能开票，当前用户没有操作权限！");
			}
		} catch (QinFeiException e) {
//			e.printStackTrace();
			return ResponseData.customerError(e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1001, e.getMessage());
		}

	}

	@ResponseBody
	@RequestMapping("/listPgForSelectedArticle")
	public PageInfo<Map> listPgForSelectedArticle(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
		if(ObjectUtils.isEmpty(map.get("id"))){
			return null;
		}else{
            return invoiceService.listPgForSelectedArticle(pageable.getPageNumber(), pageable.getPageSize(), map);
		}
	}

	@RequestMapping(value = "/listPgForSelectArticle")
	@ResponseBody
	public PageInfo<Map> listPgForSelectArticle(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
		User user = AppUtil.getUser();
		map.put("id", user.getId());
        return invoiceService.listPgForSelectArticle(pageable.getPageNumber(), pageable.getPageSize(), map);
	}

	@RequestMapping(value = "/listPgForSelectArticleSum")
	@ResponseBody
	public Map listPgForSelectArticleSum(@RequestParam Map map) {
		User user = AppUtil.getUser();
		map.put("id", user.getId());
		return invoiceService.listPgForSelectArticleSum(map);
	}

	@RequestMapping("/saveStepOne")
	@ResponseBody
//	@Log(opType = OperateType.ADD, module = "财务管理|发票管理", note = "新增发票")
	@Verify(code = "/invoice/saveStepOne", module = "财务管理/新增发票")
	public ResponseData saveStepOne(@RequestParam Map map) {
		try {
			if (ObjectUtils.isEmpty(map.get("custCompanyId"))
					|| ObjectUtils.isEmpty(map.get("custId"))
					|| ((ObjectUtils.isEmpty(map.get("articleIdsSec")) && ObjectUtils.isEmpty(map.get("checkState"))))) {
				return ResponseData.customerError(1002, "未选择客户或稿件！");
			} else {
				ResponseData data = ResponseData.ok();
				Invoice entity = invoiceService.saveStepOne(map);
				data.putDataValue("entity", entity);
				data.putDataValue("message", "操作成功");
				return data;
			}
		} catch (QinFeiException e) {
//			e.printStackTrace();
			return ResponseData.customerError(e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1001, e.getMessage());
		}
	}

	/**
	 * 根据稿件id查询出对应的发票信息
	 * @param articleId
	 * @return
	 */
	@RequestMapping("/queryInvoiceId")
	@ResponseBody
	public Integer queryInvoiceId(@RequestParam("articleId") Integer articleId) {
		return invoiceService.queryInvoiceId(articleId);
	}

	/**
	 * state=2时财务撤回，此时，稿件状态和借款状态还未变更
	 * 1、完成待办
	 * 2、增加新的待办
	 * 3、请款订单状态修改为-1
	 *
	 * @param
	 */
	@RequestMapping("/CWReject")
	@ResponseBody
//	@Log(opType = OperateType.UPDATE, module = "开票管理|财务撤回", note = "财务撤回")
	public ResponseData CWReject(@RequestParam("id") Integer id) {
		try {
			User user = AppUtil.getUser();
			Boolean flag = false;

			if (user.getRoles() != null && user.getRoles().size() > 0) {
				for (Role role : user.getRoles()) {
					if (IConst.ROLE_TYPE_CW.equals(role.getType()) && IConst.ROLE_CODE_ZL.equals(role.getCode())) {
						flag = true;
					}
				}
			}
			if (flag) {
				Invoice entity = invoiceService.getById(id);
				if (entity.getState() == IConst.STATE_PASS || entity.getState() == IConst.STATE_CWKP) {
					entity.setState(IConst.STATE_REJECT);
					invoiceService.CWReject(entity);
					ResponseData data = ResponseData.ok();
					data.putDataValue("message", "操作成功");
					data.putDataValue("entity", entity);
					return data;
				} else {
					return ResponseData.customerError(1002, "当前状态不能撤回！");
				}
			} else {
				return ResponseData.customerError(1002, "当前用户无法撤回，请联系财务助理撤回！");
			}
		}catch (QinFeiException e){
//			e.printStackTrace();
			return ResponseData.customerError(e.getCode(),e.getMessage()) ;
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1001, e.getMessage());
		}

	}

	/**
	 * 暂时不用
	 * state=1或state=12时财务撤回，需要变更以下内容
	 * 1、关联的稿件请款状态修改为请款中2
	 * 2、关联的借款还原
	 * 3、完成待办
	 * 4、增加新的待办
	 * 5、请款订单状态修改为-1
	 *
	 * @param
	 */
	@RequestMapping("/CWReturn")
	@ResponseBody
//	@Log(opType = OperateType.UPDATE, module = "开票管理|财务驳回", note = "财务驳回")
	public ResponseData CWReturn(@RequestParam("id") Integer id) {
		try {
			User user = AppUtil.getUser();
			Boolean flag = false;

			if (user.getRoles() != null && user.getRoles().size() > 0) {
				for (Role role : user.getRoles()) {
					if (IConst.ROLE_TYPE_CW.equals(role.getType()) && IConst.ROLE_CODE_ZL.equals(role.getCode())) {
						flag = true;
					}
				}
			}
			if (flag) {
				Invoice entity = invoiceService.getById(id);
				if (entity.getState() == IConst.STATE_FINISH) {
					entity.setState(IConst.STATE_REJECT);
					Boolean returnFlag = invoiceService.CWReturn(entity);
					if (returnFlag) {
						ResponseData data = ResponseData.ok();
						data.putDataValue("message", "操作成功");
						data.putDataValue("entity", entity);
						return data;
					} else {
						return ResponseData.customerError(1002, "撤回失败，请联系管理员！");
					}
				} else {
					return ResponseData.customerError(1002, "当前状态不能撤回！");
				}
			} else {
				return ResponseData.customerError(1002, "当前用户无法撤回，请联系财务助理撤回！");
			}
		}catch (QinFeiException e){
//			e.printStackTrace();
			return ResponseData.customerError(e.getCode(),e.getMessage()) ;
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1001, e.getMessage());
		}
	}

	@RequestMapping(value = "/querySumAmount")
	@ResponseBody
	public ResponseData querySumAmount(@RequestParam("id") Integer id) {
		ResponseData data = ResponseData.ok();
		Map map = invoiceService.querySumAmount(id);
		data.putDataValue("saleSum", map.get("saleSum") == null ? 0 : map.get("saleSum"));
		data.putDataValue("incomeSum", map.get("incomeSum") == null ? 0 : map.get("incomeSum"));
		data.putDataValue("paySum", map.get("paySum") == null ? 0 : map.get("paySum"));
		data.putDataValue("outgoSum", map.get("outgoSum") == null ? 0 : map.get("outgoSum"));
		return data;
	}

	@RequestMapping("exportInvoice")
	@Log(opType = OperateType.UPDATE, module = "开票管理|导出", note = "导出")
	@ResponseBody
	public void  exportInvoice (HttpServletResponse response,@RequestParam Map map){
		try{
			User user = AppUtil.getUser();
			List<Role> roles =user.getRoles();
			if(roles == null || roles.size()==0){
				throw  new Exception("未查询到角色信息");
			}else{
				map.put("roleType",roles.get(0).getType()) ;
				map.put("roleCode",roles.get(0).getCode()) ;
				map.put("user",user) ;
				response.setContentType("application/binary;charset=UTF-8");
				response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("请款导出" + DateUtils.getNowTime() + ".xls", "UTF-8"));
				OutputStream outputStream = response.getOutputStream();
				invoiceService.exportInvoice(map, outputStream);
			}
		}catch(Exception e){
			log.error("导出失败",e);
		}
	}

}