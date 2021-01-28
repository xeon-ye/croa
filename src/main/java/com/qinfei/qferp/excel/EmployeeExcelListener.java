package com.qinfei.qferp.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.employ.EmployeeBasic;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.employ.EmployEntryMapper;
import com.qinfei.qferp.mapper.employ.EmployResourceMapper;
import com.qinfei.qferp.mapper.employ.EmployeeBasicMapper;
import com.qinfei.qferp.mapper.employ.EmployeeMapper;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.PostMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.utils.AppUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yanhonghao on 2019/4/29 16:22.
 */
public class EmployeeExcelListener extends AnalysisEventListener {

    private List<EmployeeExcelInfo> datas = new ArrayList<>();
    private List<String> resultMsg = new ArrayList<>();
    private int updateNum = 0;

    private final EmployeeMapper employeeMapper;
    private final EmployEntryMapper employEntryMapper;
    private final EmployeeBasicMapper employeeBasicMapper;
    private final DeptMapper deptMapper;
    private final UserMapper userMapper;
    private final EmployResourceMapper employResourceMapper;
    private final PostMapper postMapper;

    public EmployeeExcelListener(EmployeeMapper employeeMapper, EmployEntryMapper employEntryMapper, EmployeeBasicMapper employeeBasicMapper,
                                 DeptMapper deptMapper, UserMapper userMapper, EmployResourceMapper employResourceMapper,
                                 PostMapper postMapper) {
        this.employeeMapper = employeeMapper;
        this.employEntryMapper = employEntryMapper;
        this.employeeBasicMapper = employeeBasicMapper;
        this.deptMapper = deptMapper;
        this.userMapper = userMapper;
        this.employResourceMapper = employResourceMapper;
        this.postMapper = postMapper;
    }

