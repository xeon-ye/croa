package com.qinfei.qferp.service.impl.biz.statistics;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.biz.statistics.BusinessStatisticsMapper;
import com.qinfei.qferp.mapper.fee.IncomeMapper;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.biz.statistics.IBusinessStatisticsService;
import com.qinfei.qferp.service.impl.BaseService;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.SysConfigUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.*;

@Service
public class BusinessStatisticsService extends BaseService implements IBusinessStatisticsService {
    public static final String cacheKey = "businessStatistics";

    @Autowired
    BusinessStatisticsMapper businessStatisticsMapper;
    @Autowired
    UserService userService;
    @Autowired
    private DeptMapper deptMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IncomeMapper incomeMapper;

    // @Cacheable(value=cacheKey)
/*    @Override
    public List<Map<String, Object>> statisticsResult(Map map) {
        this.addSecurityByFore(map);
        return businessStatisticsMapper.statisticsResult(map);
    }*/

    @Override
    public Map<String, Object> businessStatisticsResult(Map map) {
        this.addSecurityByFore(map);
        Map businessStatistics = businessStatisticsMapper.getBusinessStatisticsByParam(map);
        List<Map<String, Object>> trend = businessStatisticsMapper.listBusinessTrendStatisticsByParam(map);
        List<Map<String, Object>> mediaTypeRate = businessStatisticsMapper.listBusinessMediaTypeStatisticsByParam(map);
        List<Map<String, Object>> artTypeRate = businessStatisticsMapper.listBusinessArtTypeStatisticsByParam(map);
        map.put("custType", 1);
        List<Map<String, Object>> custTypeRate = businessStatisticsMapper.listBusinessCustTypeStatisticsByParam(map);
        map.remove("custType"); //移除客户公司类型
        map.put("custIndustryType", 1); //添加客户行业类型
        List<Map<String, Object>> custIndustryTypeRate = businessStatisticsMapper.listBusinessCustIndustryTypeStatisticsByParam(map);
        Map<String, Object> result = new HashMap<>();
        result.put("businessStatistics", businessStatistics);
        result.put("trend", trend);
        result.put("mediaTypeRate", mediaTypeRate);
        result.put("custTypeRate", custTypeRate);
        result.put("custIndustryType", custIndustryTypeRate);
        result.put("artTypeRate", artTypeRate);
        return result;
    }

