package com.qinfei.qferp.controller.employ;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.metadata.TableStyle;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.util.IOUtils;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.employ.EmployeeSalary;
import com.qinfei.qferp.excel.EmployeeExcelInfo;
import com.qinfei.qferp.service.employ.IEmployeeSalaryService;
import com.qinfei.qferp.utils.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.employ.EmployeeConnect;
import com.qinfei.qferp.entity.employ.EmployeeLeave;
import com.qinfei.qferp.entity.employ.EmployeeTransfer;
import com.qinfei.qferp.service.employ.IEmployeeService;
import com.github.pagehelper.PageInfo;

import io.swagger.annotations.ApiOperation;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * 员工花名册管理；
 *
 * @Author ：Yuan；
 * @Date ：2019/3/22 0022 11:08；
 */
@Controller
@RequestMapping(value = "/employeeManage")
class EmployeeManageController {
    // 员工花名册业务接口；
    @Autowired
    private IEmployeeService employeeService;
    @Autowired
    private IEmployeeSalaryService employeeSalaryService;

    /**
     * 员工信息编辑；
     *
     * @param employeeInfo：员工对象字符串；
     * @return ：处理结果；
     */
    @RequestMapping("updateEmployee")
    @ApiOperation(value = "更新员工信息", notes = "员工信息编辑")
//    @Log(opType = OperateType.UPDATE, module = "员工管理", note = "员工信息编辑")
    @Verify(code = "/employeeManage/updateEmployee", module = "员工管理/编辑员工", action = "2")
    @ResponseBody
    public ResponseData updateEmployee(@RequestParam String employeeInfo) {
        ResponseData data = ResponseData.ok();
        employeeService.updateEmployee(employeeInfo);
        data.putDataValue("message", "操作完成。");
        return data;
    }

    /**
     * 删除员工数据；
     *
     * @param empIds：员工ID数组；
     * @return ：处理结果；
     */
    @RequestMapping("deleteEmployee")
    @ApiOperation(value = "更新员工信息", notes = "删除员工数据")
//    @Log(opType = OperateType.UPDATE, module = "员工管理", note = "删除员工数据")
    @Verify(code = "/employeeManage/deleteEmployee", module = "员工管理/删除员工", action = "2")
    @ResponseBody
    public ResponseData deleteEmployee(@RequestParam Integer[] empIds) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", employeeService.deleteEmployee(empIds));
        return data;
    }

    /**
     * 员工生成账号；
     *
     * @param empId：员工ID；
     * @return ：处理结果；
     */
    @RequestMapping("grantEmployee")
    @ApiOperation(value = "更新员工信息", notes = "绑定用户登录账户")
//    @Log(opType = OperateType.UPDATE, module = "员工管理", note = "绑定用户登录账户")
    @Verify(code = "/employeeManage/grantEmployee", module = "员工管理/授权账号", action = "2")
    @ResponseBody
    public ResponseData grantEmployee(@RequestParam Integer empId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", employeeService.grantEmployee(empId));
        return data;
    }

    /**
     * 提交员工转正审核流程；
     *
     * @param empId：员工ID；
     * @param userId：审核人ID；
     * @param userName：审核人姓名；
     * @return ：处理结果提示信息；
     */
    @RequestMapping("startFormal")
    @ApiOperation(value = "更新员工信息", notes = "提交员工转正审核流程")
//    @Log(opType = OperateType.UPDATE, module = "员工管理", note = "提交员工转正审核流程")
    @Verify(code = "/employeeManage/startFormal", module = "员工管理/提交转正", action = "2")
    @ResponseBody
    public ResponseData startFormal(@RequestParam Integer empId, @RequestParam Integer userId, @RequestParam String userName) {
        ResponseData data = ResponseData.ok();
        employeeService.startFormalProcess(data, empId, userId, userName);
        return data;
    }

    /**
     * 提交员工离职审核流程；
     *
     * @param leave：员工离职对象；
     * @param userId：审核人ID；
     * @param userName：审核人姓名；
     * @param leaveCompany：选中的公司原因；
     * @param leavePerson：选中的个人原因；
     * @param otherReason：选中的其他原因；
     * @return ：处理结果提示信息；
     */
    @RequestMapping("startLeave")
    @ApiOperation(value = "更新员工信息", notes = "提交员工离职审核流程")
