package com.qinfei.qferp.service.impl.administrative;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.UserBusinessConclusion;
import com.qinfei.qferp.entity.administrative.UserBusinessPlan;
import com.qinfei.qferp.entity.media1.MediaAudit;
import com.qinfei.qferp.entity.media1.MediaExtendParamJson;
import com.qinfei.qferp.entity.media1.MediaForm1;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.administrative.AdministrativeMapper;
import com.qinfei.qferp.mapper.sys.DeptZwMapper;
import com.qinfei.qferp.service.administrative.IAdministrative;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.DataImportUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.OutputStream;
import java.util.*;

@Service
public class AdministrativeService implements IAdministrative {
    @Autowired
    private AdministrativeMapper administrativeMapper;
    @Autowired
    private IUserService userService;
    @Autowired
    private DeptZwMapper deptZwMapper;

    List<String> planExportCommonFieldName = Arrays.asList("出差标题","出差人","出差部门","出差申请日期","地点类型","出差地点","出差事由","出差交通工具","出差开始时间","出差结束时间","出差天数",
            "工作待办人","出差原因及目标","出差详细行程","备注","同行人员");
    List<String> planExportCommonField= Arrays.asList("title", "applyName","deptName",  "applicationDate", "placeType", "place", "reason", "traffic",
            "travelStateTime", "travelEndTime", "numberDay", "travelUser", "target", "trip", "costBudget", "note", "fieldUser");
    List<String> planConclusionFieldName = Arrays.asList("出差标题","出差人","出差部门","出差申请日期","出差开始时间","出差结束时间","出差总结");
    List<String> planConclusionField = Arrays.asList("title","applyName","deptName","applicationDate","travelStateTime","travelEndTime","conclusion");
    //地点
    public final static Map<Integer,String> placeType = new HashMap() {{
                put(1, "省内");
                put(2, "省外");
                put(3, "国外");
        }};
    //交通工具
    public final static Map<Integer,String> traffic = new HashMap() {{
        put(1, "公司派车");
        put(2, "飞机(经济舱)");
        put(3, "飞机(商务舱)");
        put(4, "高铁(一等座)");
        put(5, "高铁(二等座)");
        put(6, "高铁(特等座)");
        put(7, "火车(硬座)");
        put(8, "火车(硬卧)");
        put(9, "火车(软卧)");
        put(10, "轮船");
        put(11, "汽车");
    }};
    //
    public final static Map<Integer,String> reason = new HashMap() {{
        put(1, "公司派遣");
        put(2, "客户谈判");
        put(3, "培训学习");
        put(4, "商务会议");
        put(5, "工作拍摄");
        put(6, "其他");
    }};


