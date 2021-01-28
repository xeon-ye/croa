package com.qinfei.qferp.controller.employ;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qinfei.core.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.employ.EmployEntryComment;
import com.qinfei.qferp.entity.employ.EmployeeHire;
import com.qinfei.qferp.entity.employ.EmployeeSalary;
import com.qinfei.qferp.service.employ.IEmployEntryService;
import com.github.pagehelper.PageInfo;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 入职申请管理；
 *
 * @Author ：Yuan；
 * @Date ：2019/3/7 0007 14:42；
 */
@Controller
@RequestMapping(value = "/entryManage")
class EntryManageController {
    // 入职申请管理的业务接口；
    @Autowired
    private IEmployEntryService entryService;
    @Autowired
    private Config config;

    /**
     * 增加或更新录用信息；
     *
     * @param employeeHire：录用记录对象；
     * @return ：操作结果提示；
     */
    @RequestMapping("saveHire")
    @ApiOperation(value = "更新入职申请", notes = "增加或更新录用信息")
//    @Log(opType = OperateType.ADD, module = "入职申请管理", note = "增加或更新录用信息")
    @Verify(code = "/entryManage/saveHire", module = "入职管理/录用记录", action = "1")
    @ResponseBody
    public ResponseData saveHire(EmployeeHire employeeHire) {
        ResponseData data = ResponseData.ok();
        entryService.saveHire(data, employeeHire);
        return data;
    }

    /**
     * 增加或更新薪资信息；
     *
     * @param employeeSalary：薪资对象；
     * @return ：操作结果提示；
     */
    @RequestMapping("saveSalary")
    @ApiOperation(value = "更新入职申请", notes = "增加或更新薪资信息")
//    @Log(opType = OperateType.ADD, module = "入职申请管理", note = "增加或更新薪资信息")
    @Verify(code = "/entryManage/saveSalary", module = "入职管理/录用薪资", action = "1")
    @ResponseBody
    public ResponseData saveSalary(EmployeeSalary employeeSalary) {
        ResponseData data = ResponseData.ok();
        entryService.saveSalary(data, employeeSalary);
        return data;
    }

    /**
     * 增加或更新能力评估信息；
     *
     * @param entryComment：评估信息对象；
     * @param operate：操作类型，0为人事，1为测试；
     * @return ：操作结果提示；
     */
    @RequestMapping("saveComment")
    @ApiOperation(value = "更新入职申请", notes = "增加或更新能力评估信息")
//    @Log(opType = OperateType.ADD, module = "入职申请管理", note = "增加或更新能力评估信息")
    @Verify(code = "/entryManage/saveComment", module = "入职管理/评估信息", action = "1")
    @ResponseBody
    public ResponseData saveComment(EmployEntryComment entryComment, @RequestParam Integer operate) {
        ResponseData data = ResponseData.ok();
        entryService.saveComment(data, entryComment, operate);
        return data;
    }

    /**
     * 批量更新入职申请的状态；
     *
     * @param entryIds：入职申请ID数组；
     * @param operate：操作类型，0为删除，1为存档备用（不予考虑），2为恢复，3为离职再入职；
     * @return ：操作结果提示；
     */
    @RequestMapping("updateEntryState")
    @ApiOperation(value = "更新入职申请", notes = "更新入职申请的状态")
//    @Log(opType = OperateType.UPDATE, module = "入职申请管理", note = "更新入职申请的状态")
    @Verify(code = "/entryManage/updateEntryState", module = "入职管理/更新状态", action = "2")
    @ResponseBody
    public ResponseData updateEntryState(@RequestParam Integer[] entryIds, @RequestParam Integer operate) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("type", entryService.updateStateByBatchId(entryIds, operate));
        return data;
    }

    /**
     * 上传入职申请的各项材料，完成入职申请；
     *
     * @param entry：提交的申请字符串；
     * @return ：处理结果；
     */
    @RequestMapping("completeEntry")
    @ApiOperation(value = "更新入职申请", notes = "上传入职申请的各项材料，完成入职申请")
//    @Log(opType = OperateType.UPDATE, module = "入职申请管理", note = "上传入职申请的各项材料，完成入职申请")
    @Verify(code = "/entryManage/completeEntry", module = "入职管理/资料上传", action = "2")
    @ResponseBody
    public ResponseData completeEntry(@RequestParam String entry) {
        ResponseData data = ResponseData.ok();
        // 解析对象；
        JSONObject json = JSON.parseObject(entry);
        // 处理数据；
        data.putDataValue("message", entryService.completeEntry(json));
        return data;
    }

    /**
     * 入职申请的各项资料审核；
     *
     * @param entryComment：入职申请的审核信息；
     * @param operate：操作类型，0为同意，1为拒绝；
     * @return ：处理结果；
     */
    @RequestMapping("approveEntry")
    @ApiOperation(value = "更新入职申请", notes = "入职申请的各项资料审核")