//    @Log(opType = OperateType.UPDATE, module = "员工管理", note = "提交员工离职审核流程")
    @Verify(code = "/employeeManage/startLeave", module = "员工管理/提交离职", action = "2")
    @ResponseBody
    public ResponseData startLeave(EmployeeLeave leave, @RequestParam Integer userId, @RequestParam String userName, Integer[] leaveCompany, Integer[] leavePerson, Integer[] otherReason) {
        ResponseData data = ResponseData.ok();
        employeeService.startLeaveProcess(data, leave, leaveCompany, leavePerson, otherReason, userId, userName);
        return data;
    }

    /**
     * 提交员工调岗审核流程；
     *
     * @param transfer：员工调岗对象；
     * @param userId：审核人ID；
     * @param userName：审核人姓名；
     * @return ：处理结果提示信息；
     */
    @RequestMapping("startTransfer")
    @ApiOperation(value = "更新员工信息", notes = "提交员工调岗审核流程")
//    @Log(opType = OperateType.UPDATE, module = "员工管理", note = "提交员工调岗审核流程")
    @Verify(code = "/employeeManage/startTransfer", module = "员工管理/提交调岗", action = "2")
    @ResponseBody
    public ResponseData startTransfer(EmployeeTransfer transfer, @RequestParam Integer userId, @RequestParam String userName) {
        ResponseData data = ResponseData.ok();
        employeeService.startTransferProcess(data, transfer, userId, userName);
        return data;
    }

    /**
     * 提交员工交接审核流程；
     *
     * @param connect：员工交接对象；
     * @param userId：审核人ID；
     * @param userName：审核人姓名；
     * @return ：处理结果提示信息；
     */
    @RequestMapping("startConnect")
    @ApiOperation(value = "更新员工信息", notes = "提交员工交接审核流程")
//    @Log(opType = OperateType.UPDATE, module = "员工管理", note = "提交员工交接审核流程")
    @Verify(code = "/employeeManage/startConnect", module = "员工管理/提交交接", action = "2")
    @ResponseBody
    public ResponseData startConnect(EmployeeConnect connect, @RequestParam Integer userId, @RequestParam String userName) {
        ResponseData data = ResponseData.ok();
        employeeService.startConnectProcess(data, connect, userId, userName);
        return data;
    }

    /**
     * 审批流程中的交接数据更新，此接口只需登录即可访问，没有权限控制；
     *
     * @param connect：员工交接对象；
     * @param processState：当前审批节点；
     * @return ：处理结果提示信息；
     */
    @RequestMapping("updateConnect")
    @ApiOperation(value = "更新员工信息", notes = "审批流程中的交接数据更新")
//    @Log(opType = OperateType.UPDATE, module = "员工管理", note = "审批流程中的交接数据更新")
    @ResponseBody
    public ResponseData updateConnect(EmployeeConnect connect, @RequestParam Integer processState) {
        ResponseData data = ResponseData.ok();
        employeeService.updateConnect(data, connect, processState);
        return data;
    }

    /**
     * 分页查询员工花名册信息；
     *
     * @param params：查询参数集合；
     * @param pageable：分页信息对象；
     * @return ：员工花名册信息；getEmployee
     */
    @RequestMapping("getPageEmployee")
    @ApiOperation(value = "查询员工信息", notes = "分页查询员工花名册信息")
