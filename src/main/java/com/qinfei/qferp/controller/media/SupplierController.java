package com.qinfei.qferp.controller.media;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.fee.AccountChange;
import com.qinfei.qferp.entity.media.Supplier;
import com.qinfei.qferp.entity.media1.SupplierChange;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.media.ISupplierService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.DataImportUtil;
import com.qinfei.qferp.utils.EasyExcelUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/supplier")
@Api(description = "媒体供应商接口")
class SupplierController {
	@Autowired
	ISupplierService supplierService;
	@Autowired
	private Config config;

	@PostMapping("checkSupplier")
	@ResponseBody
	@ApiOperation(value = "供应商管理", notes = "校验供应商唯一性")
	public ResponseData checkSupplier(@RequestParam("id") Integer id, @RequestParam("name") String name, @RequestParam("phone") String phone){
		try{
			supplierService.checkSupplier(id, name,phone);
			return ResponseData.ok();
		}catch (QinFeiException e){
			return ResponseData.customerError(e.getCode(), e.getMessage());
		}catch (Exception e){
			return ResponseData.customerError(1002, "供应商校验异常！");
		}
	}

	@PostMapping("addSupplier")
	@ResponseBody
	@ApiOperation(value = "供应商管理", notes = "供应商登记")
	public ResponseData addSupplier(@RequestBody Supplier supplier){
		try{
			supplierService.addSupplier(supplier);
			return ResponseData.ok();
		}catch (QinFeiException e){
			return ResponseData.customerError(e.getCode(), e.getMessage());
		}catch (Exception e){
			log.error("【供应商登记】供应商编辑异常：{}", e.getMessage());
			return ResponseData.customerError(1002, "供应商登记异常！");
		}
	}

	@PostMapping("editSupplier")
	@ResponseBody
	@ApiOperation(value = "供应商管理", notes = "供应商编辑")
	public ResponseData editSupplier(@RequestBody Supplier supplier){
		try{
			supplierService.editSupplier(supplier);
			return ResponseData.ok();
		}catch (QinFeiException e){
			return ResponseData.customerError(e.getCode(), e.getMessage());
		}catch (Exception e){
			log.error("【供应商编辑】供应商编辑异常：{}", e.getMessage());
			return ResponseData.customerError(1002, "供应商联系人编辑异常！");
		}
	}

	@PostMapping("updateSupplier")
	@ResponseBody
	@ApiOperation(value = "请款/退稿管理", notes = "供应商编辑")
	public ResponseData updateSupplier(@RequestParam Map<String, Object> param){
		try{
			supplierService.updateSupplier(param);
			return ResponseData.ok();
		}catch (QinFeiException e){
			return ResponseData.customerError(e.getCode(), e.getMessage());
		}catch (Exception e){
			log.error("【请款/退稿管理】供应商编辑异常：{}", e.getMessage());
			return ResponseData.customerError(1002, "供应商编辑异常！");
		}
	}

	@PostMapping("editSupplierCompany")
	@ResponseBody
	@ApiOperation(value = "供应商管理", notes = "供应商公司编辑")
	public ResponseData editSupplierCompany(@RequestParam Map<String, Object> param){
		try{
			supplierService.editSupplierCompany(param);
			return ResponseData.ok();
		}catch (QinFeiException e){
			return ResponseData.customerError(e.getCode(), e.getMessage());
		}catch (Exception e){
			log.error("【供应商公司编辑】供应商编辑异常：{}", e.getMessage());
			return ResponseData.customerError(1002, "供应商公司名称编辑异常！");
		}
	}

	@PostMapping("supplierAccountList")
	@ResponseBody
	@ApiOperation(value = "供应商管理", notes = "供应商账号列表")
	public PageInfo<Map<String,Object>> supplierAccountList(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map<String, Object> param){
		return supplierService.supplierAccountList(pageable, param);
	}