    @Override
    public PageInfo<Administrative> administrativeList(Map<String, Object> params, Pageable pageable) {
        //对传递过来的数组进行处理
        User emp = AppUtil.getUser();

        List<Administrative> list = new ArrayList<>();
        //查看当前用户是否有行政部权限
        int flag = 0;
        List<Role> roles = emp.getRoles();
        emp.getIsMgr();
        for (Role role:roles) {
            if(IConst.ROLE_TYPE_XZ.equals(role.getType()) ||IConst.ROLE_TYPE_RS.equals(role.getType())){
                flag = 1;
            }else if(IConst.ROLE_CODE_ZW.equals(role.getCode()) && IConst.ROLE_CODE_ZW.equals(emp.getDept().getCode())){
                flag = 2;
            }else if (IConst.ROLE_CODE_BZ.equals(role.getCode()) && IConst.ROLE_TYPE_CW.equals(role.getType())){
                flag = 3;
            }
        }
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        if(flag==1){
            //有行政权限的可以查看所有人已经提交审批的数据和自己所有的数据
            if(params.get("empId")!=null){
                if(params.get("empId").equals(emp.getId())){
                    //如果搜索的是自己的
                    list = administrativeMapper.getList1(params);
                }else{
                    list = administrativeMapper.getList2(params);
                }
            }else{
                params.put("id",emp.getId());
            }
            params.put("companyCode",emp.getCompanyCode());
            list = administrativeMapper.getList(params);
        }else if(flag==2){
            //如果是政委角色，可以查看部门下的所有行政流程
            if(params.get("empId")!=null){
                if(params.get("empId").equals(emp.getId())){
                    //如果搜索的是自己的
                    list = administrativeMapper.getList1(params);
                }else{
                    list = administrativeMapper.getList2(params);
                }
            }else{
                params.put("id",emp.getId());
            }
            params.put("companyCode",emp.getCompanyCode());
            //获取部门和子部门
           /* String deptIds = "";
            Integer deptId =  emp.getDeptId();
            if (deptId != null) {
                deptIds = userService.getChilds(deptId);
                if (StringUtils.isEmpty(deptIds)) {
                    throw new QinFeiException(1003, "无法获取该用户所辖部门信息！");
                }
                deptIds = deptIds.replaceAll("\\$,", "");
                params.put("deptIds", deptIds);
            }*/
            //前台没有传递指定部门，则查询当前政委所管理的所有部门下人员，否则，查看当前部门及其子部门（政委管理的）
            String deptCode = params.get("deptCode") != null ? String.valueOf(params.get("deptCode")) : null;
            List<Map<String, Object>> deptList = deptZwMapper.listDeptInfoByParam(null, emp.getId(), deptCode);
            //当前政委没有绑定部门，则只查询自己的
            if(CollectionUtils.isEmpty(deptList)){
                //当前政委没有绑定对应部门，查看自己的和自己审批的流程
                params.put("empId",emp.getId());
                params.put("approverUserId",emp.getId());
                params.put("companyCode",emp.getCompanyCode());
                list = administrativeMapper.administrativeList(params);
            }else {
                //查询绑定部门的 和 自己审核的
                params.put("approverUserId",emp.getId());
                List<Integer> deptIds = new ArrayList<>();
                for(Map<String, Object> dept : deptList){
                    deptIds.add(Integer.parseInt(String.valueOf(dept.get("id"))));
                }
                params.put("deptIds", org.apache.commons.lang3.StringUtils.join(deptIds, ","));
                list = administrativeMapper.getList(params);
            }
        }
//        else if(flag ==3){
//            //如果是财务部长可以查看到所有的出差流程
//            //查看自己的和自己审批的流程
//            params.put("empId",emp.getId());
//            params.put("approverUserId",emp.getId());
//            params.put("companyCode",emp.getCompanyCode());
//            list = administrativeMapper.administrativeList(params);
//        }
        else if (flag == 0){
            if (params.get("empId")==null) {
                params.put("userId",emp.getId());
            }
            //只能查询到自己的 和 自己审核 的
            params.put("approverUserId",emp.getId());
            params.put("companyCode",emp.getCompanyCode());
            list = administrativeMapper.getList4(params);
        } else{
            //查看自己的和自己审批的流程
            params.put("empId",emp.getId());
            params.put("approverUserId",emp.getId());
            params.put("companyCode",emp.getCompanyCode());
            if (flag==3){
                params.put("cwbz",1);
            }
            list = administrativeMapper.administrativeList(params);

        }
        return new PageInfo<>(list);
    }
    @Override
    public PageInfo<Administrative> administrativeList1(Map<String, Object> params, Pageable pageable ,Integer type) {
        //对传递过来的数组进行处理
        User emp = AppUtil.getUser();
        if (!StringUtils.isEmpty(params.get("containsTime"))){
            String containsTime = params.get("containsTime").toString();
            params.put("containsTimeStart",containsTime.split("~")[0]);
            params.put("containsTimeEnd",containsTime.split("~")[1]);
        }
        List<Administrative> list = new ArrayList<>();
        //查看当前用户是否有行政部权限
        int flag = 0;
        List<Role> roles = emp.getRoles();
        emp.getIsMgr();
        for (Role role:roles) {
            if(IConst.ROLE_TYPE_XZ.equals(role.getType()) || IConst.ROLE_TYPE_RS.equals(role.getType()) ){
                flag = 1;
            }else if(IConst.ROLE_CODE_ZW.equals(role.getCode()) && IConst.ROLE_CODE_ZW.equals(emp.getDept().getCode()) ){
                flag = 2;
            }else if (IConst.ROLE_CODE_BZ.equals(role.getCode()) && IConst.ROLE_TYPE_CW.equals(role.getType())){
                flag = 3;
            }
        }

        if(flag==1){
            //有行政权限的可以查看所有人已经提交审批的数据和自己所有的数据
            if(params.get("empId")!=null){
                if(params.get("empId").equals(emp.getId())){
                    //如果搜索的是自己的
                    PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                    list = administrativeMapper.getList1(params);
                }else{
                    PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                    list = administrativeMapper.getList2(params);
                }
            }else{
                params.put("id",emp.getId());
            }
            params.put("companyCode",emp.getCompanyCode());
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            list = administrativeMapper.getList(params);
        }else if(flag==2){
            //如果是政委角色，可以查看部门下的所有行政流程
            if(params.get("empId")!=null){
                if(params.get("empId").equals(emp.getId())){
                    //如果搜索的是自己的
                    PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                    list = administrativeMapper.getList1(params);
                }else{
                    PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                    list = administrativeMapper.getList2(params);
                }
            }else{
                params.put("id",emp.getId());
            }
            params.put("companyCode",emp.getCompanyCode());
            //获取部门和子部门
            /*String deptIds = "";
            Integer deptId =  emp.getDeptId();
            if (deptId != null) {
                deptIds = userService.getChilds(deptId);
                if (StringUtils.isEmpty(deptIds)) {
                    throw new QinFeiException(1003, "无法获取该用户所辖部门信息！");
                }
                deptIds = deptIds.replaceAll("\\$,", "");
                params.put("deptIds", deptIds);
            }*/
            //前台没有传递指定部门，则查询当前政委所管理的所有部门下人员，否则，查看当前部门及其子部门（政委管理的）
            String deptCode = params.get("deptCode") != null ? String.valueOf(params.get("deptCode")) : null;
            List<Map<String, Object>> deptList = deptZwMapper.listDeptInfoByParam(null, emp.getId(), deptCode);
            //当前政委没有绑定部门，则只查询自己的
            if(CollectionUtils.isEmpty(deptList)){
                //当前政委没有绑定对应部门，查看自己的和自己审批的流程
                params.put("empId",emp.getId());
                params.put("approverUserId",emp.getId());
                params.put("companyCode",emp.getCompanyCode());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                list = administrativeMapper.administrativeList(params);
            }else {
                //查询绑定部门的 和 自己审核的
                params.put("approverUserId",emp.getId());
                List<Integer> deptIds = new ArrayList<>();
                for(Map<String, Object> dept : deptList){
                    deptIds.add(Integer.parseInt(String.valueOf(dept.get("id"))));
                }
                params.put("deptIds", org.apache.commons.lang3.StringUtils.join(deptIds, ","));
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                list = administrativeMapper.getList(params);
            }
        }else if (flag == 0){
            //只能查询到自己的 和 自己审核 的
            params.put("approverUserId",emp.getId());
            params.put("companyCode",emp.getCompanyCode());
            if (params.get("empId")==null) {
                params.put("userId",emp.getId());
            }
            if ( !StringUtils.isEmpty(params.get("empId"))&&params.get("empId").equals(emp.getId().toString())){
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                //如果搜索的是自己的
                list = administrativeMapper.getList1(params);
            }else {
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                list = administrativeMapper.getList4(params);
            }

        } else{
            if (type==1){
                if (params.get("empId")!=null) {
                    params.put("approverUserId",emp.getId());
                    params.put("companyCode",emp.getCompanyCode());
                    if (flag==3){
                        params.put("cwbz",1);
                    }
                    PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                    list = administrativeMapper.getList3(params);
                }else {
                    params.put("empId",emp.getId());
                    params.put("approverUserId",emp.getId());
                    params.put("companyCode",emp.getCompanyCode());
                    if (flag==3){
                        params.put("cwbz",1);
                    }
                    PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                    list = administrativeMapper.administrativeList(params);
                }
            }else{
                //查看自己的和自己审批的流程
                params.put("empId",emp.getId());
                params.put("approverUserId",emp.getId());
                params.put("companyCode",emp.getCompanyCode());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                list = administrativeMapper.administrativeList(params);
            }

        }
        return new PageInfo<>(list);
    }


