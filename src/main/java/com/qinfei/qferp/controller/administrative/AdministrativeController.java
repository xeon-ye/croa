package com.qinfei.qferp.controller.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.UserBusinessPlan;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.administrative.IAdministrative;
import com.qinfei.qferp.service.administrative.IAdministrativeLeaveService;
import com.qinfei.qferp.service.administrative.IAdministrativeOutWorkService;
import com.qinfei.qferp.service.administrative.IAdministrativeOverTimeWorkService;
import com.qinfei.qferp.service.impl.administrative.AdministrativeOnBusinessService;
import com.qinfei.qferp.service.impl.administrative.UserBusinessPlanService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ProcessDefinitionUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;

@Controller
@Slf4j
@RequestMapping("/administrative")
class AdministrativeController {
    @Autowired
    private IAdministrative administrative;
    @Autowired
    private IAdministrativeLeaveService leaveService;
    @Autowired
    private IAdministrativeOutWorkService outWork;
    @Autowired
    private IAdministrativeOverTimeWorkService overTimeWork;
    @Autowired
    private AdministrativeOnBusinessService onBusinessService;
    @Autowired
    private ProcessDefinitionUtils processDefinitionUtils;
    @Autowired
    private UserBusinessPlanService userBusinessPlanService;


    /**
     * 获取流程管理相关数据
     *
     * @param params
     * @param pageable
     * @return
     */
    @RequestMapping("getAdministrative")
    @ApiOperation(value = "获取行政考勤相关数据", notes = "获取行政考勤相关数据\n" + "return data ;")
//    @Log(opType = OperateType.QUERY, module = "行政管理", note = "获取行政考勤相关数据")
    @ResponseBody
    public PageInfo<Administrative> getAdministrative(@RequestParam Map<String, Object> params, @RequestParam(value = "administrativeType[]", required = false) Integer[] administrativeTypes, @RequestParam(value = "approveState[]", required = false) Integer[] approveStates, @PageableDefault() Pageable pageable) {
        if (administrativeTypes != null) {
            params.put("administrativeTypes", administrativeTypes);
        }
        if (approveStates != null) {
            params.put("approveStates", approveStates);
        }
        return administrative.administrativeList(params, pageable);
    }

    @RequestMapping("/getAdministrative1/{type}")
    @ApiOperation(value = "获取行政考勤相关数据", notes = "获取行政考勤相关数据\n" + "return data ;")
//    @Log(opType = OperateType.QUERY, module = "行政管理", note = "获取行政考勤相关数据")
    @ResponseBody
    public PageInfo<Administrative> getAdministrative1(@RequestParam Map<String, Object> params, @RequestParam(value = "administrativeType[]", required = false) Integer[] administrativeTypes, @RequestParam(value = "approveState[]", required = false) Integer[] approveStates, @PageableDefault() Pageable pageable, @PathVariable("type") Integer type) {
        if (administrativeTypes != null) {
            params.put("administrativeTypes", administrativeTypes);
        }
        if (approveStates != null) {
            params.put("approveStates", approveStates);
        }
        return administrative.administrativeList1(params, pageable,type);
    }

    /**
     * 获取行政流程详情
     *
     * @param administrativeType
     * @param administrativeId
     * @return
     */
    @RequestMapping("getAdministrativeDetail")
    @ApiOperation(value = "获取行政考勤详情", notes = "获取行政考勤详情\n" + "return data ;")
//    @Log(opType = OperateType.QUERY, module = "行政管理", note = "获取行政考勤详情")
    @ResponseBody
    public ResponseData getAdministrativeDetail(Integer administrativeType, Integer administrativeId) {
        //通过id获取各种行政详情
        ResponseData data = null;
        switch (administrativeType) {
            case 1://请假
                data = leaveService.getLeaveByAdministrativeId(administrativeId);
                break;
            case 2://加班
                data = overTimeWork.getTimeworkByAdministrativeId(administrativeId);
                break;
            case 3://外出
                data = outWork.getOutWorkByAdministrativeId(administrativeId);
                break;
            case 4://出差
                data = userBusinessPlanService.viewBusiness(administrativeId);
                break;
        }
        return data;
    }

    @RequestMapping("exportList")
//    @Log(opType = OperateType.QUERY, module = "行政管理", note = "导出列表")
    @ResponseBody
    public void exportList(HttpServletResponse response,@RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("行政流程表格导出" + DateUtils.getNowTime() + ".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            administrative.exportList(params, outputStream);
        } catch (Exception e) {
            log.error("导出失败", e);
        }
    }

    @RequestMapping("exportContent")
//    @Log(opType = OperateType.QUERY, module = "行政管理", note = "导出详情")
    @ResponseBody
    public void exportContent(HttpServletResponse response, @RequestParam Map map) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("行政流程内容导出" + DateUtils.getNowTime() + ".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            administrative.exportContent(map, outputStream);
        } catch (Exception e) {
            log.error("导出失败", e);
        }
    }

    /**
     * 获取所有的可以驳回的节点
     *
     * @param dataId
     * @param process
     */
    @RequestMapping("getFlowElement")
//    @Log(opType = OperateType.QUERY, module = "行政管理", note = "获取流程可以驳回的节点")
    @ResponseBody
    public ResponseData getFlowElement(String dataId, int process) {
        ResponseData ok = ResponseData.ok();
        List<Map<String, Object>> list = processDefinitionUtils.getAllTask(dataId, process);
        ok.putDataValue("data", list);
        return ok;
    }


    /**
     * 通过id获取对象
     * @param id
     * @return
     */
    @RequestMapping("getById")
    //@Log(opType = OperateType.QUERY, module = "行政管理", note = "通过id获取对象")
    @ResponseBody
    public ResponseData getAdministrativeById(Integer id){
        ResponseData ok = ResponseData.ok();
        Administrative adm = administrative.getById(id);
        ok.putDataValue("entity", adm);
        return ok;
    }

}