	@PostMapping("updateSupplierUserId")
	@ApiOperation(value = "供应商管理", notes = "供应商责任人指派")
	@ResponseBody
	public ResponseData updateMediaUserId(@RequestParam("id") Integer id, @RequestParam("userId") Integer userId) {
		try{
			supplierService.updateSupplierUserId(id, userId);
			return ResponseData.ok();
		}catch (QinFeiException e){
			return ResponseData.customerError(e.getCode(), e.getMessage());
		}catch (Exception e){
			return ResponseData.customerError(1002, "供应商责任人指派异常！");
		}
	}

	@PostMapping("listSupplierChange")
	@ApiOperation(value = "供应商管理", notes = "供应商异动详情")
	@ResponseBody
	public List<SupplierChange> listSupplierChange(@RequestParam String supplierIds){
		return supplierService.listSupplierChange(supplierIds);
	}

	@PostMapping("listSupplierAccountChange")
	@ApiOperation(value = "供应商管理", notes = "供应商账号异动详情")
	@ResponseBody
	public List<AccountChange> listSupplierAccountChange(@RequestParam String accountIds){
		return supplierService.listSupplierAccountChange(accountIds);
	}


	//以下是老代码20200717以前

	@PostMapping("/listall")
	@ResponseBody
	@Log(opType = OperateType.QUERY, module = "媒体管理|供应商管理", note = "分页查询媒体供应商列表")
	@ApiOperation(value = "分页查询媒体供应商列表", notes = "分页查询媒体供应商列表", response = PageInfo.class)
	public PageInfo<Supplier> listall(@ApiParam("媒体供应商筛选条件") @RequestParam Map<String, Object> map, @ApiParam("分页对象") Pageable pageable) {
		return supplierService.listall(map, pageable);
	}

	/**
	 * 媒体供应商板块下拉框分页数据获取；
	 * 
	 * @param map：查询条件；
	 * @param pageable：分页对象；
	 * @return ：分页数据集合；
	 */
	/*@PostMapping("/listMediaSupplier")
	@ResponseBody
	@Log(opType = OperateType.QUERY, module = "媒体管理|供应商管理", note = "分页查找指定媒体板块的供应商信息")
	@ApiOperation(value = "分页查询媒体供应商列表", notes = "分页查询媒体供应商列表", response = PageInfo.class)
	public PageInfo<Supplier> listMediaSupplier(@ApiParam("媒体供应商筛选条件") @RequestParam Map<String, Object> map, @ApiParam("分页对象") Pageable pageable) {
		return supplierService.listMediaSupplier(map, pageable);
	}*/

	/**
	 * 媒体供应商板块下拉框分页数据获取；
	 *
	 * @param map：查询条件；
	 * @param pageable：分页对象；
	 * @return ：分页数据集合；
	 */
	@PostMapping("/listSupplierByPlateCompany")
	@ResponseBody
//	@Log(opType = OperateType.QUERY, module = "媒体管理|供应商管理", note = "分页查找指定媒体板块的供应商信息")
	@ApiOperation(value = "分页查询媒体供应商列表", notes = "分页查询媒体供应商列表", response = PageInfo.class)
	public PageInfo<Supplier> listSupplierByPlateCompany(@ApiParam("媒体供应商筛选条件") @RequestParam Map<String, Object> map, @ApiParam("分页对象") Pageable pageable) {
		return supplierService.listSupplierByPlateCompany(map, pageable);
	}

	/*@GetMapping("/list")
	@ResponseBody
	@Log(opType = OperateType.QUERY, module = "媒体管理|供应商管理", note = "供应商查询")
	public List<Supplier> list(Supplier supplier) {
		return supplierService.list(supplier);
	}*/