    @Override
    public List<Administrative> exportList(Map<String, Object> params, OutputStream outputStream){
        //对传递过来的数组进行处理
        User emp = AppUtil.getUser();
        if (!StringUtils.isEmpty(params.get("containsTime"))){
            String containsTime = params.get("containsTime").toString();
            params.put("containsTimeStart",containsTime.split("~")[0]);
            params.put("containsTimeEnd",containsTime.split("~")[1]);
        }
        List<Administrative> list = new ArrayList<>();
        //查看当前用户是否有行政部权限
        boolean flag = false;
        List<Role> roles = emp.getRoles();
        emp.getIsMgr();
        for (Role role:roles) {
            if(role.getType().equals(IConst.ROLE_TYPE_XZ)||role.getType().equals(IConst.ROLE_TYPE_RS)){
                flag = true;
            }
        }
        if(flag){
            //有行政权限的可以查看所有人已经提交审批的数据和自己所有的数据
            params.put("id",emp.getId());
            params.put("companyCode",emp.getCompanyCode());
            list = administrativeMapper.getList1(params);
            String[] heads = {"类型","名称","开始时间","结束时间","时长","审批状态","审批结果",
                    "办结时间","创建人"};
            String[] fields={"administrativeName", "title", "beginTime", "endtime" ,"administrativeTime",
                    "approveState", "approveResult", "finishTime" ,"empName"};
            List<Administrative> finalList = list;
            ExcelUtil.exportExcel1("行政列表",heads,fields, list, outputStream,"yyyy-MM-dd HH:mm:ss",(sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (value != null) {
                     if ("beginTime".equals(field)||"endtime".equals(field)||"finishTime".equals(field)) {
                         cell.setCellValue(DateUtils.format((Date) value, DateUtils.DATE_FULL));
                     }else if ("approveState".equals(field)){
                         switch (finalList.get(rowIndex).getApproveState()){
                             case -1:
                                 cell.setCellValue("拒绝");
                                 break;
                             case 1:
                                 cell.setCellValue("审核中");
                                 break;
                             case 2:
                                 cell.setCellValue("审核完成");
                                 break;
                         }

                     }else if("administrativeTime".equals(field)){
                         if(finalList.get(rowIndex).getAdministrativeName().equals("出差") && finalList.get(rowIndex).getAdministrativeTime()!=null){
                                 cell.setCellValue(finalList.get(rowIndex).getAdministrativeTime()*8+"");
                         }else {
                             cell.setCellValue(value.toString());
                         }

                     }
                     else {
                        cell.setCellValue(value.toString());
                    }
                }
            });

        }else{
            //查看自己的和自己审批的流程
            if (StringUtils.isEmpty(params.get("empId"))){

                params.put("userId",emp.getId());
            }
            params.put("approverUserId",emp.getId());
            params.put("companyCode",emp.getCompanyCode());
            if (AppUtil.isRoleType(IConst.ROLE_TYPE_CW)&& AppUtil.isRoleCode(IConst.ROLE_CODE_BZ)){
                params.put("cwbz",1);
            }
            list = administrativeMapper.administrativeList(params);
            if(CollectionUtils.isEmpty(list)){
                throw new QinFeiException(1001,"未查询到数据");
            }
            String[] heads = {"类型","名称","开始时间","结束时间","时长","审批状态","审批结果",
                    "办结时间","创建人"};
            String[] fields={"administrativeName", "title", "beginTime", "endtime" ,"administrativeTime",
                    "approveState", "approveResult", "finishTime" ,"empName"};
            List<Administrative> finalList = list;
            ExcelUtil.exportExcel1("行政列表",heads,fields, list, outputStream,"yyyy-MM-dd HH:mm:ss",(sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (value != null) {
                    if ("beginTime".equals(field)||"endtime".equals(field)||"finishTime".equals(field)) {
//                        cell.setCellValue(value.toString());
                    cell.setCellValue(DateUtils.format((Date) value, DateUtils.DATE_FULL));
                    }else if ("approveState".equals(field)){
                        switch (finalList.get(rowIndex).getApproveState()){
                            case -1:
                                cell.setCellValue("拒绝");
                                break;
                            case 1:
                                cell.setCellValue("审核中");
                                break;
                            case 2:
                                cell.setCellValue("审核完成");
                                break;
                        }

                    }else if("administrativeTime".equals(field)){
                        if(finalList.get(rowIndex).getAdministrativeName().equals("出差") && finalList.get(rowIndex).getAdministrativeTime()!=null){
                            cell.setCellValue(finalList.get(rowIndex).getAdministrativeTime()*8+"");
                        }else {
                            cell.setCellValue(value.toString());
                        }

                    }
                    else {
                        cell.setCellValue(value.toString());
                    }
                }
            });
        }
        return  list;
    }