    @Override
    public void invoke(Object object, AnalysisContext context) {
        EmployeeExcelInfo info = (EmployeeExcelInfo) object;
        if (info.getIndex() == 0) return;
        String empNum = info.getEmpNum();

        List<User> sysUserList = userMapper.getIdByPhoneAndName(info.getEmpPhone(), info.getEmpName());
        if (Objects.nonNull(sysUserList) && !sysUserList.isEmpty()) {
            if(sysUserList.size()==1){
                User user = sysUserList.get(0);
                info.setUserId(user.getId());
                info.setUserName(user.getUserName());
            }
        }

        User user = AppUtil.getUser();

        //部门id 若部门信息输入错误中断流程
        String deptName = info.getEmpDeptStr();
        String companyCode = user.getCompanyCode();

        List<Dept> depts = deptMapper.queryDeptByNameAndCompanyCode(companyCode, deptName);
        if (depts.size() > 0) {
            Integer deptId = depts.get(0).getId();
            info.setEmpDept(deptId);

            String postStr = info.getEmpPostStr();

            if (StringUtils.isEmpty(postStr)) throw new QinFeiException(50000, "职位信息不能为空");

            List<Integer> postIds = postMapper.findByCompanyCodeAndPostNameAndDeptId(companyCode, postStr, deptId);

            if (postIds.isEmpty()) throw new QinFeiException(50000, "请校验部门【" + deptName + "】下职位【" + postStr + "】是否存在");

            else info.setEmpPostId(postIds.get(0));

        } else throw new QinFeiException(50000, "【部门信息错误】没有找到【" + deptName + "】的相关信息");

        String ageStr = info.getEmpAgeStr();
        if (StringUtils.isNotEmpty(ageStr)) {
            try {
                Integer age = Integer.parseInt(ageStr);
                if(age>0 && age<120){
                    info.setEmpAge(Integer.valueOf(ageStr));
                }
            } catch (NumberFormatException e) {
                throw new QinFeiException(50000, "【年龄必须是整数，请检查年龄是否正确，并确认模板是否正确】");
            }
        }

        //婚姻状况
        String mar = info.getEmpMarriageStr();
        int empMarriage = 0;
        if (mar.equalsIgnoreCase("已婚")) empMarriage = 1;
        else if (mar.equalsIgnoreCase("离婚")) empMarriage = 2;
        else if (mar.equalsIgnoreCase("丧偶")) empMarriage = 3;
        info.setEmpMarriage(empMarriage);

        //性别
        String empSex = info.getEmpSex();
        if (StringUtils.isNotEmpty(empSex)) {
            int sex = 1;//1 男
            if (empSex.equalsIgnoreCase("女")) sex = 0;
            info.setSex(sex);
            info.setEmpGender(sex);
        }

        //生日
        String empBirthStr = info.getEmpBirthStr();
        if (StringUtils.isNotEmpty(empBirthStr)) {
            Date birth;
            try{
                if(empBirthStr.indexOf("\"") > 0){
                    empBirthStr = empBirthStr.replaceAll("\"", "");
                    birth = DateUtils.parse(empBirthStr, "yyyy-MM-dd");
                }else if(empBirthStr.indexOf("-") > 0){
                    birth = DateUtils.parse(empBirthStr, "yyyy-MM-dd");
                }else{
//                            我们期望这个日期解析出来是：2019/10/30,而结果却是43768.什么原因呢？这个数字是什么呢？是以1900年为原点，到2019年10月30日，之间经过的天数。
//                            知道这个后，就很好处理了，我们拿到1900年的日期，在这个日期上加上43768天即可
                    Calendar calendar = new GregorianCalendar(1900,0,-1);
                    Date d = calendar.getTime();
////                            然后，利用DateUtils的方法，加上天数（这个天数被转为了字符串，值为43768）
                    birth = DateUtils.getAfterDay(d,Integer.valueOf(empBirthStr));
                }
                info.setEmpBirth(birth);
            }catch (Exception e){
                throw new QinFeiException(50000, "【请您检查生日是否正确，并确认模板是否正确】");
            }

        }
        //入职日期
        String entryDateStr = info.getEntryDateStr();
        if (StringUtils.isNotEmpty(entryDateStr)) {
            Date entry;
            try{
                if(entryDateStr.indexOf("\"") > 0){
                    entryDateStr = entryDateStr.replaceAll("\"", "");
                    entry = DateUtils.parse(entryDateStr, "yyyy-MM-dd");
                }else if(entryDateStr.indexOf("-") > 0){
                    entry = DateUtils.parse(entryDateStr, "yyyy-MM-dd");
                }else{
//                            我们期望这个日期解析出来是：2019/10/30,而结果却是43768.什么原因呢？这个数字是什么呢？是以1900年为原点，到2019年10月30日，之间经过的天数。
//                            知道这个后，就很好处理了，我们拿到1900年的日期，在这个日期上加上43768天即可
                    Calendar calendar = new GregorianCalendar(1900,0,-1);
                    Date d = calendar.getTime();
////                            然后，利用DateUtils的方法，加上天数（这个天数被转为了字符串，值为43768）
                    entry = DateUtils.getAfterDay(d,Integer.valueOf(entryDateStr));
                }
                info.setEntryDate(entry);
            }catch (Exception e){
                throw new QinFeiException(50000, "【请您检查入职日期是否正确，并确认模板是否正确】");
            }
        }
        String empBirthday = info.getEmpBirthday();
        if (StringUtils.isNotEmpty(empBirthday)) info.setEmpBirthday(empBirthday.replaceAll("\"", ""));

        //查询推荐人信息
        Map<String, Object> map = new HashMap<>();
        map.put("name", info.getEmpRelativeName());
        List<User> relUserList = userMapper.listByParams(map);
        if (relUserList.size() > 0) {
            User relUser = relUserList.get(0);
            info.setEmpRelative(relUser.getId());
            info.setEmpRelativePhone(relUser.getPhone());
        }

        //紧急联系人电话
        String str = info.getEmpUrgent();
        if (StringUtils.isNotEmpty(str)) {
            Pattern r = Pattern.compile("(\\d+)");
            Matcher m = r.matcher(str);
            if (m.find()) {
                String phone = m.group(0);
                info.setEmpUrgentPhone(phone);
            }
        }

        //查询民族id
        Integer raceId = employResourceMapper.getIdByNationName(info.getEmpRaceStr() + "族");
        info.setEmpRace(raceId);

        info.setCreateId(user.getId());
        info.setCreateName(user.getName());
        info.setCreateTime(new Date());

        int state = 0;
        if (StringUtils.isNotEmpty(info.getEmpPositive())) state = 1;
        info.setState(state);

        //相同员工则更新操作
        Integer entryId = employeeBasicMapper.countByEmpCode(info.getEmpCode());
        if (Objects.nonNull(entryId)) {
            updateNum++;
            if (resultMsg.size() < 6)
                resultMsg.add("【员工数据已更新】【" + info.getEmpName() + ":" + empNum + "】\n");

            String empName = info.getEmpName();
            String empPhone = info.getEmpPhone();
            List<Integer> entryIds = employEntryMapper.selectByNameAndPhone(empName, empPhone);

//            if (entryIds.isEmpty())
//                throw new QinFeiException(50000, "录入错误,请检查员工【" + empName + "】对应的电话【" + empPhone + "】是否正确");
            info.setUpdateId(user.getId());
            info.setUpdateName(user.getName());
            info.setUpdateTime(new Date());

            //如果包含重复数据 删除较新的数据 保留老数据
            if (entryIds.size() > 1) {
                for (int i = 1; i < entryIds.size(); i++) {
                    info.setEntryId(entryIds.get(i));
                    employEntryMapper.deleteByEntryId(info);
                    employeeBasicMapper.deleteByEntryId(info);
                    employeeMapper.deleteByEntryId(info);
                }
            }
            info.setEntryId(entryId);
            updateNum = employEntryMapper.updateFromExcel(info);
//            employeeBasicMapper.updateFromExcel(info);
//            employeeMapper.updateFromExcel(info);
        } else {
            datas.add(info);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (datas.size() > 0) {
//            employEntryMapper.insertSelectiveFromExcel(datas);
//            employeeBasicMapper.insertSelectiveExcelBatch(datas);
//            employeeMapper.insertSelectiveFormExcel(datas);
            datas.clear();
        }
    }

    public List<EmployeeExcelInfo> getDatas() {
        return datas;
    }

    public void setDatas(List<EmployeeExcelInfo> datas) {
        this.datas = datas;
    }

    public List<String> getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(List<String> resultMsg) {
        this.resultMsg = resultMsg;
    }
}
