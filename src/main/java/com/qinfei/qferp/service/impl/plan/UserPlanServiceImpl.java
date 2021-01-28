package com.qinfei.qferp.service.impl.plan;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.plan.UserGroup;
import com.qinfei.qferp.entity.plan.UserPlan;
import com.qinfei.qferp.entity.sys.Resource;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.sys.WorkDate;
import com.qinfei.qferp.mapper.plan.UserPlanMapper;
import com.qinfei.qferp.mapper.sys.DeptZwMapper;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.service.plan.IUserPlanService;
import com.qinfei.qferp.service.sys.IDeptService;
import com.qinfei.qferp.service.sys.IWorkDateService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.SysConfigUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @CalssName UserPlanServiceImpl
 * @Description 用户计划服务
 * @Author xuxiong
 * @Date 2019/8/10 0010 9:52
 * @Version 1.0
 */
@Service
public class UserPlanServiceImpl implements IUserPlanService{
    @Autowired
    private UserPlanMapper userPlanMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private IWorkDateService workDateService;
    @Autowired
    private DeptZwMapper deptZwMapper;
    @Autowired
    private IDeptService deptService;

    @Override
    @Transactional
    public void save(UserPlan userPlan) {
        userPlan.setCreateDate(new Date());
        try{
            if(userPlan == null){
               throw new QinFeiException(1002, "录入数据不存在！");
            }
            User user = AppUtil.getUser();
            userPlan.setCreateId(user.getId());
            userPlan.setUpdateId(user.getId());
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            //0、判断今天是否有录入记录
            UserPlan existsPlan = userPlanMapper.getPlanByUserIdAndDate(user.getId(), DateUtils.format(userPlan.getCreateDate(),"yyyyMMdd"), 0);
            if(existsPlan != null){
                throw new QinFeiException(1002, "很抱歉，您已经录入计划了，请明天再录...");
            }
            //判断今天是否需要填写计划
            isNeedAddPlan(userPlan);

            //1、获取昨日计划数据，和当前录入的总结进行比较，并将结果存在对应字段
//            UserPlan lastPlan = userPlanMapper.getPlanByUserIdAndDate(user.getId(), getLastDate(userPlan),1);
            UserPlan lastPlan = userPlanMapper.getLastPlanByUserId(user.getId(),1);
            userPlan.setSummaryResult(calLastPlanResult(userPlan, lastPlan));//设置昨日统计结果
            userPlan.setIsOvertime(getIsOverTime(userPlan)); //设置是否超时登记，0-未超时、1-超时
            userPlanMapper.save(userPlan);
            //2、新增一条记录，存储今日计划和今日总结，用于统计使用，今日总结由于明日才会出，所以默认为0
            UserPlan addPlan = new UserPlan();
            BeanUtils.copyProperties(userPlan, addPlan);  //下面需要把总结数据覆盖成对应的计划总结
            addPlan.setSummaryType(1); //1-当天总结(当前计划的总结)
            addPlan.setSummaryResult(""); //总结结果
            addPlan.setPerfoSummary(new BigDecimal(0)); //业绩（元)
            addPlan.setProfitSummary(new BigDecimal(0)); //利润业绩（元）
            addPlan.setYxCustomSummary(0); //有效客户量
            addPlan.setXcjCustomSummary(0); //新成交客户量
            addPlan.setGjCustomSummary(0); //跟进客户数量
            addPlan.setTzyCustomSummary(0); //推资源客户数量
            userPlanMapper.save(addPlan);
            //3、修改昨日计划总结
            if(lastPlan != null){
                setLastPlanSummary(lastPlan,userPlan);
                userPlanMapper.updateSummaryById(lastPlan);
            }else{ //如果所有计划为空（第一次录入），说明昨日没有录入计划，但是今日录入了昨日总结，所以需要添加一条昨日总结记录
                UserPlan lastPlanSummary = new UserPlan();
                lastPlanSummary.setCreateId(user.getId());
                lastPlanSummary.setCreateDate(DateUtils.parse(getLastDate(userPlan),"yyyyMMdd"));
                setLastPlanSummary(lastPlanSummary,userPlan);
                lastPlanSummary.setSummaryType(1); //总结
                userPlanMapper.save(lastPlanSummary);
            }
        }catch (QinFeiException e){
            throw new QinFeiException(e.getCode(),e.getMessage());
        }
        catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "新增计划出问题啦...");
        }
    }

    /**
     * 判断是否需要添加计划
     * @param userPlan
     */
    private void isNeedAddPlan(UserPlan userPlan){
        //查询工作日表，有数据，判断是否为工作日，没有数据，判断当前是否是周末，默认周末为休息日
        WorkDate workDate = workDateService.getWorkDateByDate(DateUtils.format(userPlan.getCreateDate(), "yyyy-MM-dd"));
        if(workDate != null){
            if(workDate.getDateType() == 1){
                throw new QinFeiException(1002, "很抱歉，今天休息日，好好休息，工作日再来录入...");
            }else if(workDate.getDateType() == 2){
                throw new QinFeiException(1002, "很抱歉，今天法定节假日，好好休息，工作日再来录入...");
            }
        }else{
            int week = getWeek(userPlan.getCreateDate());
            if(week == Calendar.SUNDAY || week == Calendar.SATURDAY){
                throw new QinFeiException(1002, "很抱歉，今天周末，好好休息，工作日再来录入...");
            }
        }
    }

    /**
     * 设置昨日计划总结
     * @param lastPlanSummary 昨日计划
     * @param userPlan
     */
    private void  setLastPlanSummary(UserPlan lastPlanSummary, UserPlan userPlan){
        lastPlanSummary.setPerfoSummary(userPlan.getPerfoSummary()); //业绩（元)
        lastPlanSummary.setProfitSummary(userPlan.getProfitSummary()); //利润业绩（元）
        lastPlanSummary.setYxCustomSummary(userPlan.getYxCustomSummary()); //有效客户量
        lastPlanSummary.setXcjCustomSummary(userPlan.getXcjCustomSummary()); //新成交客户量
        lastPlanSummary.setGjCustomSummary(userPlan.getGjCustomSummary()); //跟进客户数量
        lastPlanSummary.setTzyCustomSummary(userPlan.getTzyCustomSummary()); //推资源客户数量
    }

    //获取当前昨日计划时间，当当前日期为星期一时，昨日计划日期为上周五
    private String getLastDate(UserPlan userPlan){
        int week = getWeek(userPlan.getCreateDate());
        Date date = DateUtils.calDay(userPlan.getCreateDate(),-1);
        if(week == Calendar.MONDAY){ //周一
            date = DateUtils.calDay(userPlan.getCreateDate(),-3);
        }
        return DateUtils.format(date, "yyyyMMdd");
    }

    //获取周序号
    private int getWeek(Date date){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int week = c.get(Calendar.DAY_OF_WEEK);
        return week;
    }

    //计算昨日计划结果
    private String calLastPlanResult(UserPlan currentPlan, UserPlan lastPlan){
        String result = "000000"; //默认都完成，如果昨天计划没有，则昨日总结结果都为完成
        if(lastPlan != null){
            StringBuilder stringBuilder = new StringBuilder();
            //业绩（元)
            if(currentPlan.getPerfoSummary().compareTo(lastPlan.getPerfoPlan()) >= 0){
                stringBuilder.append("0"); //完成
            }else {
                stringBuilder.append("1"); //未完成
            }
            //利润业绩（元）
            if(currentPlan.getProfitSummary().compareTo(lastPlan.getProfitPlan()) >= 0){
                stringBuilder.append("0"); //完成
            }else {
                stringBuilder.append("1"); //未完成
            }
            //有效客户量
            if(currentPlan.getYxCustomSummary() >= lastPlan.getYxCustomPlan()){
                stringBuilder.append("0"); //完成
            }else {
                stringBuilder.append("1"); //未完成
            }
            //新成交客户量
            if(currentPlan.getXcjCustomSummary() >= lastPlan.getXcjCustomPlan()){
                stringBuilder.append("0"); //完成
            }else {
                stringBuilder.append("1"); //未完成
            }
            //跟进客户数量
            if(currentPlan.getGjCustomSummary() >= lastPlan.getGjCustomPlan()){
                stringBuilder.append("0"); //完成
            }else {
                stringBuilder.append("1"); //未完成
            }
            //推资源客户数量
            if(currentPlan.getTzyCustomSummary() >= lastPlan.getTzyCustomPlan()){
                stringBuilder.append("0"); //完成
            }else {
                stringBuilder.append("1"); //未完成
            }
            result = stringBuilder.toString();
        }
        return result;
    }

    //获取登记是否超时
    private Integer getIsOverTime(UserPlan userPlan){
        Date finalDate = DateUtils.parse(DateUtils.format(new Date(),"yyyy-MM-dd")+" 08:51","yyyy-MM-dd HH:mm");
        int result = finalDate.compareTo(userPlan.getCreateDate());
        return result > 0 ? 0 : 1;
    }

    @Override
    public PageInfo<UserPlan> listPlanByCurrentUser(Map<String, Object> map, Pageable pageable) {
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<UserPlan> result = userPlanMapper.listPlanByUserId(map);
        return new PageInfo<>(result);
    }

    @Override
    public UserPlan getTotalByUserId(Map<String, Object> map) {
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        return userPlanMapper.getTotalByUserId(map);
    }

    @Override
    public PageInfo<UserPlan> listPlanByParam(Map<String, Object> map, Pageable pageable) {
        //判断是否前端有传入用户，addSecurityByFore不是领导会默认当前用户，所以如果普通员工选择期号后，没选用户则可查看当前期号所有的，否则只能查看自己的
        boolean isRequestUserFalg = map.get("currentUserId") == null ? false : true;
        this.addSecurityByFore(map); //添加权限，不是部长默认添加用户
        if(!isRequestUserFalg && map.get("groupId") != null && map.get("deptIds") == null){ //如果没有传入用户，并且选择了群组，并且没有得到部门（说明不是领导）
            map.remove("currentUserId");
        }
        map.put("excludeDeptList", SysConfigUtils.getConfigValue("userPlanExcudeDept",List.class));//读取配置排除部门的列表
        map.put("excludeUserList", SysConfigUtils.getConfigValue("userPlanExcudeUser", List.class));//读取配置排除人员的列表
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<UserPlan> result = userPlanMapper.listPlanByParam(map);
        return new PageInfo<>(result);
    }

    @Override
    public UserPlan getTotalByParam(Map<String, Object> map) {
        //判断是否前端有传入用户，addSecurityByFore不是领导会默认当前用户，所以如果普通员工选择期号后，没选用户则可查看当前期号所有的，否则只能查看自己的
        boolean isRequestUserFalg = map.get("currentUserId") == null ? false : true;
        this.addSecurityByFore(map); //添加权限，不是部长默认添加用户
        if(!isRequestUserFalg && map.get("groupId") != null && map.get("deptIds") == null){ //如果没有传入用户，并且选择了群组，并且没有得到部门（说明不是领导）
            map.remove("currentUserId");
        }
        map.put("excludeDeptList", SysConfigUtils.getConfigValue("userPlanExcudeDept",List.class));//读取配置排除部门的列表
        map.put("excludeUserList", SysConfigUtils.getConfigValue("userPlanExcudeUser", List.class));//读取配置排除人员的列表
        UserPlan userPlan = userPlanMapper.getTotalByParam(map);
        return userPlan;
    }

    @Override
    public PageInfo<UserPlan> listPlanStatisticsByParam(Map<String, Object> map, Pageable pageable) {
        this.addSecurityByFore(map); //添加权限
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        map.put("excludeDeptList", SysConfigUtils.getConfigValue("userPlanExcudeDept",List.class));//读取配置排除部门的列表
        map.put("excludeUserList", SysConfigUtils.getConfigValue("userPlanExcudeUser", List.class));//读取配置排除人员的列表
        List<UserPlan> result = userPlanMapper.listPlanStatisticsByParam(map);
        return new PageInfo<>(result);
    }

    @Override
    public UserPlan getStatisticsTotalByParam(Map<String, Object> map) {
        this.addSecurityByFore(map); //添加权限
        map.put("excludeDeptList", SysConfigUtils.getConfigValue("userPlanExcudeDept",List.class));//读取配置排除部门的列表
        map.put("excludeUserList", SysConfigUtils.getConfigValue("userPlanExcudeUser", List.class));//读取配置排除人员的列表
        UserPlan userPlan = userPlanMapper.getStatisticsTotalByParam(map);
        //如果不是只查询某个人的则需要获取人数，否则查询人数
        if(map.get("currentUserId") == null){
            Map<String, Map<String, Integer>> deptUserMap = getDeptUserNum(map);//获取业务人员数量
            if(deptUserMap != null && deptUserMap.get("total") != null && deptUserMap.get("total").get("userNum") != 0){
                int userTotal = deptUserMap.get("total").get("userNum");
                userPlan.setRxbSummary(userPlan.getProfitSummary().divide(new BigDecimal(userTotal), 2, RoundingMode.HALF_UP).floatValue()); //人效比
                userPlan.setRjkhSummary(new BigDecimal(userPlan.getYxCustomSummary().floatValue()/userTotal).setScale(2, RoundingMode.HALF_UP).floatValue());//人均开发有效客户数
            }
        }else {
            userPlan.setRxbSummary(userPlan.getProfitSummary().divide(new BigDecimal(1), 2, RoundingMode.HALF_UP).floatValue()); //人效比
            userPlan.setRjkhSummary(new BigDecimal(userPlan.getYxCustomSummary().floatValue()/1).setScale(2, RoundingMode.HALF_UP).floatValue());//人均开发有效客户数
        }
        return userPlan;
    }

    @Override
    public List<UserGroup> listUserGroupByParam(Map<String, Object> map) {
        User user = AppUtil.getUser();
        map.put("companyCode",user.getDept().getCompanyCode());
        return userPlanMapper.listUserGroupByParam(map);
    }

    @Override
    public PageInfo<Map<String, Object>> listNotEnterPlanUserByParam(Map<String, Object> map, Pageable pageable) {
        this.addSecurityByFore(map); //添加权限
        int diffDay = getDiffDays(map); //相差天数
        map.put("dayNum",diffDay);
        List<String> dateList = findDates(diffDay);
        List<Map<String, Object>> result = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(dateList)){
            String startDate = dateList.get(dateList.size()-1);
            String endDate = dateList.get(0);
            Map<String, List<String>> dateGroupMap = workDateService.listDateByRange(startDate, endDate);
            if(dateGroupMap != null && CollectionUtils.isNotEmpty(dateGroupMap.get("workDate"))){
                map.put("workDateList", dateGroupMap.get("workDate")); //设置工作日
            }else{
                return new PageInfo<>(new ArrayList<>());
            }
            map.put("excludeDeptList", SysConfigUtils.getConfigValue("userPlanExcudeDept",List.class));//读取配置排除部门的列表
            map.put("excludeUserList", SysConfigUtils.getConfigValue("userPlanExcudeUser", List.class));//读取配置排除人员的列表
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            result = userPlanMapper.listNotEnterPlanUserByParam(map);
        }
        return new PageInfo<>(result);
    }

    //获取当前时间时间段往前时间列表
    private List<String> findDates(int diff) {
        List<String> result = new ArrayList<>();
        result.add(DateUtils.format(new Date(), "yyyy-MM-dd")); //将结束时间保存
        Calendar calendar = Calendar.getInstance();
        for(int i = 1; i < diff; i++){
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            result.add(DateUtils.format(calendar.getTime(), "yyyy-MM-dd"));
        }
        return result;
    }

    //获取距离当前日期的天数
    private int getDiffDays(Map<String, Object> map){
        int result = 31;
        if(map.get("timeQuantum") == null){  //查询当天
            result = 1;
        }else if(String.valueOf(map.get("timeQuantum")).equals("1")){  //本周
            result = 7;
        }else if(String.valueOf(map.get("timeQuantum")).equals("2")){ //本月
            result = 31;
        }else { //时间区间
            if(map.get("startDate") != null){
               result = DateUtils.getDiffDays(DateUtils.parse(String.valueOf(map.get("startDate")),"yyyy/MM/dd"),new Date()) + 1;
            }else if(map.get("endDate") != null){
               result = DateUtils.getDiffDays(DateUtils.parse(String.valueOf(map.get("endDate")),"yyyy/MM/dd"),new Date()) + 3 * 31; //如果只填写结束时间，则支持往前推3个月的
            }
        }
        return result;
    }

    @Override
    public void exportPlanByUserId(Map<String, Object> map, OutputStream outputStream) {
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        List<Map<String, Object>> result = userPlanMapper.listPlanMapByUserId(map);
        String fileName = getExcelTitle(map,4);
        String[] heads = {"姓名","时间","业绩（昨日总结)","利润业绩（昨日总结)","有效客户量（昨日总结)","新成交客户量（昨日总结)","跟进客户数量（昨日总结) ","推资源客户数量（昨日总结)","业绩（今日计划)","利润业绩（今日计划）","有效客户量（今日计划）","新成交客户量（今日计划）","跟进客户数量（今日计划）","推资源客户数量（今日计划）"};
        String[] fields={ "userName","createDate","perfoSummary","profitSummary","yxCustomSummary","xcjCustomSummary","gjCustomSummary","tzyCustomSummary","perfoPlan","profitPlan","yxCustomPlan","xcjCustomPlan","gjCustomPlan","tzyCustomPlan"};
        ExcelUtil.exportExcel( fileName,heads, fields, result, outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if(value!=null){
                if("perfoSummary".equals(field) || "profitSummary".equals(field) || "perfoPlan".equals(field) || "profitPlan".equals(field)){
                    cell.setCellValue(Double.parseDouble(value.toString()));
                }else{
                    cell.setCellValue(value.toString());
                }
            }else {
                cell.setCellValue("");
            }
        });
    }

    @Override
    public void exportPlanByParam(Map<String, Object> map, OutputStream outputStream) {
        //判断是否前端有传入用户，addSecurityByFore不是领导会默认当前用户，所以如果普通员工选择期号后，没选用户则可查看当前期号所有的，否则只能查看自己的
        boolean isRequestUserFalg = map.get("currentUserId") == null ? false : true;
        this.addSecurityByFore(map); //添加权限，不是部长默认添加用户
        if(!isRequestUserFalg && map.get("groupId") != null && map.get("deptIds") == null){ //如果没有传入用户，并且选择了群组，并且没有得到部门（说明不是领导）
            map.remove("currentUserId");
        }
        map.put("excludeDeptList", SysConfigUtils.getConfigValue("userPlanExcudeDept",List.class));//读取配置排除部门的列表
        map.put("excludeUserList", SysConfigUtils.getConfigValue("userPlanExcudeUser", List.class));//读取配置排除人员的列表
        List<Map<String, Object>> result = userPlanMapper.listPlanMapByParam(map);
        String fileName = getExcelTitle(map,1);
        String[] heads = {"姓名","时间","业绩（昨日总结)","利润业绩（昨日总结)","有效客户量（昨日总结)","新成交客户量（昨日总结)","跟进客户数量（昨日总结) ","推资源客户数量（昨日总结)","业绩（今日计划)","利润业绩（今日计划）","有效客户量（今日计划）","新成交客户量（今日计划）","跟进客户数量（今日计划）","推资源客户数量（今日计划）"};
        String[] fields={ "userName","createDate","perfoSummary","profitSummary","yxCustomSummary","xcjCustomSummary","gjCustomSummary","tzyCustomSummary","perfoPlan","profitPlan","yxCustomPlan","xcjCustomPlan","gjCustomPlan","tzyCustomPlan"};
        ExcelUtil.exportExcel( fileName,heads, fields, result, outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if(value!=null){
                if("perfoSummary".equals(field) || "profitSummary".equals(field) || "perfoPlan".equals(field) || "profitPlan".equals(field)){
                    cell.setCellValue(Double.parseDouble(value.toString()));
                }else{
                    cell.setCellValue(value.toString());
                }
            }else{
                cell.setCellValue("");
            }
        });
    }

    @Override
    public void exportPlanStatisticsByParam(Map<String, Object> map, OutputStream outputStream) {
        this.addSecurityByFore(map); //添加权限
        map.put("excludeDeptList", SysConfigUtils.getConfigValue("userPlanExcudeDept",List.class));//读取配置排除部门的列表
        map.put("excludeUserList", SysConfigUtils.getConfigValue("userPlanExcudeUser", List.class));//读取配置排除人员的列表
        List<Map<String, Object>> result = userPlanMapper.listPlanStatisticsMapByParam(map);
        String fileName = getExcelTitle(map,2);
        String[] heads = {"姓名","业绩（计划)","利润业绩（计划)","有效客户量（计划)","新成交客户量（计划)","跟进客户数量（计划)","推资源客户数量（计划)","业绩（总结)","利润业绩（总结)","有效客户量（总结)","新成交客户量（总结)","跟进客户数量（总结)","推资源客户数量（总结)"};
        String[] fields={ "userName","perfoPlan","profitPlan","yxCustomPlan","xcjCustomPlan","gjCustomPlan","tzyCustomPlan","perfoSummary","profitSummary","yxCustomSummary","xcjCustomSummary","gjCustomSummary","tzyCustomSummary"};
        ExcelUtil.exportExcel( fileName,heads, fields, result, outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if(value!=null){
                if("perfoSummary".equals(field) || "profitSummary".equals(field) || "perfoPlan".equals(field) || "profitPlan".equals(field)){
                    cell.setCellValue(Double.parseDouble(value.toString()));
                }else{
                    cell.setCellValue(value.toString());
                }
            }else {
                cell.setCellValue("");
            }
        });
    }

    @Override
    public void exportNotEnterPlanByParam(Map<String, Object> map, OutputStream outputStream) {
        this.addSecurityByFore(map); //添加权限
        map.put("dayNum",getDiffDays(map));
        map.put("excludeDeptList", SysConfigUtils.getConfigValue("userPlanExcudeDept",List.class));//读取配置排除部门的列表
        map.put("excludeUserList", SysConfigUtils.getConfigValue("userPlanExcudeUser", List.class));//读取配置排除人员的列表
        List<Map<String, Object>> result = userPlanMapper.listNotEnterPlanUserByParam(map);
        String fileName = getExcelTitle(map,3);
        String[] heads = {"日期","所属公司","所属部门","用户名"};
        String[] fields={ "dayTime","companyCodeName","deptName","userName"};
        ExcelUtil.exportExcel( fileName,heads, fields, result, outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if(value != null){
                cell.setCellValue(value.toString());
            }else{
                cell.setCellValue("");
            }
        });
    }

    @Override
    public Map<String, List<Map<String, String>>> listUserSummaryRanking(Map<String, Object> param) {
        Map<String, List<Map<String, String>>> result = new HashMap<>();
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录");
            }
            if(param.get("companyCode") == null){
                param.put("companyCode", user.getCompanyCode());
            }
            param.put("excludeDeptList", SysConfigUtils.getConfigValue("userPlanExcudeDept",List.class));//读取配置排除部门的列表
            param.put("excludeUserList", SysConfigUtils.getConfigValue("userPlanExcudeUser", List.class));//读取配置排除人员的列表
            //默认设置上一个工作日排名
            setLastWorkDate(param);

            //按照有效客户量排名
            List<Map<String, Object>> listUserSummaryRanking = userPlanMapper.listUserSummaryRanking(param);
            if(CollectionUtils.isNotEmpty(listUserSummaryRanking)){
                result.put("yxkhList", new ArrayList<>());
                for(Map<String, Object> userSummary : listUserSummaryRanking){
                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("name", String.valueOf(userSummary.get("name")));
                    userMap.put("companyCode", String.valueOf(userSummary.get("companyCode")));
                    userMap.put("companyCodeName", String.valueOf(userSummary.get("companyCodeName")));
                    userMap.put("yxkh", String.valueOf(userSummary.get("yxkh")));
                    userMap.put("profit", String.valueOf(userSummary.get("profit")));
                    result.get("yxkhList").add(userMap);
                }
            }
            param.put("rankingType", 1); //按照人效比排名，业绩利润排名
            List<Map<String, Object>> listUserSummaryRanking1 = userPlanMapper.listUserSummaryRanking(param);
            if(CollectionUtils.isNotEmpty(listUserSummaryRanking1)){
                result.put("rxbList", new ArrayList<>());
                for(Map<String, Object> userSummary : listUserSummaryRanking1){
                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("name", String.valueOf(userSummary.get("name")));
                    userMap.put("companyCode", String.valueOf(userSummary.get("companyCode")));
                    userMap.put("companyCodeName", String.valueOf(userSummary.get("companyCodeName")));
                    userMap.put("yxkh", String.valueOf(userSummary.get("yxkh")));
                    userMap.put("profit", String.valueOf(userSummary.get("profit")));
                    result.get("rxbList").add(userMap);
                }
            }
            return result;
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "个人计划总结排名异常！");
        }
    }

    @Override
    public Map<String, List<Map<String, Object>>> listDeptSummary(Map<String, Object> param) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            if(param.get("companyCode") == null){
                param.put("companyCode", user.getCompanyCode());
            }

            //默认设置上一个工作日排名
            setLastWorkDate(param);

            param.put("excludeDeptList", SysConfigUtils.getConfigValue("userPlanExcudeDept",List.class));//读取配置排除部门的列表
            param.put("excludeUserList", SysConfigUtils.getConfigValue("userPlanExcudeUser", List.class));//读取配置排除人员的列表
            //由于没写递归算法，此处查询部门必须按照层级倒序排序，不然计算会出问题，存在只统计直属子级部门，所以需要从低层级往层级计算
            List<Map<String, Object>> deptSummaryList = userPlanMapper.listDeptSummary(param);
            if(CollectionUtils.isNotEmpty(deptSummaryList)){
                //查询List转换成Map
                Map<String, Map<String, String>> tempMap = new HashMap<>();
                for(Map<String, Object> deptSummaryMap : deptSummaryList){
                    String deptId = String.valueOf(deptSummaryMap.get("id"));
                    tempMap.put(deptId, new HashMap<>());
                    tempMap.get(deptId).put("name",String.valueOf(deptSummaryMap.get("name")));
                    tempMap.get(deptId).put("companyCode",String.valueOf(deptSummaryMap.get("companyCode")));
                    tempMap.get(deptId).put("companyCodeName",String.valueOf(deptSummaryMap.get("companyCodeName")));
                    tempMap.get(deptId).put("level", String.valueOf(deptSummaryMap.get("level"))); //当前部门层级
                    tempMap.get(deptId).put("yxkh", String.valueOf(deptSummaryMap.get("yxkh"))); //当前部门直接有效客户，不含子级部门
                    tempMap.get(deptId).put("profit", String.valueOf(deptSummaryMap.get("profit"))); //当前部门直接业绩利润，不含子级部门
                }
                //统计子部门数据到父级部门
                for(Map<String, Object> deptSummaryMap : deptSummaryList){
                    String deptId = String.valueOf(deptSummaryMap.get("id"));
                    String parentId = String.valueOf(deptSummaryMap.get("parentId"));
                    //如果直属父级部门被查询出来了，则把子部门有效客户数和业绩利润计算进去
                    if(tempMap.containsKey(parentId)){
                        tempMap.get(parentId).put("yxkh", String.valueOf(Integer.parseInt(tempMap.get(parentId).get("yxkh")) + Integer.parseInt(tempMap.get(deptId).get("yxkh"))));
                        tempMap.get(parentId).put("profit", String.valueOf(Double.parseDouble(tempMap.get(parentId).get("profit")) + Double.parseDouble(tempMap.get(deptId).get("profit"))));
                    }
                }
                //计算人效比
                param.put("deptLevel", 3); //只查询部门层级3或者以下的
                Map<String, Map<String, Integer>> deptUserMap = getDeptUserNum(param);
                List<Map<String, Object>> tempList = new ArrayList<>();
                for(String deptId : tempMap.keySet()){
                    //只统计层级level=3的业务部门
                    if("3".equals(tempMap.get(deptId).get("level")) && deptUserMap.get(deptId) != null && deptUserMap.get(deptId).get("userNum") != null){
                        Map<String, Object> deptMap = new HashMap<>();
                        deptMap.put("name", tempMap.get(deptId).get("name"));
                        deptMap.put("companyCode", tempMap.get(deptId).get("companyCode"));
                        deptMap.put("companyCodeName", tempMap.get(deptId).get("companyCodeName"));
                        if(deptUserMap.get(deptId).get("userNum") != 0){
                            deptMap.put("yxkh", Double.parseDouble(tempMap.get(deptId).get("yxkh"))/deptUserMap.get(deptId).get("userNum"));//部门人均有效客户 = 部门有效客户总数 / 部门人数
                            deptMap.put("profit", Double.parseDouble(tempMap.get(deptId).get("profit"))/deptUserMap.get(deptId).get("userNum"));//部门人效比 = 部门业绩利润总额 / 部门人数
                        }else {
                            deptMap.put("yxkh", 0);//部门人均有效客户 = 部门有效客户总数 / 部门人数
                            deptMap.put("profit", 0);//部门人效比 = 部门业绩利润总额 / 部门人数
                        }
                        tempList.add(deptMap);
                    }
                }
                deptRanking(result, tempList, true); //人效比排序
                deptRanking(result, tempList, false); //人均有效客户排序
            }
            return result;
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "部门计划总结排名异常！");
        }
    }

    @Override
    public Map<String, Object> getPlanPermission(HttpServletRequest request) {
        Map<String, Object> permissionMap = new HashMap<>();
        try{
            User user = AppUtil.getUser();
            if(user != null){
                HttpSession session = request.getSession();
                //获取当前用户权限资源，判断是否有"计划总结统计/列表"权限
                List<Resource> resources = (List<Resource>) session.getAttribute(IConst.USER_RESOURCE);
                if(CollectionUtils.isNotEmpty(resources)){
                    for(Resource resource : resources){
                        if(!StringUtils.isEmpty(resource.getUrl()) && resource.getUrl().contains("plan/planStatistics")){
                            permissionMap.put("planStatistics", true);//有计划总结统计权限
                        }
                        if(!StringUtils.isEmpty(resource.getUrl()) && resource.getUrl().contains("plan/planManage")){
                            permissionMap.put("planManage", true);//有计划总结列表权限
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return permissionMap;
    }

    /**
     * 部门列表排序
     * @param result 排序好的结果
     * @param deptList 部门信息列表
     * @param isProfit 是否人效比排序，否则人均有效客户排序
     */
    private void deptRanking(Map<String, List<Map<String, Object>>> result, List<Map<String, Object>> deptList, boolean isProfit){
        if(CollectionUtils.isEmpty(deptList)){
            return;
        }
        List<Map<String, Object>> tempList = new ArrayList<>();
        for(Map<String, Object> map : deptList){
            tempList.add(map);
        }
        //人效比排序
        if(isProfit){
            Collections.sort(tempList, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> deptMap1, Map<String, Object> deptMap2) {
                    Double profit1 = Double.parseDouble(String.valueOf(deptMap1.get("profit")));
                    Double profit2 = Double.parseDouble(String.valueOf(deptMap2.get("profit")));
                    //升序排的话就是第一个参数.compareTo(第二个参数);
                    //降序排的话就是第二个参数.compareTo(第一个参数);
                    return profit2.compareTo(profit1);
                }
            });
            result.put("rxbList", tempList); //人效比排名
        }else {
            Collections.sort(tempList, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> deptMap1, Map<String, Object> deptMap2) {
                    Double yxkh1 = Double.parseDouble(String.valueOf(deptMap1.get("yxkh")));
                    Double yxkh2 = Double.parseDouble(String.valueOf(deptMap2.get("yxkh")));
                    return yxkh2.compareTo(yxkh1);
                }
            });
            result.put("yxkhList", tempList); //有效客户排名
        }
    }

    //获取部门人员数
    private Map<String, Map<String, Integer>> getDeptUserNum(Map<String, Object> param){
        param.put("deptCode", "YW");//只查询业务部门
        param.put("handoverState", 0); //排除交接人员
        param.put("notRoleCode", "ZW"); //排除角色为政委的
        param.put("roleType", "YW"); //角色是业务的人员
        return deptService.getDeptUserNum(param);
    }

    //获取表格标题
    private String getExcelTitle(Map<String, Object> map, Integer type){
        Object timeType = map.get("timeQuantum");
        String title = "";
        String time = DateUtils.format(new Date(), "yyyy-MM-dd");
        if(type == 1){
            title = "计划总结列表(time)";
        }else if(type == 2){
            title = "计划总结统计列表(time)";
        }else if(type == 3){
            title = "未填写计划总结(time)";
        }else {
            title = "个人计划总结列表(time)";
        }
        if(!StringUtils.isEmpty(timeType)){
            if("1".equals(timeType)){
                time = "本周";
            }else if("2".equals(timeType)){
                time = "本月";
            }else {
                Object startDate = map.get("startDate");
                Object endDate = map.get("endDate");
                if(!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)){
                    time = startDate+" - "+endDate;
                }else if (!StringUtils.isEmpty(startDate)) {
                    time = "自"+startDate+"起";
                }else if (!StringUtils.isEmpty(endDate)){
                    time = "截止"+endDate;
                }
            }
        }
        return title.replaceAll("time",time.replaceAll("/","-"));
    }

    //根据前端界面选择的部门和人员来设置查询条件，支持行政人员（无需部长）
    private void addSecurityByFore(Map map) {
        if(map.get("currentUserId") == null){ //当页面没有选具体人员时，查询部门
            User user = AppUtil.getUser();
            Object pcompanyCode = map.get(IConst.COMPANY_CODE);//获取参数中的公司编码
            String companyCode = user.getCompanyCode();//获取当前用户的公司编码
            if (AppUtil.isRoleType(IConst.ROLE_TYPE_JT) && MapUtils.getInteger(map, "currentDeptId") == null) { //如果有集团权限，并且没有在前端选择部门，则查询所有
                return;
            }
            map.put("companyCode", pcompanyCode != null ? pcompanyCode : companyCode);//优先取参数中的公司编码
            if (user.getCurrentDeptQx() || AppUtil.isRoleCode(IConst.ROLE_CODE_ZW) || AppUtil.isRoleType(IConst.ROLE_TYPE_XZ)) {
                //如果是政委部门
                if(IConst.ROLE_CODE_ZW.equals(user.getDept().getCode())){
                    //前台没有传递指定部门，则查询当前政委所管理的所有部门下人员，否则，查看当前部门及其子部门（政委管理的）
                    String deptCode = map.get("deptCode") != null ? String.valueOf(map.get("deptCode")) : "YW";
                    if(map.get("currentDeptId") == null){
                        List<Map<String, Object>> deptList = deptZwMapper.listDeptInfoByParam(null, user.getId(), deptCode);
                        if(CollectionUtils.isEmpty(deptList)){
                            throw new QinFeiException(1002, "当前政委没有绑定对应部门！");
                        }
                        List<Integer> deptIds = new ArrayList<>();
                        for(Map<String, Object> dept : deptList){
                            deptIds.add(Integer.parseInt(String.valueOf(dept.get("id"))));
                        }
                        map.put("deptIds", org.apache.commons.lang3.StringUtils.join(deptIds, ","));
                    }else {
                        String deptIds = userService.getChilds(Integer.parseInt(String.valueOf(map.get("currentDeptId"))));
                        if (org.apache.commons.lang3.StringUtils.isNotEmpty(deptIds) && deptIds.indexOf("$,") > -1) {
                            deptIds = deptIds.substring(2);
                        }
                        map.put("deptIds", org.apache.commons.lang3.StringUtils.join(deptZwMapper.listChildDeptIdByUserId(deptIds, user.getId()), ","));
                        map.remove("currentDeptId");
                    }
                }else {
                    Integer deptId = MapUtils.getInteger(map, "currentDeptId");//页面选择的部门
                    deptId = deptId == null ? user.getDeptId() : deptId;
                    String deptIds = "";
                    if (deptId != null) {
                        deptIds = userService.getChilds(deptId);
                        if (deptIds.indexOf("$,") > -1) {
                            deptIds = deptIds.substring(2);
                        }
                        map.put("deptIds", deptIds);
                    }
                }
            } else {
                map.put("currentUserId", user.getId()); //只统计当前用户自己的数据
            }
        }
    }

    //设置上一个工作日
    private void setLastWorkDate(Map<String, Object> param){
        if(param.get("timeQuantum") == null){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -1);//往前推1天，防止长假
            Date endDete = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -15);//往前推15天，防止长假
            Date startDete = calendar.getTime();
            Map<String, List<String>> workDateMap = workDateService.listDateByRange(DateUtils.format(startDete, "yyyy-MM-dd"), DateUtils.format(endDete, "yyyy-MM-dd"));
            if(workDateMap != null && CollectionUtils.isNotEmpty(workDateMap.get("workDate"))){
                String workDate = workDateMap.get("workDate").get(workDateMap.get("workDate").size() - 1);
                if(!StringUtils.isEmpty(workDate)){
                    param.put("timeQuantum", 3);
                    param.put("startDate", workDate.replaceAll("-","/"));
                    param.put("endDate", workDate.replaceAll("-","/"));
                }
            }
        }
    }
}