//    @Log(opType = OperateType.QUERY, module = "员工管理", note = "分页查询员工花名册信息")
    @Verify(code = "/employeeManage/getPageEmployee", module = "员工管理/员工查询", action = "4")
    @ResponseBody
    public PageInfo<Map<String, Object>> getPageEmployee(@RequestParam Map<String, Object> params, @PageableDefault() Pageable pageable) {
        return employeeService.selectPageEmployee(params, pageable);
    }

   /* @RequestMapping("getEmployee")
    @ApiOperation(value = "员工管理首页", notes = "查询员工信息")
    @ResponseBody
    public PageInfo<Map<String,Object>> getEmployee(@RequestParam Map<String,Object> params, @PageableDefault(size = 10) Pageable pageable){
        return employeeService.getEmployee(params, pageable);
    }


    //员工管理首页，统计前5项的值
    @RequestMapping("topStatistics")
    @ApiOperation(value = "员工管理首页",notes = "统计前5项信息")
    @ResponseBody
    public Map topStatistics(@RequestParam Map map){
        return employeeService.topStatistics(map);
    }

    //员工管理首页，学历饼图信息
    @RequestMapping("educationPie")
    @ApiOperation(value = "员工管理首页",notes ="获取学历饼图")
    @ResponseBody
    public Map educationPie(@RequestParam Map map){
        return employeeService.educationPie(map);
    }

    //员工管理首页，年龄段饼图信息
    @RequestMapping("ageGroupPie")
    @ApiOperation(value = "员工管理首页",notes ="获取年龄段饼图")
    @ResponseBody
    public Map ageGroupPie(@RequestParam Map map){
        return employeeService.ageGroupPie(map);
    }
*/
    /**
     * 根据员工ID查询编辑员工所需的相关信息；
     *
     * @param empId：员工ID；
     * @return ：员工信息；
     */
    @RequestMapping("getEmployeeEdit")
    @ApiOperation(value = "查询员工信息", notes = "根据员工ID查询编辑员工所需的相关信息")
//    @Log(opType = OperateType.QUERY, module = "员工管理", note = "根据员工ID查询编辑员工所需的相关信息")
    @Verify(code = "/employeeManage/getEmployeeEdit", module = "员工管理/员工信息", action = "4")
    @ResponseBody
    public ResponseData getEmployeeEdit(@RequestParam Integer empId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("employee", employeeService.selectEditInfoById(empId));
        return data;
    }

    /**
     * 根据员工ID查询提交转正流程需要的相关信息；
     *
     * @param empId：员工ID；
     * @return ：员工信息；
     */
    @RequestMapping("getEmployeeFormal")
    @ApiOperation(value = "查询员工信息", notes = "根据员工ID查询提交转正流程需要的相关信息")
//    @Log(opType = OperateType.QUERY, module = "员工管理", note = "根据员工ID查询提交转正流程需要的相关信息")
    @Verify(code = "/employeeManage/getEmployeeFormal", module = "员工管理/员工信息", action = "4")
    @ResponseBody
    public ResponseData getEmployeeFormal(@RequestParam Integer empId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("employee", employeeService.selectFormalInfoById(empId));
        return data;
    }

    /**
     * 根据员工ID查询提交离职流程需要的相关信息；
     *
     * @param empId：员工ID；
     * @return ：员工信息；
     */
    @RequestMapping("getEmployeeLeave")
    @ApiOperation(value = "查询员工信息", notes = "根据员工ID查询提交离职流程需要的相关信息")
//    @Log(opType = OperateType.QUERY, module = "员工管理", note = "根据员工ID查询提交离职流程需要的相关信息")
    @Verify(code = "/employeeManage/getEmployeeLeave", module = "员工管理/员工信息", action = "4")
    @ResponseBody
    public ResponseData getEmployeeLeave(@RequestParam Integer empId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("employee", employeeService.selectLeaveInfoById(empId));
        return data;
    }

    /**
     * 根据员工id获取该员工绑定的客户数
     * @param empId
     * @return
     */
    @RequestMapping("getCustByEmpId")
    @ResponseBody
    public ResponseData getCustByEmpId(@RequestParam Integer empId){
        ResponseData data = ResponseData.ok();
        long custNum = employeeService.getCustByEmpId(empId);
        data.putDataValue("custNum", custNum);
        return data;
    }

    /**
     * 根据员工ID查询提交调岗流程需要的相关信息；
     *
     * @param empId：员工ID；
     * @return ：员工信息；
     */
    @RequestMapping("getEmployeeTransfer")
    @ApiOperation(value = "查询员工信息", notes = "根据员工ID查询提交调岗流程需要的相关信息")
