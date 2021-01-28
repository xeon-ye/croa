package com.qinfei.qferp.controller;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.workup.WorkupRequestParam;
import com.qinfei.qferp.service.administrative.IUserBusinessPlanService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 流程管理；
 *
 * @Author ：Yuan；
 * @Date ：2018/12/7 0007 16:24；
 */
@Slf4j
@Controller
@RequestMapping(value = "process")
class ProcessController {
    // 获取流程接口；
    @Autowired
    private IProcessService processService;
    @Autowired
    private IUserBusinessPlanService userBusinessPlanService;
    @Autowired
    private Config config;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    /**
     * 流程的审核；
     *
     * @param taskIds：需要审核的任务ID；
     * @param desc：审核备注信息；
     * @return ：操作完成的提示信息；
     */
    @RequestMapping(value = "apply")
    @ApiOperation(value = "流程审核", notes = "流程审核操作")
    @Log(opType = OperateType.UPDATE, module = "流程管理", note = "流程审核")
    @ResponseBody
    public ResponseData applyProcesses(@RequestParam String[] taskIds, String desc, @RequestParam boolean agree) {
        try{
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", processService.approveProcess(taskIds, desc, agree));
            return data;
        }catch (FlowableException e) {
            e.printStackTrace();
            String message = e.getMessage();
            if (message.lastIndexOf(":") != -1) {
                message = message.substring(message.lastIndexOf(":") + 1);
            }
            return ResponseData.customerError(1001, message);
        }catch (QinFeiException e){
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, "审核错误，请联系管理员！");
        }
    }

    /**
     * 财务批量拒绝
     */
    @RequestMapping(value = "refused")
    @ApiOperation(value="流程审核",notes="流程审核操作")
    @Log(opType = OperateType.UPDATE,module = "审核批管理",note = "流程审核")
    @ResponseBody
    public ResponseData refused(@RequestParam Map<String,Object> map){
        try{
            ResponseData data = ResponseData.ok();

            data.putDataValue("message", processService.refused(map));
            return data;
        }catch (FlowableException e) {
            e.printStackTrace();
            String message = e.getMessage();
            if (message.lastIndexOf(":") != -1) {
                message = message.substring(message.lastIndexOf(":") + 1);
            }
            return ResponseData.customerError(1001, message);
        }catch (QinFeiException e){
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, "审核错误，请联系管理员！");
        }


    }

    /**
     * 流程的审核；
     *
     * @param taskIds：需要审核的任务ID；
     * @param desc：审核备注信息；
     * @return ：操作完成的提示信息；
     */
    @RequestMapping(value = "apply1")
    @ApiOperation(value = "流程审核", notes = "流程审核操作")
    @Log(opType = OperateType.UPDATE, module = "流程管理", note = "流程审核")
    @ResponseBody
    public ResponseData applyProcesses(@RequestParam String[] taskIds, String desc, @RequestParam boolean agree,@RequestParam boolean nextGatewayValue) {
        try{
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", processService.approveProcess(taskIds, desc, agree, nextGatewayValue));
            return data;
        }catch (FlowableException e){
            e.printStackTrace();
            String message = e.getMessage();
            if(message.lastIndexOf(":") != -1){
                message = message.substring(message.lastIndexOf(":") + 1);
            }
            return ResponseData.customerError(1001, message);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, "审核错误，请联系管理员！");
        }
    }

    /**
     * 流程的撤回；
     *
     * @param taskId：需要审核的任务ID；
     * @return ：操作完成的提示信息；
     */
    @RequestMapping(value = "withdraw")
    @ApiOperation(value = "流程审核", notes = "流程撤回操作")
    @Log(opType = OperateType.UPDATE, module = "流程管理", note = "流程撤回")
    @ResponseBody
    public ResponseData withdrawProcesses(@RequestParam String taskId, Integer itemId) {
        ResponseData data = ResponseData.ok();
        String message = processService.withdrawProcess(taskId, itemId);
        data.putDataValue("message", message);
        return data;
    }
    /**
     * 行政流程的测回
     */
    @RequestMapping(value = "administrativeWithdrawal")
    @ApiOperation(value = "行政流程测回",notes = "行政流程测回")
    @ResponseBody
    public ResponseData administrativeWithdrawal(@RequestParam Integer adminId,@RequestParam String taskId,@RequestParam Integer itemId,@RequestParam Integer administrativeType,Integer administrativeTime){
        ResponseData data = ResponseData.ok();
        Long taskState = userBusinessPlanService.selectState(taskId);
        if (taskState !=null){
            if (taskState == IConst.STATE_ZJLFS && administrativeType==4){
                userBusinessPlanService.deleteCon(adminId);
            }else if ( administrativeType==1 && administrativeTime>16){
                String instId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
                runtimeService.setVariable(instId, "nextGatewayValue", true);
            }
        }
        String message = processService.withdrawProcess(taskId, itemId);
        data.putDataValue("message", message);
        return data;
    }

    /**
     * 获取待办的审核任务列表；
     *
     * @return ：当前登录人待审核任务集合；
     */
    @RequestMapping(value = "list")
    @ApiOperation(value = "获取待审核任务", notes = "获取待审核任务列表")
    @Log(opType = OperateType.QUERY, module = "流程管理", note = "审核任务")
    @ResponseBody
    public PageInfo<Map<String, Object>> listTasks(Integer page, Integer size, @RequestParam Map map) {
        return processService.listTasks(map, page, size);
    }


    /**
     * 获取流程当前节点审批完成任务
     */

    @RequestMapping("theApproved")
    @ResponseBody
    public PageInfo<Map>theApproved(Integer page, Integer size, @RequestParam Map map){
        return  processService.theApproved(map , page ,size);
    }


    /**
     * 获取流程的审核记录；
     *
     * @param dataId：审核的数据ID；
     * @param process：流程标志，定义参考com.qinfei.qferp.utils.IProcess；
     * @return ：操作完成的提示信息；
     */
    @RequestMapping(value = "history")
    @ApiOperation(value = "获取流程审核记录", notes = "获取流程审核记录")
    @Log(opType = OperateType.QUERY, module = "流程管理", note = "审核记录")
    @ResponseBody
    public ResponseData listTaskHistory(@RequestParam String dataId, @RequestParam Integer process) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("data", processService.listTaskHistory(dataId, process));
        return data;
    }

    /**
     * 获取流程图；
     *
     * @param response：响应对象；
     * @param taskId：任务ID；
     */
    @RequestMapping(value = "image")
    @ApiOperation(value = "获取流程线路图", notes = "获取流程线路图")