    /**
     *行政流程导出内容
     */
    public List<Map> exportContent(Map map,OutputStream outputStream){
        if (map.get("administrativeType") != null){
            Integer administrativeType = Integer.parseInt(String.valueOf(map.get("administrativeType")));
            User user = AppUtil.getUser();
            map.put("companyCode",user.getCompanyCode());
            if (!StringUtils.isEmpty(map.get("containsTime"))){
                String containsTime = map.get("containsTime").toString();
                map.put("containsTimeStart",containsTime.split("~")[0]);
                map.put("containsTimeEnd",containsTime.split("~")[1]);
            }
            //请假
            if (administrativeType ==1){
                List<Map> list = administrativeMapper.exportleaveContent(map);
                String[] heads ={"标题","请假类型","请假事由","开始时间","结束时间","请假时长","折合天数","申请人"};
                String[] fields ={"title","typeName","reason","beginTime","endTime","leaveTime","leaveDays","empName"};
                ExcelUtil.exportExcel("稿件信息", heads, fields, list, outputStream, "yyyy-MM-dd HH:mm:ss",
                        (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                    if (value !=null){
                    if( "beginTime".equals(field) || "endTime".equals(field) ){
                        cell.setCellValue(DateUtils.format((Date) value, DateUtils.DATE_FULL));
                    }else {
                        cell.setCellValue(value.toString());
                    }}
                });
            }
            //加班
            else if(administrativeType ==2){
                List<Map> list = administrativeMapper.exportworkOvertimeContent(map);
                String[] heads ={"标题","申请人","开始时间","结束时间","时长","加班原因"};
                String[] fields ={"title","empName","beginTime","endTime","workTime","reason"};
                ExcelUtil.exportExcel("稿件信息", heads, fields, list, outputStream, "yyyy-MM-dd HH:mm:ss",
                        (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                            if (value !=null){
                                if( "beginTime".equals(field) || "endTime".equals(field) ){
                                    cell.setCellValue(DateUtils.format((Date) value, DateUtils.DATE_FULL));
                                }else {
                                    cell.setCellValue(value.toString());
                                }}
                        });
            }
            //外出
            else if(administrativeType ==3){
                List<Map> list = administrativeMapper.exportGoOutContent(map);
                String[] heads ={"标题","申请人","开始时间","结束时间","外出时长","折合天数 ","外出事由"};
                String[] fields ={"title","empName","beginTime","endTime","time","days","reason"};
                ExcelUtil.exportExcel("稿件信息", heads, fields, list, outputStream, "yyyy-MM-dd HH:mm:ss",
                        (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                            if (value !=null){
                                if( "beginTime".equals(field) || "endTime".equals(field) ){
                                    cell.setCellValue(DateUtils.format((Date) value, DateUtils.DATE_FULL));
                                }else {
                                    cell.setCellValue(value.toString());
                                }}
                        });
            }
            //出差
            else if(administrativeType ==4){
                if(map.get("extendParams") != null) {
                    List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")), MediaExtendParamJson.class);
                    if (CollectionUtils.isNotEmpty(mediaExtendParamJsonList)) {
                        map.put("extendParams", mediaExtendParamJsonList);
                        map.put("extendNum", mediaExtendParamJsonList.size());
                    } else {
                        map.remove("extendParams");
                    }
                }
                    List<UserBusinessPlan> list = administrativeMapper.exportBusinessPlan(map);
                    List<Integer> planIds = new ArrayList<>(); //所有计划对应ID
                    List<Map<String, Object>> sheetInfo = new ArrayList<>(); //导出模板数据
                    if(CollectionUtils.isNotEmpty(list)){
                        for(UserBusinessPlan userBusinessPlan : list){
                            planIds.add(userBusinessPlan.getId());
                        }
                        handleMediaExport(planIds,sheetInfo);
                        //导出
                        DataImportUtil.createMoreSheetFile(sheetInfo, outputStream);
                    }

            }
        }
        return null;
    }