//    @Log(opType = OperateType.QUERY, module = "员工管理", note = "根据员工ID查询提交调岗流程需要的相关信息")
    @Verify(code = "/employeeManage/getEmployeeTransfer", module = "员工管理/员工信息", action = "4")
    @ResponseBody
    public ResponseData getEmployeeTransfer(@RequestParam Integer empId) {
        ResponseData data = ResponseData.ok();
        try{
            data.putDataValue("employee", employeeService.selectTransferInfoById(empId));
        }catch (Exception e){
            throw e;
        }
        return data;
    }

    /**
     * 根据员工ID查询提交交接流程需要的相关信息；
     *
     * @param empId：员工ID；
     * @param processId：流程标识，参考com.qinfei.qferp.utils.IProcess；
     * @return ：员工信息；
     */
    @RequestMapping("getEmployeeConnect")
    @ApiOperation(value = "查询员工信息", notes = "根据员工ID查询提交交接流程需要的相关信息")
//    @Log(opType = OperateType.QUERY, module = "员工管理", note = "根据员工ID查询提交交接流程需要的相关信息")
    @Verify(code = "/employeeManage/getEmployeeConnect", module = "员工管理/员工信息", action = "4")
    @ResponseBody
    public ResponseData getEmployeeConnect(@RequestParam Integer empId, @RequestParam Integer processId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("employee", employeeService.selectConnectInfoById(empId, processId));
        return data;
    }

    /**
     * 根据员工的权限访问码查询员工转正信息，此接口只需登录即可访问，没有权限控制；
     *
     * @param code：查询码；
     * @return ：转正申请的审核信息；
     */
    @RequestMapping("getFormalInfo")
    @ApiOperation(value = "查询入职申请", notes = "根据员工的权限访问码查询员工转正信息")
//    @Log(opType = OperateType.QUERY, module = "员工管理", note = "根据员工的权限访问码查询员工转正信息")
    @ResponseBody
    public ResponseData getFormalInfo(@RequestParam String code) {
        ResponseData data = ResponseData.ok();
        employeeService.setFormalApproveData(data, code);
        return data;
    }

    /**
     * 根据员工的权限访问码查询员工离职信息，此接口只需登录即可访问，没有权限控制；
     *
     * @param code：查询码；
     * @return ：转正申请的审核信息；
     */
    @RequestMapping("getLeaveInfo")
    @ApiOperation(value = "查询入职申请", notes = "根据员工的权限访问码查询员工离职信息")
//    @Log(opType = OperateType.QUERY, module = "员工管理", note = "根据员工的权限访问码查询员工离职信息")
    @ResponseBody
    public ResponseData getLeaveInfo(@RequestParam String code) {
        ResponseData data = ResponseData.ok();
        employeeService.setLeaveApproveData(data, code);
        return data;
    }

    /**
     * 根据员工的权限访问码查询员工调岗信息，此接口只需登录即可访问，没有权限控制；
     *
     * @param code：查询码；
     * @return ：转正申请的审核信息；
     */
    @RequestMapping("getTransferInfo")
    @ApiOperation(value = "查询入职申请", notes = "根据员工的权限访问码查询员工调岗信息")
//    @Log(opType = OperateType.QUERY, module = "员工管理", note = "根据员工的权限访问码查询员工调岗信息")
    @ResponseBody
    public ResponseData getTransferInfo(@RequestParam String code) {
        ResponseData data = ResponseData.ok();
        employeeService.setTransferApproveData(data, code);
        return data;
    }

    /**
     * 根据员工的权限访问码查询员工交接信息，此接口只需登录即可访问，没有权限控制；
     *
     * @param code：查询码；
     * @return ：转正申请的审核信息；
     */
    @RequestMapping("getConnectInfo")
    @ApiOperation(value = "查询入职申请", notes = "根据员工的权限访问码查询员工交接信息")
//    @Log(opType = OperateType.QUERY, module = "员工管理", note = "根据员工的权限访问码查询员工交接信息")
    @ResponseBody
    public ResponseData getConnectInfo(@RequestParam String code, @RequestParam Integer conType) {
        ResponseData data = ResponseData.ok();
        employeeService.setConnectApproveData(data, code, conType);
        return data;
    }

    /**
     * 删除家庭成员
     */
    @RequestMapping("delFamily")
//    @Log(opType = OperateType.DELETE, module = "员工管理", note = "删除员工家庭成员")
    @ResponseBody
    public ResponseData delFamily(@RequestParam int[] ids) {
        ResponseData data = ResponseData.ok();
        employeeService.delFamily(ids);
        return data;
    }

    /**
     * 删除教育经历
     */
    @RequestMapping("delEducation")