//	@Log(opType = OperateType.QUERY, module = "流程管理", note = "流程线路")
    public void getProcessImage(HttpServletResponse response, @RequestParam String taskId) {
        processService.getProcessImage(response, taskId);
    }

    /**
     * 获取流程图；
     *
     * @param response：响应对象；
     * @param dataId：任务ID；
     * @param process：流程类型，定义参考com.qinfei.qferp.utils.IProcess；
     */
    @RequestMapping(value = "getImage")
    @ApiOperation(value = "获取流程线路图", notes = "获取流程线路图")
//	@Log(opType = OperateType.QUERY, module = "流程管理", note = "流程线路")
    public void getProcessImage(HttpServletResponse response, @RequestParam String dataId, @RequestParam Integer process) {
        processService.getProcessImag(response, dataId, process);
    }

    @GetMapping("rollback")
    @ResponseBody
    public ResponseData selectOne(String taskId, String target, Integer nextUserId) {
        processService.rollback(taskId, target,nextUserId);
        return ResponseData.ok();
    }

    @GetMapping("taskDefKey")
    @ResponseBody
    private ResponseData listTaskDefKey(String taskId) {
        return ResponseData.ok().putDataValue("result", processService.listTaskDefKey(taskId));
    }

    /**
     * 获取流程部署列表；
     */
    @RequestMapping(value = "listProcessDefinition")
    @ApiOperation(value = "获取流程部署列表", notes = "获取流程部署列表")
    @ResponseBody
    public PageInfo<Map<String, Object>> listProcessDefinition(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable) {
        return processService.listProcessDefinition(map, pageable);
    }

    /**
     * @param file     文件数组
     * @param filePart 文件属于哪个部分
     */
    @PostMapping("fileUpload")
    @ResponseBody
    public ResponseData fileUpload(MultipartFile[] file, @RequestParam(required = false) String filePart) throws IOException {
        String dateDir = DateUtils.format(new Date(), "yyyy-MM/");
        String uploadDir = config.getUploadDir() + dateDir + filePart;
        String webdir = config.getWebDir() + dateDir + filePart;
        File fileDir = new File(uploadDir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        List<Map<String, String>> fileList = new ArrayList<>();
        if(file != null && file.length > 0){
            for (MultipartFile multipartFile : file) {
                String originalFilename = multipartFile.getOriginalFilename();
                if (!StringUtils.isEmpty(originalFilename)) {
                    Map<String, String> map = new HashMap<>();
                    File f = new File(uploadDir,originalFilename);
                    multipartFile.transferTo(f);
                    map.put("file", dateDir+filePart + "/" + originalFilename);
                    map.put("oriName", originalFilename);
                    fileList.add(map);
                }
            }
        }
        return ResponseData.ok().putDataValue("result", fileList);
    }

    @PostMapping("deploy")
    @ApiOperation(value = "流程部署", notes = "流程部署")
    @ResponseBody
    public ResponseData deploy(@RequestParam("fileNames[]") List<String> fileNames, @RequestParam("uploadFlag") boolean uploadFlag){
        try{
            processService.deploy(fileNames, uploadFlag);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "流程部署失败！");
        }
    }

    @GetMapping("getProcessTaskDefKey/{taskId}")
    @ApiOperation(value = "获取流程运行的所有节点", notes = "获取流程运行的所有节点")
    @ResponseBody
    public ResponseData getProcessTaskDefKey(@PathVariable("taskId") String taskId){
        try{
            return ResponseData.ok().putDataValue("result", processService.getProcessTaskDefKey(taskId));
        }catch (Exception e){
            return ResponseData.ok().putDataValue("result", new ArrayList<>());
        }
    }

    @PostMapping("workupProcess")
    @ApiOperation(value = "唤醒流程", notes = "唤醒流程")
    @ResponseBody
    public ResponseData workupProcess(@RequestBody WorkupRequestParam workupRequestParam){
        try{
            processService.workupProcess(workupRequestParam.getWorkupId(), workupRequestParam.getWorkupName(), workupRequestParam.getProcessType(),
                    workupRequestParam.getProcessName(), workupRequestParam.getTaskDefKey(), workupRequestParam.getWorkupTaskId(),
                    workupRequestParam.getGatewayFlag(), AppUtil.getUser().getCompanyCode());
            return ResponseData.ok();
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "唤醒流程失败！");
        }
    }
}