//    @Log(opType = OperateType.UPDATE, module = "入职申请管理", note = "入职申请的各项资料审核")
    @Verify(code = "/entryManage/approveEntry", module = "入职管理/资料审核", action = "2")
    @ResponseBody
    public ResponseData approveEntry(EmployEntryComment entryComment, @RequestParam Integer operate) {
        ResponseData data = ResponseData.ok();
        // 完成申请操作；
        entryService.approveEntry(entryComment, operate);
        data.putDataValue("message", "操作完成。");
        return data;
    }

    /**
     * 提交审核入职审核流程；
     *
     * @param entryId：入职申请的ID；
     * @param userId：审核人ID；
     * @param userName：审核人姓名；
     * @param deptId：审核人所在部门；
     * @return ：处理结果提示信息；
     */
    @RequestMapping("startEmploy")
    @ApiOperation(value = "更新入职申请", notes = "提交审核入职审核流程")
//    @Log(opType = OperateType.UPDATE, module = "入职申请管理", note = "提交审核入职审核流程")
    @Verify(code = "/entryManage/startEmploy", module = "入职管理/员工录用", action = "2")
    @ResponseBody
    public ResponseData startEmploy(@RequestParam Integer entryId, @RequestParam Integer userId, @RequestParam String userName, @RequestParam Integer deptId) {
        ResponseData data = ResponseData.ok();
        entryService.startEmploy(data, entryId, userId, userName, deptId);
        return data;
    }

    /**
     * 通知入职申请这提供资料；
     *
     * @param entryId：入职申请的ID；
     * @return ：操作结果；
     */
    @RequestMapping("entryNotice")
    @ApiOperation(value = "更新入职申请", notes = "通知入职申请这提供资料")
//    @Log(opType = OperateType.UPDATE, module = "入职申请管理", note = "通知入职申请这提供资料")
    @Verify(code = "/entryManage/entryNotice", module = "入职管理/录用通知", action = "2")
    @ResponseBody
    public ResponseData entryNotice(@RequestParam Integer entryId) {
        ResponseData data = ResponseData.ok();
        entryService.noticeEntry(data, entryId);
        return data;
    }

    /**
     * 通用的审核流转；
     *
     * @param approveData：审核信息Json字符串；
     * @return ：操作结果；
     */
    @RequestMapping("completeApprove")
    @ApiOperation(value = "人事审核流转", notes = "审核流程")