    @Override
    public Map<String, Object> zwBusinessStatisticsResult(Map map) {
        Map<String, Object> result = new HashMap<>();
        try{
            map.put("deptCode", "YW");
            this.addSecurityByZw(map);
            Map businessStatistics = businessStatisticsMapper.getBusinessStatisticsByParam(map);
            List<Map<String, Object>> trend = businessStatisticsMapper.listBusinessTrendStatisticsByParam(map);
            result.put("businessStatistics", businessStatistics);
            result.put("trend", trend);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public PageInfo<Map> listCustForNotTrans(Integer pageNum, Integer pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        this.addSecurityByFore(map);
        List<Map> list = businessStatisticsMapper.listCustForNotTrans(map);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Map<String, Object>> statisticsRanking(Map map, Integer pageNum, Integer pageSize) {
        this.addSecurity(map);
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<Map<String, Object>>(businessStatisticsMapper.statisticsRanking(map));
    }

    @Override
    public List<Map<String, Object>> everyDeptBusiness(List<Integer> depts, Map<String, Object> param) {
        List<Dept> deptList = deptMapper.listById(depts);
        List<Map<String, Object>> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(deptList)) {
            for (Dept dept : deptList) {
                String deptIds = userMapper.getChilds(dept.getId());
                if (deptIds.indexOf("$,") > -1) {
                    deptIds = deptIds.substring(2);
                }
                param.put("deptIds", deptIds);
                Map<String, Object> deptResult = businessStatisticsMapper.getDeptStatisticsByDeptId(param);
                deptResult.put("deptId", dept.getId());
                deptResult.put("deptName", dept.getName());
                result.add(deptResult);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> businessTop(Map<String, Object> map) {
        User user = AppUtil.getUser();
        Integer deptId = MapUtils.getInteger(map, "currentDeptId");//页面选择的部门
        deptId = deptId == null ? user.getDeptId() : deptId;
        String deptIds = "";
        if (deptId != null) {
            deptIds = userMapper.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        List<Map<String, Object>> list = businessStatisticsMapper.businessTop(map);
        return list;
    }

    @Override
    public PageInfo<Map<String, Object>> queryNotIncome(int pageNum, int pageSize, Map map) {
        List<Map<String, Object>> list = doNotIncome(pageNum, pageSize, map, true);
        return new PageInfo<>(list);
    }

    @Override
    public List<Map<String, Object>> exportNotIncome(Map map, OutputStream outputStream) {
        Integer type = MapUtils.getInteger(map, "type");//从页面选择的类型
        Integer monthStart=0;
        Integer monthEnd=0;
        try{
            monthStart = MapUtils.getInteger(map,"monthStart");
            monthEnd = MapUtils.getInteger(map,"monthEnd");
        }catch (Exception e){
            throw new QinFeiException(1002,"获取开始结束月份失败：");
        }
        if(monthStart==0 || monthEnd==0|| monthStart>monthEnd){
            throw new QinFeiException(1002,"开始结束月份不正确。");
        }
        Integer size = monthEnd - monthStart + 2 ;
        String[] months = new String[size];
        String[] monthCns = new String[size];
        for(int i=0;i<size;i++){
            if(i==size-1){
                months[i]="sum";
                monthCns[i]="年度";
            }else{
                months[i]=String.valueOf(monthStart+i);
                monthCns[i]=String.valueOf(monthStart+i).concat("月");
            }
        }
        String[] columns = {"sale", "profit", "notIncome", "yqIncome"};
        ArrayList<String> fieldList = new ArrayList();

        String[] columnCns = {"业绩", "利润", "未到款", "逾期款"};
        ArrayList<String> headList = new ArrayList();
        for (String monthCn : monthCns) {
            for (String columnCn : columnCns) {
                String key = monthCn.concat(columnCn);
                headList.add(key);
            }
        }

        Map sumMap = new HashMap<String, Object>();//合计列数据
        //type=1业务部 type=2业务组 type=3业务员 type=4客户
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        if(type != null && type == 3){
            List<Map<String, Object>> userList = doNotIncome(1, 1, map, false);
            for (String month : months) {
                for (String column : columns) {
                    String key = column.concat("_").concat(month);
                    fieldList.add(key);
                    for (Map temp : userList) {
                        if (temp.containsKey(key)) {
                            Double value = MapUtils.getDouble(temp, key);
                            if (sumMap.containsKey(key)) {
                                Double old = MapUtils.getDouble(sumMap, key);
                                sumMap.put(key, value + old);
                            } else {
                                sumMap.put(key, value);
                            }
                        }
                    }
                }
            }
            sumMap.put("deptName", "");
            sumMap.put("userName", "合计");
            userList.add(sumMap);

            headList.add(0, "部门名称");
            headList.add(1, "业务员");
            String[] heads = new String[headList.size()];
            headList.toArray(heads);

            fieldList.add(0, "deptName");
            fieldList.add(1, "userName");
            String[] fields = new String[fieldList.size()];
            fieldList.toArray(fields);
            ExcelUtil.exportExcel("未到款统计", heads, fields, userList, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (value != null) {
                    if ("deptName".equals(field) || "userName".equals(field)) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    }
                } else {
                    cell.setCellValue(0);
                }
            });
            return userList;
        }else if(type != null && type == 4){
            List<Map<String, Object>> userList = doNotIncome(1, 1, map, false);
            for (String month : months) {
                for (String column : columns) {
                    String key = column.concat("_").concat(month);
                    fieldList.add(key);
                    for (Map temp : userList) {
                        if (temp.containsKey(key)) {
                            Double value = MapUtils.getDouble(temp, key);
                            if (sumMap.containsKey(key)) {
                                Double old = MapUtils.getDouble(sumMap, key);
                                sumMap.put(key, value + old);
                            } else {
                                sumMap.put(key, value);
                            }
                        }
                    }
                }
            }
            sumMap.put("deptName", "");
            sumMap.put("userName", "");
            sumMap.put("companyName", "");
            sumMap.put("custName", "合计");
            userList.add(sumMap);

            headList.add(0, "部门名称");
            headList.add(1, "业务员");
            headList.add(2, "客户公司");
            headList.add(3, "客户对接人");
            String[] heads = new String[headList.size()];
            headList.toArray(heads);

            fieldList.add(0, "deptName");
            fieldList.add(1, "userName");
            fieldList.add(2, "companyName");
            fieldList.add(3, "custName");
            String[] fields = new String[fieldList.size()];
            fieldList.toArray(fields);
            //如果用户是项目总监，则只能看到自己客户的对接人
            List<String> PD= SysConfigUtils.getConfigValue("projectDirector",List.class);

            Integer userId = AppUtil.getUser().getId();


            ExcelUtil.exportExcel("未到款统计", heads, fields, userList, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (value != null) {
                    if ("deptName".equals(field) || "userName".equals(field) || "companyName".equals(field)  ) {
                        cell.setCellValue(value.toString());
                    }else if ("custName".equals(field)){
                        boolean b = false;
                        boolean a = false;
                        if (CollectionUtils.isNotEmpty(PD) && PD.contains(userId.toString())){
                           b = true;
                        }
                        if (b){
                            if( b && !StringUtils.isEmpty(userList.get(rowIndex).get("userId")) &&  !userId.equals(userList.get(rowIndex).get("userId"))){
                                a = true;
                            }
                            cell.setCellValue(a ? "***":value.toString());
                        }else {
                            cell.setCellValue(value.toString());
                        }
                    }else {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    }
                } else {
                    cell.setCellValue(0);
                }
            });
            return userList;
        }else {
            List<Map<String, Object>> userList = doNotIncome(1, 1, map, false);
            String[] selfColumns = {"selfSale", "selfNotIncome", "selfYqIncome", "selfProfit"};
            for (String month : months) {
                for(int i = 0; i < columns.length; i++){
                    String column = columns[i];
                    String key = column.concat("_").concat(month);
                    String selfKey = selfColumns[i].concat("_").concat(month);
                    fieldList.add(key);
                    for (Map temp : userList) {
                        if (temp.containsKey(selfKey)) {
                            Double value = MapUtils.getDouble(temp, selfKey);
                            if (sumMap.containsKey(key)) {
                                Double old = MapUtils.getDouble(sumMap, key);
                                sumMap.put(key, decimalFormat.format(value + old));
                            } else {
                                sumMap.put(key, decimalFormat.format(value));
                            }
                        }
                    }
                }
            }
            sumMap.put("deptName", "合计");
            userList.add(sumMap);

            headList.add(0, "部门名称");
            String[] heads = new String[headList.size()];
            headList.toArray(heads);

            fieldList.add(0, "deptName");
            String[] fields = new String[fieldList.size()];
            fieldList.toArray(fields);

            ExcelUtil.exportExcel("未到款统计", heads, fields, userList, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (value != null) {
                    if ("deptName".equals(field)) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    }
                } else {
                    cell.setCellValue(0);
                }
            });
            return userList;
        }
    }

    private List<Map<String, Object>> doNotIncome(Integer pageNum, Integer pageSize, Map map, Boolean flag) {
        Integer type = MapUtils.getInteger(map, "type");//从页面选择的类型
        User user = AppUtil.getUser();
        if (ObjectUtils.isEmpty(user)) {
            return null;
        }

        map = doWithPermission(map, user);

        if(type != null  &&  type == 3){
            //查询到的详情，按月分组
            List<Map<String, Object>> list = businessStatisticsMapper.queryNotIncome(map);
            List<User> YWList = userMapper.listByType2(IConst.ROLE_TYPE_YW);
            if (flag) {
                PageHelper.startPage(pageNum, pageSize);
            }
            //返回的列表
            List<Map<String, Object>> list2 = businessStatisticsMapper.queryAllByType(map);
            List<Map> amountList = incomeMapper.queryNotAssignAmount(user.getCompanyCode());
            for (Map result : list2) {
                for (Map<String, Object> temp2 : list) {
                    if (result.get("userId").equals(temp2.get("userId"))) {
                        result = doWithMonth(temp2, result);
                    }
                }
                for (Map<String, Object> temp3 : amountList) {
                    if (result.get("userId").equals(temp3.get("userId"))) {
                        result.put("remainAmountSum", temp3.get("remainAmountSum"));
                    }
                }
            }
            for(Map results:list2){
                //防止业务员换部门导致的部门名称不一致问题，重新查一遍放入list
                for(User temp:YWList){
                    if(temp.getId().equals(results.get("userId"))){
                        results.put("deptId",temp.getDeptId());
                        results.put("deptName",temp.getDeptName());
                        break;
                    }
                }
            }
            return list2;
        }else if(type != null  &&  type == 4){
            //查询到的详情，按月分组
            List<Map<String, Object>> list = businessStatisticsMapper.queryNotIncome(map);
            List<User> YWList = userMapper.listByType2(IConst.ROLE_TYPE_YW);
            if (flag) {
                PageHelper.startPage(pageNum, pageSize);
            }
            //返回的列表
            List<Map<String, Object>> list2 = businessStatisticsMapper.queryAllByType(map);
            for (Map<String, Object> result : list2) {
                for (Map<String, Object> temp2 : list) {
                    if (result.get("custId").equals(temp2.get("custId")) && result.get("userId").equals(temp2.get("userId"))) {
                        result = doWithMonth(temp2, result);
                    }
                }
            }
            for (Map<String, Object> result : list2) {
                //防止业务员换部门导致的部门名称不一致问题，重新查一遍放入list
                for(User temp:YWList){
                    if(temp.getId().equals(result.get("userId"))){
                        result.put("deptName",temp.getDeptName());
                        result.put("deptId",temp.getDeptId());
                        break;
                    }
                }
            }
            return list2;
        }else {
            List<Map<String, Object>> list = businessStatisticsMapper.listAllNotIncome(map);
            return handlerNotIncomeTree(Integer.parseInt(String.valueOf(map.get("monthStart"))), Integer.parseInt(String.valueOf(map.get("monthEnd"))), list);
        }


        /*//查询到的详情，按月分组
        List<Map<String, Object>> list = businessStatisticsMapper.queryNotIncome(map);
        //type=1业务部 type=2业务组 type=3业务员 type=4客户
        if (type == 1) {
            if (flag) {
                PageHelper.startPage(pageNum, pageSize);
            }
            //返回的汇总列表
            List<Map> parentList = businessStatisticsMapper.queryAllParentByType(map);
            for (Map result : parentList) {
                for (Map temp2 : list) {
                    if (result.get("deptId").equals(temp2.get("deptId"))) {
                        result = doWithMonth(temp2, result);
                    }
                    if (result.get("deptId").equals(temp2.get("parentDeptId"))) {
                        result = doWithMonth(temp2, result);
                    }
                }
            }
            return parentList;
        } else if (type == 2) {
            if (flag) {
                PageHelper.startPage(pageNum, pageSize);
            }
            //返回的汇总列表
            map.put("level", 4);
            List<Map> childList = businessStatisticsMapper.queryAllByType(map);

            for (Map result : childList) {
                for (Map temp2 : list) {
                    if (result.get("deptId").equals(temp2.get("deptId"))) {
                        result = doWithMonth(temp2, result);
                    }
                }
            }
            return childList;
        } else if (type == 3) {
            List<User> YWList = userMapper.listByType2(IConst.ROLE_TYPE_YW);
            if (flag) {
                PageHelper.startPage(pageNum, pageSize);
            }
            //返回的列表
            List<Map> list2 = businessStatisticsMapper.queryAllByType(map);
            List<Map> amountList = incomeMapper.queryNotAssignAmount(user.getCompanyCode());
            for (Map result : list2) {
                for (Map<String, Object> temp2 : list) {
                    if (result.get("userId").equals(temp2.get("userId"))) {
                        result = doWithMonth(temp2, result);
                    }
                }
                for (Map<String, Object> temp3 : amountList) {
                    if (result.get("userId").equals(temp3.get("userId"))) {
                        result.put("remainAmountSum", temp3.get("remainAmountSum"));
                    }
                }
            }
            for(Map results:list2){
                //防止业务员换部门导致的部门名称不一致问题，重新查一遍放入list
                for(User temp:YWList){
                    if(temp.getId().equals(results.get("userId"))){
                        results.put("deptId",temp.getDeptId());
                        results.put("deptName",temp.getDeptName());
                        break;
                    }
                }
            }
            return list2;
        } else if (type == 4) {
            List<User> YWList = userMapper.listByType2(IConst.ROLE_TYPE_YW);
            if (flag) {
                PageHelper.startPage(pageNum, pageSize);
            }
            //返回的列表
            List<Map> list2 = businessStatisticsMapper.queryAllByType(map);
            for (Map<String, Object> result : list2) {
                for (Map<String, Object> temp2 : list) {
                    if (result.get("custId").equals(temp2.get("custId")) && result.get("userId").equals(temp2.get("userId"))) {
                        result = doWithMonth(temp2, result);
                    }
                }
            }
            for (Map<String, Object> result : list2) {
                //防止业务员换部门导致的部门名称不一致问题，重新查一遍放入list
                for(User temp:YWList){
                    if(temp.getId().equals(result.get("userId"))){
                        result.put("deptName",temp.getDeptName());
                        result.put("deptId",temp.getDeptId());
                        break;
                    }
                }
            }
            return list2;
        }

        return null;*/
    }

    //处理未到款统计部门树
    private List<Map<String, Object>> handlerNotIncomeTree(int monthStart, int monthEnd, List<Map<String, Object>> list){
        List<Map<String, Object>> result = new ArrayList<>();
        //查询List转换成Map
        Map<String, Map<String, Object>> tempMap = new HashMap<>();
        //查询出来的部门月份数据组合到一个对象
        List<String> deptList = new ArrayList<>();//记录部门按照层级倒序的顺序，并排除重复的
        for(Map<String, Object> notIncomeMap : list){
            //由于数据存在许多level为空的无效数据，所以需要排除
            if(notIncomeMap.get("level") != null){
                String deptId = String.valueOf(notIncomeMap.get("id"));
                notIncomeMap.put("isLeaf", true); //默认所有节点都是叶子节点
                notIncomeMap.put("expanded", false); //默认不展开
                if(!deptList.contains(deptId)){
                    deptList.add(deptId);//记录部门按照层级倒序的顺序，并排除重复的
                }

                //将同一个部门每个月的月份金额
                if(!tempMap.containsKey(deptId)){
                    tempMap.put(deptId, notIncomeMap);
                }

                if(notIncomeMap.get("month") != null){
                    if(!tempMap.get(deptId).containsKey("monthMoney")){
                        tempMap.get(deptId).put("monthMoney", new HashMap<>());
                    }
                    Map<String, Float> moneyMap = new HashMap<>();
                    moneyMap.put("sale", Float.parseFloat(String.valueOf(notIncomeMap.get("sale"))));
                    moneyMap.put("notIncome", Float.parseFloat(String.valueOf(notIncomeMap.get("notIncome"))));
                    moneyMap.put("yqIncome", Float.parseFloat(String.valueOf(notIncomeMap.get("yqIncome"))));
                    moneyMap.put("profit", Float.parseFloat(String.valueOf(notIncomeMap.get("profit"))));
                    ((Map<String, Object>)tempMap.get(deptId).get("monthMoney")).put(String.valueOf(notIncomeMap.get("month")), moneyMap);
                }
            }
        }
        //将月份数据封装出来
        for(String deptId : tempMap.keySet()){
            Map<String, Object> notIncomeMap = tempMap.get(deptId);
            Map<String, Object> monthMoney =  ((Map<String, Object>)notIncomeMap.get("monthMoney"));
            for(int i = monthStart; i <= monthEnd; i++){
                Float sale = 0f;
                Float notIncome = 0f;
                Float yqIncome = 0f;
                Float profit = 0f;
                //如果存在这个月的金额数据，则取出来，否则设置为0
                if(monthMoney != null && monthMoney.containsKey(String.valueOf(i))){
                    Map<String, Float> moneyMap = (Map<String, Float>) monthMoney.get(String.valueOf(i));
                    sale = moneyMap.get("sale") != null ? moneyMap.get("sale") : 0f;
                    notIncome = moneyMap.get("notIncome") != null ? moneyMap.get("notIncome") : 0f;
                    yqIncome = moneyMap.get("yqIncome") != null ? moneyMap.get("yqIncome") : 0f;
                    profit = moneyMap.get("profit") != null ? moneyMap.get("profit") : 0f;
                }
                notIncomeMap.put(String.format("selfSale_%s", i), sale);//单节点金额
                notIncomeMap.put(String.format("sale_%s", i), sale);//汇总金额，后面处理会含有其子节点的金额
                notIncomeMap.put(String.format("selfNotIncome_%s", i), notIncome);//单节点金额
                notIncomeMap.put(String.format("notIncome_%s", i), notIncome);//汇总金额，后面处理会含有其子节点的金额
                notIncomeMap.put(String.format("selfYqIncome_%s", i), yqIncome);//单节点金额
                notIncomeMap.put(String.format("yqIncome_%s", i), yqIncome);//汇总金额，后面处理会含有其子节点的金额
                notIncomeMap.put(String.format("selfProfit_%s", i), profit);//单节点金额
                notIncomeMap.put(String.format("profit_%s", i), profit);//汇总金额，后面处理会含有其子节点的金额
            }
            //移除无效字段
            notIncomeMap.remove("sale");
            notIncomeMap.remove("notIncome");
            notIncomeMap.remove("yqIncome");
            notIncomeMap.remove("profit");
            notIncomeMap.remove("month");
            notIncomeMap.remove("monthMoney");
        }

        //统计子部门数据到父级部门
        List<Map<String, Object>> sourceList = new ArrayList<>();
        Iterator<String> iterator = deptList.iterator();
        while (iterator.hasNext()){
            String deptId = iterator.next();
            Map<String, Object> notIncomeMap = tempMap.get(deptId);
            //由于数据存在许多level为空的无效数据，所以需要排除
            if(notIncomeMap.get("level") != null){
                String parentId = String.valueOf(notIncomeMap.get("parent"));
                //计算当前部门节点月份金额合计
                Float sale_sum = 0f;
                Float notIncome_sum = 0f;
                Float yqIncome_sum = 0f;
                Float profit_sum = 0f;
                Float self_sale_sum = 0f;
                Float self_notIncome_sum = 0f;
                Float self_yqIncome_sum = 0f;
                Float self_profit_sum = 0f;
                //月份金额合计
                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                for(int i = monthStart; i <= monthEnd; i++){
                    sale_sum += Float.parseFloat(String.valueOf(notIncomeMap.get(String.format("sale_%s", i))));//含子部门的月份金额合计
                    self_sale_sum += Float.parseFloat(String.valueOf(notIncomeMap.get(String.format("selfSale_%s", i))));//单节点金额月份金额合计
                    notIncome_sum += Float.parseFloat(String.valueOf(notIncomeMap.get(String.format("notIncome_%s", i))));//含子部门的月份金额合计
                    self_notIncome_sum += Float.parseFloat(String.valueOf(notIncomeMap.get(String.format("selfNotIncome_%s", i))));//单节点金额月份金额合计
                    yqIncome_sum += Float.parseFloat(String.valueOf(notIncomeMap.get(String.format("yqIncome_%s", i))));//含子部门的月份金额合计
                    self_yqIncome_sum += Float.parseFloat(String.valueOf(notIncomeMap.get(String.format("selfYqIncome_%s", i))));//单节点金额月份金额合计
                    profit_sum += Float.parseFloat(String.valueOf(notIncomeMap.get(String.format("profit_%s", i))));//含子部门的月份金额合计
                    self_profit_sum += Float.parseFloat(String.valueOf(notIncomeMap.get(String.format("selfProfit_%s", i))));//单节点金额月份金额合计
                    notIncomeMap.put("sale_sum", decimalFormat.format(sale_sum));
                    notIncomeMap.put("notIncome_sum", decimalFormat.format(notIncome_sum));
                    notIncomeMap.put("yqIncome_sum", decimalFormat.format(yqIncome_sum));
                    notIncomeMap.put("profit_sum", decimalFormat.format(profit_sum));
                    notIncomeMap.put("selfSale_sum", decimalFormat.format(self_sale_sum));
                    notIncomeMap.put("selfNotIncome_sum", decimalFormat.format(self_notIncome_sum));
                    notIncomeMap.put("selfYqIncome_sum", decimalFormat.format(self_yqIncome_sum));
                    notIncomeMap.put("selfProfit_sum", decimalFormat.format(self_profit_sum));

                    //如果直属父级部门被查询出来了，则把子部门金额数据统计起来
                    if(tempMap.containsKey(parentId)){
                        //将子节点的月份金额汇总到父节点
                        tempMap.get(parentId).put(String.format("sale_%s", i), String.valueOf(Float.parseFloat(String.valueOf(tempMap.get(parentId).get(String.format("sale_%s", i)))) + Float.parseFloat(String.valueOf(tempMap.get(deptId).get(String.format("sale_%s", i))))));
                        tempMap.get(parentId).put(String.format("notIncome_%s", i), String.valueOf(Float.parseFloat(String.valueOf(tempMap.get(parentId).get(String.format("notIncome_%s", i)))) + Float.parseFloat(String.valueOf(tempMap.get(deptId).get(String.format("notIncome_%s", i))))));
                        tempMap.get(parentId).put(String.format("yqIncome_%s", i), String.valueOf(Float.parseFloat(String.valueOf(tempMap.get(parentId).get(String.format("yqIncome_%s", i)))) + Float.parseFloat(String.valueOf(tempMap.get(deptId).get(String.format("yqIncome_%s", i))))));
                        tempMap.get(parentId).put(String.format("profit_%s", i), String.valueOf(Float.parseFloat(String.valueOf(tempMap.get(parentId).get(String.format("profit_%s", i)))) + Float.parseFloat(String.valueOf(tempMap.get(deptId).get(String.format("profit_%s", i))))));
                    }
                }

                //找到父节点
                if(tempMap.containsKey(parentId)){
                    //设置父节点不为叶子节点
                    tempMap.get(parentId).put("isLeaf", false);
                    //将子节点包含进去
                    if(!tempMap.get(parentId).containsKey("nodes")){
                        tempMap.get(parentId).put("nodes", new ArrayList<>());
                    }
                    ((List<Map<String, Object>>)tempMap.get(parentId).get("nodes")).add(notIncomeMap);
                    iterator.remove();//列表移除当前节点
                }else {
                    sourceList.add(notIncomeMap);//没有找到父节点将就记录当前节点
                }
            }
        }

        tree2List(0, sourceList, result);
        return result;
    }

    private Map doWithMonth(Map temp2, Map result) {
        if (temp2.containsKey("MONTH")) {
            Integer[] months = new Integer[12];
            for(int i=0;i<12;i++){
                months[i] = i+1;
            }
            String[] moneyStr = new String[4];
            moneyStr[0] = "sale";
            moneyStr[1] = "notIncome";
            moneyStr[2] = "yqIncome";
            moneyStr[3] = "profit";
            for(int j=0;j<months.length;j++){
                for(int k=0;k<moneyStr.length;k++){
                    if(months[j].equals(MapUtils.getInteger(temp2,"MONTH"))){
                        StringBuffer sb = new StringBuffer();
                        sb.append(moneyStr[k]).append("_").append(months[j]);
                        String key = sb.toString();
//                        System.out.println("***key="+key+"\t new="+MapUtils.getDouble(result,key)+"\t old="+temp2.get(moneyStr[k]));
                        if(result.containsKey(key)){
                            result.put(key,MapUtils.getDouble(temp2,moneyStr[k])+MapUtils.getDouble(result,key));
                        }else{
                            result.put(key,temp2.get(moneyStr[k]));
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Map querySumNotIncome(Map map) {
        User user = AppUtil.getUser();
        if (ObjectUtils.isEmpty(user)) {
            return null;
        }
        map = doWithPermission(map, user);
        Integer type = MapUtils.getInteger(map, "type");//从页面选择的类型
        if (type == 2) {
            map.put("level", 4);
        }
        return businessStatisticsMapper.querySumNotIncome(map);
    }

    private Map doWithPermission(Map map, User user) {
        Integer deptId = MapUtils.getInteger(map, "currentDeptId");//页面选择的部门
        String deptIds = "";
        List<Role> roles = user.getRoles();
        Integer typeQx = 0;//权限类型，放在where子句,type=1
        for (Role role : roles) {
            if (IConst.ROLE_TYPE_YW.equals(role.getType())) {
                map.put("qxCompanyCode", user.getCompanyCode());
                deptId = deptId == null ? user.getDeptId() : deptId;
                if (deptId != null) {
                    deptIds = userMapper.getChilds(deptId);
                    if (deptIds.indexOf("$,") > -1) {
                        deptIds = deptIds.substring(2);
                    }
                    map.put("deptIds", deptIds);
                }
                String code = role.getCode();
                switch (code) {
                    case "ZL"://助理
                        typeQx = 1;
                        map.put("userId", user.getId());
                        break;
                    case "YG"://业务员
                        typeQx = 1;
                        map.put("userId", user.getId());
                        break;
                    case "ZZ"://组长
                        typeQx = 2;
                        map.put("level", user.getDept().getLevel());
                        break;
                    case "BZ"://部长
                        typeQx = 2;
                        map.put("level", user.getDept().getLevel());
                        break;
                    case "ZJ"://总监
                        typeQx = 3;
                        break;
                    default:
                        break;
                }
            } else if (IConst.ROLE_TYPE_JT.equals(role.getType())
                    || IConst.ROLE_TYPE_ZJB.equals(role.getType())
                    || IConst.ROLE_TYPE_CW.equals(role.getType())) {
                if (!IConst.ROLE_TYPE_JT.equals(role.getType())) {
                    map.put("qxCompanyCode", user.getCompanyCode());
                }
                if (deptId != null) {
                    deptIds = userMapper.getChilds(deptId);
                    if (deptIds.indexOf("$,") > -1) {
                        deptIds = deptIds.substring(2);
                    }
                    map.put("deptIds", deptIds);
                }
                typeQx = 3;
            }
        }
        map.put("typeQx", typeQx);
        return map;
    }

    @Override
    public PageInfo<Map<String, Object>> queryNotIncomeYear(int pageNum, int pageSize, Map map) {
        List<Map<String, Object>> list = doNotIncomeYear(pageNum, pageSize, map, true);
        return new PageInfo<>(list);
    }

    private List<Map<String, Object>> doNotIncomeYear(Integer pageNum, Integer pageSize, Map map, Boolean flag) {
        Integer thisYear = MapUtils.getInteger(map, "year");
        Integer lastYear = thisYear - 1;
        User user = AppUtil.getUser();
        if (ObjectUtils.isEmpty(user)) {
            return null;
        }
        Integer type = MapUtils.getInteger(map, "type");//从页面选择的类型

        doWithPermission(map, user);

        //type=1业务部 type=2业务组 type=3业务员 type=4客户
        if(type != null && type == 3){
            List<Map<String, Object>> list = businessStatisticsMapper.queryNotIncomeYear(map);
            if (flag) {
                PageHelper.startPage(pageNum, pageSize);
            }
            //返回的列表
            List<Map<String, Object>> list2 = businessStatisticsMapper.queryAllByType(map);
            for (Map<String, Object> result : list2) {
                for (Map<String, Object> temp2 : list) {
                    if (result.get("userId").equals(temp2.get("userId"))) {
                        result = doWithYear(temp2, result, thisYear, lastYear);
                    }
                }
            }
            List<User> YWList = userMapper.listByType2(IConst.ROLE_TYPE_YW);
            for(Map<String, Object> results:list2){
                //防止业务员换部门导致的部门名称不一致问题，重新查一遍放入list
                for(User temp:YWList){
                    if(temp.getId().equals(results.get("userId"))){
                        results.put("deptId",temp.getDeptId());
                        results.put("deptName",temp.getDeptName());
                        break;
                    }
                }
            }
            return list2;
        }else if(type != null && type == 4){
            List<Map<String, Object>> list = businessStatisticsMapper.queryNotIncomeYear(map);
            if (flag) {
                PageHelper.startPage(pageNum, pageSize);
            }
            //返回的列表
            List<Map<String, Object>> list2 = businessStatisticsMapper.queryAllByType(map);
            for (Map<String, Object> result : list2) {
                for (Map<String, Object> temp2 : list) {
                    if (result.get("custId").equals(temp2.get("custId")) && result.get("userId").equals(temp2.get("userId"))) {
                        result = doWithYear(temp2, result, thisYear, lastYear);
                    }
                }
            }
            List<User> YWList = userMapper.listByType2(IConst.ROLE_TYPE_YW);
            for(Map results:list2){
                //防止业务员换部门导致的部门名称不一致问题，重新查一遍放入list
                for(User temp:YWList){
                    if(temp.getId().equals(results.get("userId"))){
                        results.put("deptId",temp.getDeptId());
                        results.put("deptName",temp.getDeptName());
                        break;
                    }
                }
            }
            return list2;
        }else{
            map.put("yearStart", lastYear);
            map.put("yearEnd", thisYear);
            List<Map<String, Object>> list = businessStatisticsMapper.listAllNotIncomeYear(map);
            return handlerNotIncomeTree(lastYear, thisYear, list);
        }
    }

    private Map doWithYear(Map temp2, Map result, Integer thisYear, Integer lastYear) {
        Integer year = MapUtils.getInteger(temp2, "year");
        if (thisYear.equals(year)) {
            result.put("this_sale", temp2.get("sale"));
            result.put("this_notIncome", temp2.get("notIncome"));
            result.put("this_yqIncome", temp2.get("yqIncome"));
            result.put("this_profit", temp2.get("profit"));
        } else if (lastYear.equals(year)) {
            result.put("last_sale", temp2.get("sale"));
            result.put("last_notIncome", temp2.get("notIncome"));
            result.put("last_yqIncome", temp2.get("yqIncome"));
            result.put("last_profit", temp2.get("profit"));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> exportNotIncomeDetail(Map map, OutputStream outputStream) {
        Integer type = MapUtils.getInteger(map, "type");//从页面选择的类型
        if(type != null && type == 3){
            List<Map<String, Object>> userList = doNotIncomeYear(1, 1, map, false);
            String[] heads = {"部门名称", "业务员",
                    "上一年业绩", "上一年利润", "上一年未到款", "上一年逾期款",
                    "当前年业绩", "当前年利润", "当前年未到款", "当前年逾期款"
            };
            String[] fields = {"deptName", "userName",
                    "last_sale", "last_profit", "last_notIncome", "last_yqIncome",
                    "this_sale", "this_profit", "this_notIncome", "this_yqIncome"
            };
            ExcelUtil.exportExcel("未到款统计详情", heads, fields, userList, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (!StringUtils.isEmpty(value)) {
                    if ("deptName".equals(field) || "userName".equals(field)) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    }
                } else {
                    cell.setCellValue(0);
                }
            });
            return userList;
        }else if(type != null && type == 4){
            List<Map<String, Object>> userList = doNotIncomeYear(1, 1, map, false);
            String[] heads = {"部门名称", "业务员", "客户公司", "客户对接人",
                    "上一年业绩", "上一年利润", "上一年未到款", "上一年逾期款",
                    "当前年业绩", "当前年利润", "当前年未到款", "当前年逾期款"
            };
            String[] fields = {"deptName", "userName", "companyName", "custName",
                    "last_sale", "last_profit", "last_notIncome", "last_yqIncome",
                    "this_sale", "this_profit", "this_notIncome", "this_yqIncome"
            };
            ExcelUtil.exportExcel("未到款统计详情", heads, fields, userList, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (!StringUtils.isEmpty(value)) {
                    if ("deptName".equals(field) || "userName".equals(field) || "companyName".equals(field) || "custName".equals(field)) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    }
                } else {
                    cell.setCellValue(0);
                }
            });
            return userList;
        }else {
            Integer thisYear = MapUtils.getInteger(map, "year");
            Integer lastYear = thisYear - 1;
            List<Map<String, Object>> userList = doNotIncomeYear(1, 1, map, false);
            String[] heads = new String[9];
            String[] fields = new String[9];
            heads[0] = "部门名称";
            fields[0] = "deptName";
            int indexHead = 0;
            int indexField = 0;
            Map<String, Object> sumMap = new HashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            for(int i = lastYear; i <= thisYear; i++){
                heads[++indexHead] = String.format("%s年业绩", i);
                heads[++indexHead] = String.format("%s年利润", i);
                heads[++indexHead] = String.format("%s年未到款", i);
                heads[++indexHead] = String.format("%s年逾期款", i);
                fields[++indexField] = String.format("sale_%s", i);
                fields[++indexField] = String.format("profit_%s", i);
                fields[++indexField] = String.format("notIncome_%s", i);
                fields[++indexField] = String.format("yqIncome_%s", i);
                for(Map<String, Object> itemMap : userList){
                    Double currentSale = MapUtils.getDouble(itemMap, String.format("selfSale_%s", i));
                    Double currentNotIncome = MapUtils.getDouble(itemMap, String.format("selfNotIncome_%s", i));
                    Double currentYqIncome = MapUtils.getDouble(itemMap, String.format("selfYqIncome_%s", i));
                    Double currentProfit = MapUtils.getDouble(itemMap, String.format("selfProfit_%s", i));
                    if(sumMap.containsKey(String.format("sale_%s", i))){
                        Double oldSale = MapUtils.getDouble(sumMap, String.format("sale_%s", i));
                        sumMap.put(String.format("sale_%s", i), decimalFormat.format(currentSale + oldSale));
                    }else {
                        sumMap.put(String.format("sale_%s", i), decimalFormat.format(currentSale));
                    }
                    if(sumMap.containsKey(String.format("profit_%s", i))){
                        Double oldProfit = MapUtils.getDouble(sumMap, String.format("profit_%s", i));
                        sumMap.put(String.format("profit_%s", i), decimalFormat.format(currentProfit + oldProfit));
                    }else {
                        sumMap.put(String.format("profit_%s", i), decimalFormat.format(currentProfit));
                    }
                    if(sumMap.containsKey(String.format("notIncome_%s", i))){
                        Double oldNotIncome = MapUtils.getDouble(sumMap, String.format("notIncome_%s", i));
                        sumMap.put(String.format("notIncome_%s", i), decimalFormat.format(currentNotIncome + oldNotIncome));
                    }else {
                        sumMap.put(String.format("notIncome_%s", i), decimalFormat.format(currentNotIncome));
                    }
                    if(sumMap.containsKey(String.format("yqIncome_%s", i))){
                        Double oldYqIncome = MapUtils.getDouble(sumMap, String.format("yqIncome_%s", i));
                        sumMap.put(String.format("yqIncome_%s", i), decimalFormat.format(currentYqIncome + oldYqIncome));
                    }else {
                        sumMap.put(String.format("yqIncome_%s", i), decimalFormat.format(currentYqIncome));
                    }
                }
            }
            sumMap.put("deptName", "合计");
            userList.add(sumMap);
            ExcelUtil.exportExcel("未到款统计详情", heads, fields, userList, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (!StringUtils.isEmpty(value)) {
                    if ("deptName".equals(field)) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    }
                } else {
                    cell.setCellValue(0);
                }
            });
            return userList;
        }
    }

    @Override
    public PageInfo<Map<String, Object>> querySaleStat(int pageNum, int pageSize, Map map) {
        List<Map<String, Object>> list = doSaleStat(pageNum, pageSize, map, true);
        return new PageInfo<>(list);
    }

    private List<Map<String, Object>> doSaleStat(Integer pageNum, Integer pageSize, Map map, Boolean flag) {
        Integer type = MapUtils.getInteger(map, "type");//从页面选择的类型
        User user = AppUtil.getUser();
        if (ObjectUtils.isEmpty(user)) {
            return null;
        }
        map = doWithPermission(map, user);
        //type=1业务部 type=2业务组 type=3业务员
        if(type != null && type == 3){
            if (flag) {
                PageHelper.startPage(pageNum, pageSize);
            }
            //返回的列表
            List<Map<String, Object>> list = businessStatisticsMapper.queryAllSaleByTypeAndLevel(map);
            return list;
        }else {
            List<Map<String, Object>> list = businessStatisticsMapper.listAllSaleByParam(map);
            return handlerSaleTree(list);
        }
    }

    //处理业绩统计部门树
    private List<Map<String, Object>> handlerSaleTree(List<Map<String, Object>> list){
        List<Map<String, Object>> result = new ArrayList<>();
        //查询List转换成Map
        Map<String, Map<String, Object>> tempMap = new HashMap<>();
        for(Map<String, Object> saleMap : list){
            //由于数据存在许多level为空的无效数据，所以需要排除
            if(saleMap.get("level") != null){
                String deptId = String.valueOf(saleMap.get("id"));
                saleMap.put("isLeaf", true); //默认所有节点都是叶子节点
                saleMap.put("expanded", false); //默认不展开
                saleMap.put("selfSale", saleMap.get("sale"));//缓存单节点金额
                saleMap.put("selfIncome", saleMap.get("income"));//缓存单节点金额
                saleMap.put("selfTax", saleMap.get("tax"));//缓存单节点金额
                saleMap.put("selfOutgo", saleMap.get("outgo"));//缓存单节点金额
                saleMap.put("selfRefund", saleMap.get("refund"));//缓存单节点金额
                saleMap.put("selfOtherPay", saleMap.get("otherPay"));//缓存单节点金额
                saleMap.put("selfProfit", saleMap.get("profit"));//缓存单节点金额
                saleMap.put("selfComm", saleMap.get("comm"));//缓存单节点金额
                tempMap.put(deptId, saleMap);
            }
        }
        //统计子部门数据到父级部门
        Iterator<Map<String, Object>> iterator = list.iterator();
        while (iterator.hasNext()){
            Map<String, Object> saleMap = iterator.next();
            //由于数据存在许多level为空的无效数据，所以需要排除
            if(saleMap.get("level") != null){
                String deptId = String.valueOf(saleMap.get("id"));
                String parentId = String.valueOf(saleMap.get("parent"));
                //如果直属父级部门被查询出来了，则把子部门金额数据统计起来 并且 设置父节点不为叶子节点
                if(tempMap.containsKey(parentId)){
                    tempMap.get(parentId).put("sale", String.valueOf(Float.parseFloat(String.valueOf(tempMap.get(parentId).get("sale"))) + Float.parseFloat(String.valueOf(tempMap.get(deptId).get("sale")))));
                    tempMap.get(parentId).put("income", String.valueOf(Float.parseFloat(String.valueOf(tempMap.get(parentId).get("income"))) + Float.parseFloat(String.valueOf(tempMap.get(deptId).get("income")))));
                    tempMap.get(parentId).put("tax", String.valueOf(Float.parseFloat(String.valueOf(tempMap.get(parentId).get("tax"))) + Float.parseFloat(String.valueOf(tempMap.get(deptId).get("tax")))));
                    tempMap.get(parentId).put("outgo", String.valueOf(Float.parseFloat(String.valueOf(tempMap.get(parentId).get("outgo"))) + Float.parseFloat(String.valueOf(tempMap.get(deptId).get("outgo")))));
                    tempMap.get(parentId).put("refund", String.valueOf(Float.parseFloat(String.valueOf(tempMap.get(parentId).get("refund"))) + Float.parseFloat(String.valueOf(tempMap.get(deptId).get("refund")))));
                    tempMap.get(parentId).put("otherPay", String.valueOf(Float.parseFloat(String.valueOf(tempMap.get(parentId).get("otherPay"))) + Float.parseFloat(String.valueOf(tempMap.get(deptId).get("otherPay")))));
                    tempMap.get(parentId).put("profit", String.valueOf(Float.parseFloat(String.valueOf(tempMap.get(parentId).get("profit"))) + Float.parseFloat(String.valueOf(tempMap.get(deptId).get("profit")))));
                    tempMap.get(parentId).put("comm", String.valueOf(Float.parseFloat(String.valueOf(tempMap.get(parentId).get("comm"))) + Float.parseFloat(String.valueOf(tempMap.get(deptId).get("comm")))));
                    //设置父节点不为叶子节点
                    tempMap.get(parentId).put("isLeaf", false);
                    //将子节点包含进去
                    if(!tempMap.get(parentId).containsKey("nodes")){
                        tempMap.get(parentId).put("nodes", new ArrayList<>());
                    }
                    ((List<Map<String, Object>>)tempMap.get(parentId).get("nodes")).add(saleMap);
                    iterator.remove();//列表移除当前节点
                }
            }
        }

        tree2List(0, list, result);//将树转换成列表
        return result;
    }
    //树转换成List
    private void tree2List(int currentLevel, List<Map<String, Object>> sourceList, List<Map<String, Object>> targetList){
      for(Map<String, Object> nodeMap : sourceList){
          nodeMap.put("level", currentLevel);//设置当前节点层级，从0开始
          targetList.add(nodeMap);//列表添加
          //判断是否有子节点
          if(nodeMap.containsKey("nodes") && CollectionUtils.isNotEmpty(((List<Map<String, Object>>)nodeMap.get("nodes")))){
              tree2List(currentLevel + 1, ((List<Map<String, Object>>)nodeMap.get("nodes")), targetList);
          }
          nodeMap.remove("nodes");
      }
    }


    @Override
    public Map querySaleStatSum(Map map) {
        Integer type = MapUtils.getInteger(map, "type");//从页面选择的类型
        User user = AppUtil.getUser();
        if (ObjectUtils.isEmpty(user)) {
            return null;
        }
        if (type == 2) {
            map.put("level", 4);
        }
        map = doWithPermission(map, user);
        return businessStatisticsMapper.queryAllSaleByTypeAndLevelSum(map);
    }

    @Override
    public void exportSaleStat(Map map, OutputStream outputStream) {
        Integer type = MapUtils.getInteger(map, "type");//从页面选择的类型
        List<Map<String, Object>> list = doSaleStat(0, 0, map, false);
        if(type != null && type == 3){
            String[] heads = {"部门名称", "业务员", "业绩（含税）", "回款", "税金", "退款", "其它支出",
                    "成本", "利润", "提成"};
            String[] fields = {"deptName", "userName", "sale", "income", "tax", "refund", "otherPay",
                    "outgo", "profit", "comm"};
            ExcelUtil.exportExcel("业绩统计", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (value != null) {
                    if ("sale".equals(field) || "income".equals(field) || "tax".equals(field) || "refund".equals(field)
                            || "otherPay".equals(field) || "outgo".equals(field) || "profit".equals(field) || "comm".equals(field)) {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
            });
        }else {
            String[] heads = {"部门名称", "业绩（含税）", "回款", "税金", "退款", "其它支出",
                    "成本", "利润", "提成"};
            String[] fields = {"deptName", "sale", "income", "tax", "refund", "otherPay",
                    "outgo", "profit", "comm"};
            ExcelUtil.exportExcel("业绩统计", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (value != null) {
                    if ("sale".equals(field) || "income".equals(field) || "tax".equals(field) || "refund".equals(field)
                            || "otherPay".equals(field) || "outgo".equals(field) || "profit".equals(field) || "comm".equals(field)) {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
            });
        }
    }
}