	@GetMapping("/get")
	@ResponseBody
	@Log(opType = OperateType.QUERY, module = "媒体管理|供应商管理", note = "根据id查询供应商")
	public ResponseData getById(Integer id) {
		try {
			ResponseData data = ResponseData.ok();
			data.putDataValue("message", "操作成功");
			if (id != null) {
				Supplier supplier = supplierService.getById(id);
				data.putDataValue("supplier", supplier);
			}
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1002, e.getMessage());
		}
	}

	/**
	 * 判断供应商+联系人是否重复
	 *
	 * @param supplierName
	 * @param contactor
	 * @return
	 */
	/*@PostMapping("/repeat")
	@ResponseBody
	public ResponseData repeat(String supplierName, String contactor, Integer mediaTypeId, @RequestParam(name = "phone", required = false) String phone, @RequestParam(name = "qqwechat", required = false) String qqwechat) {
		boolean repe = false;
		try {
			repe = supplierService.isRepeat(supplierName, contactor, mediaTypeId, phone, qqwechat);
			ResponseData responseData = ResponseData.ok();
			responseData.putDataValue("repeatResult", repe);
			return responseData;
		} catch (Exception e) {
			log.error("", e);
            return ResponseData.customerError(1002, e.getMessage());
		}
	}*/

	/*@RequestMapping("/add")
	@ResponseBody
	@Log(opType = OperateType.ADD, module = "媒体管理|供应商管理", note = "新增供应商")
	public ResponseData add(@RequestBody Supplier entity) {
		try {
			// 编辑和新增公用一个页面，导致页面id可能有值，以防万一，新增时删掉id
			entity.setId(null);
			ResponseData data = ResponseData.ok();
			if (supplierService.isRepeat(entity.getName(), entity.getContactor(), entity.getMediaTypeId(), entity.getPhone(), entity.getQqwechat())) {
				data.putDataValue("message", "【供应商名称+联系人】已经存在或【联系人+电话】已经存在或【联系人+QQ微信】已经存在，不能重复，请重新输入！");
			} else {
				//User user = (User) session.getAttribute(IConst.USER_KEY);
				User user= AppUtil.getUser();
				// 去除空格；
				entity.setName(entity.getName().trim());
				entity.setContactor(entity.getContactor().trim());
				if(StringUtils.isNotEmpty(entity.getPhone())){
					//供应商联系方式加密
					String phone = EncryptUtils.encrypt(entity.getPhone().trim());
					entity.setPhone(phone);
				}
				if(StringUtils.isNotEmpty(entity.getQqwechat())){
					//供应商微信QQ加密
					String qqwechat = EncryptUtils.encrypt(entity.getQqwechat().trim());
					entity.setQqwechat(qqwechat);
				}
				entity.setCreator(user.getId());
				entity.setCreateTime(new Date());
				Dept dept = user.getDept();
				entity.setCompanyCode(dept.getCompanyCode());
				supplierService.save(entity);

				data.putDataValue("message", "添加成功");
				data.putDataValue("entity", entity);
			}

			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1002, e.getMessage());
		}
	}*/

	/*@RequestMapping("/edit")
	@ResponseBody
	@Log(opType = OperateType.UPDATE, module = "媒体管理|供应商管理", note = "修改供应商")
	public ResponseData edit(@RequestBody Supplier entity) {
		try {
			ResponseData data = ResponseData.ok();
			if (supplierService.isRepeat(entity.getName(), entity.getContactor(), entity.getMediaTypeId(), entity.getPhone(), entity.getQqwechat(), entity.getId())) {

				data.putDataValue("message", "【供应商名称+联系人】已经存在或【联系人+电话】已经存在或【联系人+QQ微信】已经存在，不能重复，请重新输入！");
			} else {
				//User user = (User) session.getAttribute(IConst.USER_KEY);
				User user= AppUtil.getUser();
				// 去除空格；
				entity.setName(entity.getName().trim());
				entity.setContactor(entity.getContactor().trim());
				if(StringUtils.isNotEmpty(entity.getPhone())){
					//供应商联系方式加密
					String phone = EncryptUtils.encrypt(entity.getPhone().trim());
					entity.setPhone(phone);
				}
				if(StringUtils.isNotEmpty(entity.getQqwechat())){
					//供应商微信QQ加密
					String qqwechat = EncryptUtils.encrypt(entity.getQqwechat().trim());
					entity.setQqwechat(qqwechat);
				}
				entity.setUpdateUserId(user.getId());

				supplierService.update(entity);

				data.putDataValue("message", "操作成功");
				data.putDataValue("entity", entity);
			}
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1002, e.getMessage());
		}
	}*/

	/**
	 * 按照id设置供应商信息的状态，状态：0正常、1停用、2、注销
	 *
	 * @param id：供应商ID；
	 * @return ：操作结果；
	 */
	@RequestMapping("/del")
	@ResponseBody
	@Log(opType = OperateType.DELETE, module = "媒体管理|供应商管理", note = "删除供应商")
	public ResponseData del(@RequestParam Integer id) {
		try {
			return supplierService.delete(id);
		}catch (QinFeiException e){
			e.printStackTrace();
			return ResponseData.customerError(e.getCode(), e.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1002, e.getMessage());
		}
	}

	@GetMapping("active/{id}")
	@ApiOperation(value = "媒体供应商管理", notes = "供应商联系人启用")
	@ResponseBody
	public ResponseData active(@PathVariable("id") Integer id) {
		try{
			supplierService.active(id);
			return ResponseData.ok();
		}catch (QinFeiException byeException){
			return ResponseData.customerError(byeException.getCode(),byeException.getMessage());
		}catch (Exception e){
			e.printStackTrace();
			log.error(e.getMessage());
			return ResponseData.customerError(1002,"供应商联系人启用失败！");
		}
	}

	@GetMapping("stop/{id}")
	@ApiOperation(value = "媒体供应商管理", notes = "供应商联系人禁用")
	@ResponseBody
	public ResponseData stop(@PathVariable("id") Integer id) {
		try{
			supplierService.stop(id);
			return ResponseData.ok();
		}catch (QinFeiException byeException){
			return ResponseData.customerError(byeException.getCode(),byeException.getMessage());
		}catch (Exception e){
			e.printStackTrace();
			log.error(e.getMessage());
			return ResponseData.customerError(1002,"供应商联系人禁用失败！");
		}
	}

	@RequestMapping("/querySupplierList")
	@ResponseBody
	@Log(opType = OperateType.QUERY, module = "媒体管理|供应商管理", note = "供应商筛选")
	public PageInfo<Map> querySupplierList(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
		return supplierService.querySupplierList(pageable.getPageNumber(), pageable.getPageSize(), map);
	}

	/*@RequestMapping("/querySupplierListByType")
	@ResponseBody
	@Log(opType = OperateType.QUERY, module = "媒体管理|供应商管理", note = "供应商做分公司限制")
	public PageInfo<Map> querySupplierListByType(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
		User user = AppUtil.getUser();
		Dept dept = user.getDept();
		map.put("companyCode",dept.getCompanyCode());
		map.put("user", user);

		return supplierService.querySupplierListByType(pageable.getPageNumber(), pageable.getPageSize(), map);
	}*/

	@RequestMapping("/querySupplierListByTypeNew")
	@ResponseBody
	@Log(opType = OperateType.QUERY, module = "媒体管理|供应商管理", note = "供应商做分公司限制")
	public PageInfo<Map> querySupplierListByTypeNew(@PageableDefault(size = 10) Pageable pageable, @RequestParam Map map) {
		return supplierService.querySupplierListByTypeNew(pageable.getPageNumber(), pageable.getPageSize(), map);
	}
	/**
	 * 获取指定板块的数据导入模板；
	 *
	 * @param request：请求对象；
	 * @param response：响应对象；
	 */
	@RequestMapping("downTemplate")
	@ResponseBody