//    @Log(opType = OperateType.UPDATE, module = "人事管理", note = "审核流程")
    @ResponseBody
    public ResponseData completeApprove(@RequestParam String approveData) {
        ResponseData data = ResponseData.ok();
        entryService.completeApprove(data, approveData);
        return data;
    }

    /**
     * 分页查询入职申请信息；
     *
     * @param params：查询参数集合；
     * @param pageable：分页信息对象；
     * @return ：入职申请信息；
     */
    @RequestMapping("getPageEntry")
    @ApiOperation(value = "查询入职申请", notes = "分页查询入职申请信息")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "分页查询入职申请信息")
    @Verify(code = "/entryManage/getPageEntry", module = "入职管理/入职查询", action = "4")
    @ResponseBody
    public PageInfo<Map<String, Object>> getPageEntry(@RequestParam Map<String, Object> params, @PageableDefault() Pageable pageable) {
        return entryService.selectPageEntry(params, pageable);
    }

    /**
     * 根据入职申请的ID查询入职申请信息；
     *
     * @param id：入职申请ID；
     * @return ：入职申请信息；
     */
    @RequestMapping("getEntryInfo")
    @ApiOperation(value = "查询入职申请", notes = "根根据入职申请的提供的查询码查询入职申请信息")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "根据入职申请的提供的查询码查询入职申请信息")
    @Verify(code = "/entryManage/getEntryInfo", module = "入职管理/入职查看", action = "4")
    @ResponseBody
    public ResponseData getEntryInfo(@RequestParam Integer id) {
        ResponseData data = ResponseData.ok();
        Map<String, Object> params = new HashMap<>();
        params.put("entryId", id);
        data.putDataValue("entry", entryService.getEntryInfo(params));
        return data;
    }

    /**
     * 导出单个入职申请数据；
     *
     * @param entryId：查询参数集合；
     * @return ：操作结果提示；
     */
    @RequestMapping("exportSingleData")
    @ApiOperation(value = "导出入职申请", notes = "导出单个入职申请数据")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "导出单个入职申请数据")
    @Verify(code = "/entryManage/exportSingleData", module = "入职管理/入职导出", action = "4")
    @ResponseBody
    public ResponseData exportData(@RequestParam Integer entryId) {
        ResponseData data = ResponseData.ok();
        String fileName = entryService.exportEntryData(entryId);
        if (StringUtils.isEmpty(fileName)) {
            data.putDataValue("message", "入职申请不存在，请检查。");
        } else {
            data.putDataValue("file", fileName);
        }
        return data;
    }

    /**
     * 批量导出入职申请数据；
     *
     * @param params：查询参数集合；
     * @param entryIds：入职申请ID数组；
     * @return ：操作结果提示；
     */
    @RequestMapping("exportData")
    @ApiOperation(value = "导出入职申请", notes = "批量导出入职申请数据")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "批量导出入职申请数据")
    @Verify(code = "/entryManage/exportData", module = "入职管理/入职导出", action = "4")
    @ResponseBody
    public ResponseData exportData(@RequestParam Map<String, Object> params, @RequestParam(value = "entryIds[]", required = false) Integer[] entryIds) {
        ResponseData data = ResponseData.ok();
        // 如果是勾选了ID，清空查询参数；
        if (entryIds != null) {
            params = new HashMap();
            params.put("entryIds", entryIds);
        }
        String fileName = entryService.exportEntryData(params);
        if (StringUtils.isEmpty(fileName)) {
            data.putDataValue("message", "没有找到相关数据，请检查。");
        } else {
            data.putDataValue("file", fileName);
        }
        return data;
    }

    /**
     * 根据入职申请ID查询入职申请相关的文件信息；
     *
     * @param entryId：入职申请ID；
     * @return ：入职申请信息；
     */
    @RequestMapping("getEntryFile")
    @ApiOperation(value = "查询入职申请", notes = "根据入职申请ID查询入职申请相关的文件信息")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "根据入职申请ID查询入职申请相关的文件信息")
    @Verify(code = "/entryManage/getEntryFile", module = "入职管理/文件获取", action = "4")
    @ResponseBody
    public ResponseData getEntryFile(@RequestParam Integer entryId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("comment", entryService.selectEntryFileById(entryId));
        return data;
    }

    /**
     * 根据入职申请ID查询入职申请相关的员工录用信息；
     *
     * @param entryId：入职申请ID；
     * @param deptId：入职申请的部门ID；
     * @return ：入职申请的审核信息；
     */
    @PostMapping("getEmployInfo")
    @ApiOperation(value = "查询入职申请", notes = "根据入职申请ID查询入职申请相关的员工录用信息")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "根据入职申请ID查询入职申请相关的员工录用信息")
    @Verify(code = "/entryManage/getEmployInfo", module = "入职管理/录用信息", action = "4")
    @ResponseBody
    public ResponseData getEmployInfo(@RequestParam Integer entryId) {
        ResponseData data = ResponseData.ok();
        entryService.getEmployInfo(data, entryId);
        return data;
    }

    @PostMapping("listDeptByFirstDept")
    @ApiOperation(value = "查询入职申请", notes = "根据一级部门获取二级部门信息")
    @ResponseBody
    public ResponseData getPost(@RequestParam String companyCode, @RequestParam Integer firstDept) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("dept", entryService.listDeptByFirstDept(companyCode, firstDept));
        return data;
    }

    @PostMapping("listLeaderByDeptId")
    @ApiOperation(value = "查询入职申请", notes = "根据部门ID获取部门负责人列表")
    @ResponseBody
    public ResponseData listLeaderByDeptId(@RequestParam Integer deptId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("users", entryService.listLeaderByDeptId(deptId));
        return data;
    }

    /**
     * 根据入职申请的查询码查询入职申请相关的员工录用信息，此接口只需登录即可访问，没有权限控制；
     *
     * @param code：查询码；
     * @return ：入职申请的审核信息；
     */
    @RequestMapping("getApproveInfo")
    @ApiOperation(value = "查询入职申请", notes = "根据入职申请的查询码查询入职申请相关的员工录用信息")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "根据入职申请的查询码查询入职申请相关的员工录用信息")
    @ResponseBody
    public ResponseData getApproveInfo(@RequestParam String code) {
        ResponseData data = ResponseData.ok();
        entryService.setEmployApproveData(data, code);
        return data;
    }

    /**
     * 导入员工数据
     */
    @PostMapping("importEmployeeData")
    @ApiOperation(value = "导入员工数据", notes = "导入员工数据")
//    @Log(opType = OperateType.ADD, module = "导入员工数据", note = "导入员工数据")
//    @Verify(code = "/entryManage/importEmployeeData", module = "入职管理/录用信息", action = "1")
    @ResponseBody
    public ResponseData importEmployeeData(@RequestParam("file") MultipartFile file){
//        return ResponseData.ok().putDataValue("msg", entryService.importEmployeeData(file, request, response));
        return entryService.importEmployeeData(file);
    }
}