//    @Log(opType = OperateType.DELETE, module = "员工管理", note = "删除员工教育经历")
    @ResponseBody
    public ResponseData delEducation(@RequestParam int[] ids) {
        ResponseData data = ResponseData.ok();
        employeeService.delEducation(ids);
        return data;
    }

    /**
     * 删除工作经历
     */
    @RequestMapping("delExperience")
//    @Log(opType = OperateType.DELETE, module = "员工管理", note = "删除员工教育经历")
    @ResponseBody
    public ResponseData delExperience(@RequestParam int[] ids) {
        ResponseData data = ResponseData.ok();
        employeeService.delExperience(ids);
        return data;
    }

    @GetMapping("template")
    public void export(HttpServletResponse response) {
        //调用service查询方法返回结果集
        OutputStream out = null;
        try {
            String fileName = "员工模板";
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setCharacterEncoding("utf-8");
            //通知浏览器以附件的形式下载处理
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xls");
            out = response.getOutputStream();
            templateDownload(out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(out);
        }
    }

    //文件下载
    private void templateDownload(OutputStream out) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        String filePath = "classpath:/files" +  File.separator + "employeeTemplate.xls";
        Resource resource = resourceLoader.getResource(filePath);
        InputStream in = resource.getInputStream();
        int totalSize = in.available(); //文件总大小
        int size = totalSize > 1024 ? 1024 : in.available();
        byte [] bytes = new byte[size];
        int length = -1;
        while ((length = in.read(bytes)) != -1){
            out.write(bytes, 0, length);
            out.flush();
        }
        if(in != null){
            in.close();
        }
    }

    @GetMapping("linkEmployee")
    @Verify(code = "/employeeManage/getEmployeeEdit", module = "员工管理/员工信息", action = "4")
    @ResponseBody
    @ApiOperation(value = "关联员工", notes = "关联员工")
    public ResponseData linkEmployee(Integer empId) {
        return employeeService.linkEmployee(empId);
    }

    /**
     * 手动关联用户（多个）
     * @param userId
     * @param userName
     * @param empId
     * @return
     */
    @GetMapping("linkEmpUserId")
    @ResponseBody
    public ResponseData linkEmpUserId(@RequestParam("userId") Integer userId,@RequestParam("userName") String userName,
                                      @RequestParam("deptId") Integer deptId,@RequestParam("empId") Integer empId,@RequestParam("empDept") Integer empDept) {
        try{
            ResponseData data = ResponseData.ok();
            employeeService.linkEmpUserId(userId,userName,deptId, empId,empDept);
            return data;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "关联用户失败！");
        }
    }

    @PostMapping("getEmploySalary")
    @ApiOperation(value = "员工查询补全", notes = "获取当前员工薪资")
    @Verify(code = "/employeeManage/getEmploySalary", module = "员工查询补全/获取当前员工薪资", action = "4")
    @ResponseBody
    public ResponseData getEmploySalary(@RequestParam Integer entryId){
        try{
            return ResponseData.ok().putDataValue("salary", employeeSalaryService.selectByParentEntryId(entryId));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "获取员工薪资失败！");
        }
    }

    /**
     * 增加或更新薪资信息；
     *
     * @param employeeSalary：薪资对象；
     * @return ：操作结果提示；
     */
    @RequestMapping("saveSalary")
    @ApiOperation(value = "员工查询补全", notes = "增加或更新薪资信息")
    @Verify(code = "/employeeManage/getEmploySalary", module = "员工查询补全/获取当前员工薪资", action = "1")
    @ResponseBody
    public ResponseData saveSalary(EmployeeSalary employeeSalary) {
        try{
            employeeSalaryService.saveOrUpdate(employeeSalary);
            return ResponseData.ok();
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "更新员工薪资失败！");
        }

    }

    @PostMapping("getSalaryPermission")
    @ApiOperation(value = "档案/员工查询补全", notes = "查询工资权限")
    @Verify(code = "/employeeManage/getEmploySalary", module = "员工查询补全/查询工资权限", action = "4")
    @ResponseBody
    public ResponseData getEmploySalaryPermission(){
        return ResponseData.ok();
    }
}