//	@Log(opType = OperateType.QUERY, note = "下载模板", module = "供应商管理/下载模板")
	public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.setContentType("application/binary;charset=UTF-8");
			String agent = request.getHeader("user-agent");
			String fileName = "供应商信息导入模板";
			response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(fileName, agent) + ".xls");
			OutputStream outputStream = response.getOutputStream();
			DataImportUtil.createExportFile(fileName, supplierService.getSupplierForms(), outputStream, null, supplierService.getSupplierNotices());
		} catch (IOException e) {
			log.error("导出模板失败", e);
		}
	}

	/**
	 * 批量导入数据；
	 *
	 * @param multipartFile：上传的文件对象；
	 * @return ：处理结果；
	 */
	@RequestMapping("importData")
	@ResponseBody
	@Log(opType = OperateType.ADD, note = "批量导入数据", module = "供应商管理/批量新增")
	public ResponseData batchAddSupplier(@RequestParam(value = "file") MultipartFile multipartFile) {
        try {
            File file = DataImportUtil.copyFile(multipartFile, config.getUploadDir(), "supplier/");
            if (file.exists()) {
                // 获取文件内容；
                List<Object[]> excelContent = EasyExcelUtil.getExcelContent(file, 1, 3, 2);
                if (CollectionUtils.isNotEmpty(excelContent)) {
                    Map<String, Object> handResult = supplierService.batchAddSupplier(excelContent);
                    ResponseData data = ResponseData.ok();
                    if (handResult != null) {
                        String filePath = DataImportUtil.createFile(String.valueOf(handResult.get("templateName")), config.getUploadDir(), config.getWebDir(), (List<String>) handResult.get("rowTitles"), (List<Object[]>) handResult.get("exportData"));
                        data.putDataValue("msg", "导入失败，请点击红色提示内容下载文件检查内容是否符合要求。");
                        data.putDataValue("file", filePath);
                    }
                    return data;
                } else {
                    return ResponseData.customerError(1002, "没有数据导入成功，请确认导入板块与模板对应，且内容符合要求，并检查数据是否已存在！");
                }
            } else {
                return ResponseData.customerError(1002, "导入文件不存在！");
            }
        } catch (DuplicateKeyException e) {
            return ResponseData.customerError(1002, "存在相同的供应商公司名称和手机号！");
		}
	}

	/**
	 * 导出供应商
	 * @param response
	 * @param map
	 */
	@RequestMapping("/export")
	@ResponseBody
	@Log(opType = OperateType.QUERY, note = "导出供应商", module = "供应商管理/导出供应商")
	@Verify(code = "/media/supplier_manage", module = "供应商管理/导出供应商", action = "4")
	public void export(HttpServletResponse response, @RequestParam Map map){
		try{
			User user = AppUtil.getUser();
			List<Role> roleList = user.getRoles();
			if(roleList==null || roleList.size()==0){
				throw new QinFeiException(1001,"未查到角色信息");
			}else{
//				map.put("roleType",roleList.get(0).getType());
//				map.put("roleCode",roleList.get(0).getCode());
//				map.put("companyCode",user.getCompanyCode());
//				map.put("user",user);
				response.setContentType("application/binary;charset=utf-8");
				response.setHeader("Content-Disposition","attachment;fileName="+ URLEncoder.encode("供应商导出"+ DateUtils.getNowTime()+".xls","utf-8"));
				OutputStream out = response.getOutputStream();
				supplierService.export(map,out);
			}
		}catch (Exception e){
			log.error("供应商导出失败",e);
			throw new QinFeiException(1001,"供应商导出失败");
		}
	}

    @GetMapping("copy/{id}")
    @ApiOperation(value = "供应商拷贝", notes = "供应商拷贝")
    @Verify(code = "/media/copy", module = "供应商管理/供应商拷贝", action = "1")
    @ResponseBody
    public ResponseData copy(@PathVariable("id") Integer id) {
        try{
            supplierService.copy(id);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(),byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002,e.getMessage());
        }
    }

	@GetMapping("handlerSameSupplierPhone")
	@ResponseBody
	public ResponseData handlerSameSupplierPhone() {
		return supplierService.handlerSameSupplierPhone();
	}
}