    private void handleMediaExport(List<Integer> planIds, List<Map<String, Object>> sheetInfo){
        Map param = new HashMap();
        param.put("ids",planIds);
        List<UserBusinessPlan> userBusinessPlansList = administrativeMapper.listByParam(param);
        List<Map<String, Object>> planMapList =new ArrayList<>();
        if(CollectionUtils.isNotEmpty(userBusinessPlansList)) {
            List<String> mediaRowTitles = new ArrayList<>();
            mediaRowTitles.addAll(planExportCommonFieldName);


            List<String> mediaRowTitleFields = new ArrayList<>(); //媒体导出模板列
            mediaRowTitleFields.addAll(planExportCommonField);
            for(UserBusinessPlan userBusinessPlan : userBusinessPlansList){

                Map<String, Object> plan = new HashMap<>();
                plan.put("title",userBusinessPlan.getTitle());
                plan.put("deptName",userBusinessPlan.getDeptName());
                plan.put("applicationDate",DateUtils.format(userBusinessPlan.getApplicationDate(),"yyyy-MM-dd HH:mm:ss"));
                plan.put("placeType",placeType.get(userBusinessPlan.getPlaceType()));
                plan.put("place",userBusinessPlan.getPlace());
                plan.put("reason",reason.get(userBusinessPlan.getReason()));
                plan.put("traffic",traffic.get(userBusinessPlan.getTraffic()));
                plan.put("travelStateTime",DateUtils.format(userBusinessPlan.getTravelStateTime(),"yyyy-MM-dd HH:mm:ss"));
                plan.put("travelEndTime",DateUtils.format(userBusinessPlan.getTravelEndTime(),"yyyy-MM-dd HH:mm:ss"));
                plan.put("numberDay",userBusinessPlan.getNumberDay());
                plan.put("travelUser",userBusinessPlan.getTravelUser());
                plan.put("target",userBusinessPlan.getTarget());
                plan.put("trip",userBusinessPlan.getTrip());
                plan.put("costBudget",userBusinessPlan.getCostBudget());
                plan.put("note",userBusinessPlan.getNote());
                plan.put("applyName",userBusinessPlan.getApplyName());
                plan.put("fieldUser",userBusinessPlan.getFieldUser());
                planMapList.add(plan);
            }
            List<Object[]> planBaseInfo = new ArrayList<>(); //媒体基本信息
            for(Map<String, Object> media : planMapList){
                Object[] objects = new Object[mediaRowTitleFields.size()];
                for(int i = 0; i < mediaRowTitleFields.size(); i++){
                    objects[i] = media.get(mediaRowTitleFields.get(i));
                }
                planBaseInfo.add(objects);
            }
            Map<String, Object> planMap = new HashMap<>();
            planMap.put("templateName","出差导出信息");
            planMap.put("rowTitles",mediaRowTitles);
            planMap.put("exportData",planBaseInfo);
            sheetInfo.add(planMap);
        }
        //获取出差总结
        Map<String, Object> param1 = new HashMap<>();
        param1.put("cid",planIds);
        List<UserBusinessPlan> userBusinessConclusions = administrativeMapper.listConclusion(param1);
        List<Map<String, Object>> planConclusionsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userBusinessConclusions)){
            List<String>  planConclusionTitles= new ArrayList<>();
            planConclusionTitles.addAll(planConclusionFieldName);

            List<String> planConclusionTitleFields = new ArrayList<>();
            planConclusionTitleFields.addAll(planConclusionField);
            for (UserBusinessPlan userBusinessPlan: userBusinessConclusions){
                Map<Integer, Map<String, Object>> temp = new HashMap<>(); //缓存一个媒体多个供应商信息
                Map<String, Object> plancon = new HashMap<>();
                plancon.put("title",userBusinessPlan.getTitle());
                plancon.put("applyName",userBusinessPlan.getApplyName());
                plancon.put("deptName",userBusinessPlan.getDeptName());
                plancon.put("applicationDate",DateUtils.format(userBusinessPlan.getApplicationDate(),"yyyy-MM-dd HH:mm:ss"));
                plancon.put("travelStateTime",DateUtils.format(userBusinessPlan.getTravelStateTime(),"yyyy-MM-dd HH:mm:ss"));
                plancon.put("travelEndTime",DateUtils.format(userBusinessPlan.getTravelEndTime(),"yyyy-MM-dd HH:mm:ss"));
                plancon.put("conclusion",userBusinessPlan.getConclusion());
                temp.put(userBusinessPlan.getId(),plancon);
                planConclusionsList.addAll(temp.values());
            }
            List<Object[]> conclusionList = new ArrayList<>();
            for(Map<String, Object> mediaSupplier : planConclusionsList){
                Object[] objects = new Object[planConclusionTitleFields.size()];
                for(int i = 0; i < planConclusionTitleFields.size(); i++){
                    objects[i] = mediaSupplier.get(planConclusionTitleFields.get(i));
                }
                conclusionList.add(objects);
            }
            Map<String, Object> mediaSupplierMap = new HashMap<>();
            mediaSupplierMap.put("templateName","出差总结导出信息");
            mediaSupplierMap.put("rowTitles",planConclusionTitles);
            mediaSupplierMap.put("exportData",conclusionList);
            sheetInfo.add(mediaSupplierMap);

        }
    }

    @Override
    public Administrative getById(Integer id) {
        return administrativeMapper.selectByPrimaryKey(id);
    }
}
