package com.qinfei.qferp.service.impl.propose;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.propose.Propose;
import com.qinfei.qferp.entity.propose.ProposeRemark;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.propose.ProposeMapper;
import com.qinfei.qferp.mapper.propose.ProposeRelationMapper;
import com.qinfei.qferp.mapper.propose.ProposeRemarkMapper;
import com.qinfei.qferp.mapper.sys.DeptZwMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.propose.IProposeService;
import com.qinfei.qferp.service.propose.IProposeTipsService;
import com.qinfei.qferp.service.workbench.IItemsService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelExportConvert;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * 建议模块接口实现类
 * @author tsf
 */
@Service
public class ProposeService implements IProposeService {

    @Autowired
    private ProposeMapper proposeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IItemsService itemsService;
    @Autowired
    private ProposeRelationMapper proposeRelationMapper;
    @Autowired
    private ProposeRemarkMapper proposeRemarkMapper;
    @Autowired
    private DeptZwMapper deptZwMapper;
    @Autowired
    private IProposeTipsService proposeTipsService;
    /**
     * 根据id查询建议信息
     * @param id
     * @return
     */
    @Override
    public Propose queryProposeById(int id) {
        return proposeMapper.getById(id);
    }

    /**
     * 查询所有的建议信息
     * @param map
     * @param pageable
     * @return
     */
    @Override
    public PageInfo<Map> queryPropose(Map map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        map.put("companyCode",AppUtil.getUser().getDept().getCompanyCode());
        map.put("userId",AppUtil.getUser().getId());
        map.put("name",AppUtil.getUser().getName());
        List<Map> list =  proposeMapper.queryPropose(map);
        return new PageInfo<>(list);
    }

    /**
     * 只查询自己的建议信息(政委查部门下的人员)
     * @param map
     * @param pageable
     * @return
     */
    @Override
    public PageInfo<Propose> queryProposeByself(Map map, Pageable pageable) {
        User user = AppUtil.getUser();
        List<Role> roles = user.getRoles();
        map.put("companyCode",user.getCompanyCode());
        map.put("userId",user.getId());
        map.put("name",user.getName());
        List<Propose> list = new ArrayList<>();
        if(roles!=null && roles.size()>0){
            for(Role role : roles){
                if(role.getCode().equals("ZW") && IConst.ROLE_CODE_ZW.equals(user.getDept().getCode())){
                    //对政委角色特殊处理
//                    map.put("flag",1);
                    //由于政委角色的特殊性，故暂取其部门id
                    /*Integer deptId = user.getDeptId();
                    String deptIds = userMapper.getChilds(deptId);
                    if (deptIds.indexOf("$,") > -1) {
                        deptIds = deptIds.substring(2);
                    }
                    map.put("deptIds", deptIds);*/
                    String deptCode = map.get("deptCode") != null ? String.valueOf(map.get("deptCode")) : null;
                    List<Map<String, Object>> deptList = deptZwMapper.listDeptInfoByParam(null, user.getId(), deptCode);
                    if(CollectionUtils.isEmpty(deptList)){
                        throw new QinFeiException(1002, "当前政委没有绑定对应部门！");
                    }
                    List<Integer> deptIds = new ArrayList<>();
                    for(Map<String, Object> dept : deptList){
                        deptIds.add(Integer.parseInt(String.valueOf(dept.get("id"))));
                    }
                    map.put("deptIds", org.apache.commons.lang3.StringUtils.join(deptIds, ","));
                    //政委查询建议方法
                    PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
                    list =  proposeMapper.queryProposeByZW(map);
                    break;
                }else{
//                    map.put("flag",0);
                    //除政委外人员查询建议方法
                    PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
                    list =  proposeMapper.queryProposeByself(map);
                }
            }
        }
        return new PageInfo<>(list);
    }

    /**
     * 保存建议信息
     * @param propose
     */
    @Override
    public Propose save(Propose propose) {
        try {
            propose.setState(0);
            User user = AppUtil.getUser();
            String companyCode = user.getCompanyCode();
            //发起人id
            Integer userId = user.getId();
            //发起人姓名
            Integer deptId = user.getDeptId();
            propose.setCompanyCode(companyCode);
            propose.setId(null);
            propose.setEntryTime(new Date());
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH )+1;
            propose.setYear(year);
            propose.setMonth(month);
            List<User> list = proposeRelationMapper.queryProposeUsers(propose.getProposeType());
            if(CollectionUtils.isNotEmpty(list)){
                String str = "";
                proposeMapper.insert(propose);
                for(int i=0;i<list.size();i++){
                    Items item = addDealItem(propose,userId,deptId,list.get(i));
                    str += item.getId()+",";
                }
                //对待办id进行特殊处理
                proposeMapper.updateProposeItems(str,propose.getId());
                //刷新建议提醒缓存
                proposeTipsService.updateProposeCache(userId,companyCode);
            }else {
                throw new QinFeiException(1002,"该建议没有负责人，请联系行政人员配置负责人！");
            }
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，保存建议出错啦，请联系技术人员");
        }
        return propose;
    }

    /**
     * 建议管理修改建议信息
     * @param propose
     */
    @Override
    public void update(Propose propose) {
        //处理结果:0同意,1不同意，2待议，3指定给其他人，4其它
        try {
            if(propose.getHandleResult()==3){
                //处理结果为指定给其他人（处理中）
                propose.setState(2);
                //待办变已办
                dealItem(propose);
                //添加指定人处理的待办事项
                Items item= addOtherPersonItem(propose,AppUtil.getUser());
                propose.setItemId(item.getId().toString());
            }else if(propose.getHandleResult()==2){
                //2处理中
                propose.setState(2);
            }else{
                //已处理
                Boolean cc = dealItem(propose);
                if(cc){
                    propose.setState(1);
                    //已确认：添加确认待办
                    Items item = addConfirmItem(propose,AppUtil.getUser());
                    propose.setItemId(item.getId().toString());
                }else{
                    throw new QinFeiException(1001,"处理建议异常");
                }
            }
            propose.setUpdateTime(new Date());
            propose.setHandleTime(new Date());
            proposeMapper.updatePropose(propose);
            saveProposeRemark(propose.getId(),propose.getHandleAdvice(),propose.getHandleResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断建议类别下是否有建议
     * @param id
     * @return
     */
    public List<Propose> queryAdviceById(Integer id){
        return proposeMapper.queryAdviceById(id);
    }

    /**
     * 确认驳回
     * @param propose
     */
    @Override
    public void confirmReject(Propose propose){
        Boolean flag = finishItem(propose);
        if(flag){
            propose.setUpdateTime(new Date());
            proposeMapper.updatePropose(propose);
        }
    }

    /**
     * 建议查询修改建议信息(如果待办已完成，就不能修改建议)
     * @param propose
     */
    @Override
    public void modifyPropose(Propose propose) {
         propose.setUpdateTime(new Date());
         proposeMapper.updatePropose(propose);
    }

    /**
     * 删除建议信息
     * @param id
     */
    @Override
    public void deletePropose(int id) {
        Propose advice = proposeMapper.getById(id);
        Boolean flag  = dealItem(advice);
        if(flag){
            proposeMapper.deletePropose(id,new Date());
        }
    }

    /**
     * 获取建议类别负责人
     * @return
     */
    @Override
    public List<Map> queryChargeUsers() {
        return proposeRelationMapper.queryChargeUsers(AppUtil.getUser().getCompanyCode());
    }

    /**
     * 导出建议
     * @param map
     * @param outputStream
     * @return
     */
    @Override
    public List<Map> exportPropose(Map map, OutputStream outputStream) {
        List<Map> proposeList = proposeMapper.queryPropose(map);
        String [] titles = {"建议类型","提出人","年","月","录入时间","部门","状态","问题描述","期待解决方案","处理结果","解决人","解决时间","处理意见"};
        String [] obj = {"adviceType","name","year","month","entry_time","dept_name","state","problem_description","expect_solution","handle_result","handle_person","handle_time","id"};
        exportExcel("建议列表",titles,obj,proposeList,outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value)->{
            if(value!=null){
                if("entry_time".equals(field) || "handle_time".equals(field)){
                    cell.setCellValue(value.toString());
                }else if("state".equals(field)){
                    if((int) value ==0){
                        cell.setCellValue("未处理");
                    }else if((int) value  ==1){
                        cell.setCellValue("已处理");
                    }else if((int) value ==2){
                        cell.setCellValue("处理中");
                    }else if((int) value ==3){
                        cell.setCellValue("已确认");
                    }else{
                        cell.setCellValue("已驳回");
                    }
                }else if("handle_result".equals(field)){
                    if((int) value ==1){
                        cell.setCellValue("已处理");
                    }else if((int) value ==2){
                        cell.setCellValue("处理中");
                    }else if((int) value ==3){
                        cell.setCellValue("指定给其他人");
                    }else{
                        cell.setCellValue("其它");
                    }
                }else if("id".equals(field)){
                    int adviceId = (int)value;
                    List<ProposeRemark> list = proposeRemarkMapper.queryProposeRemark(adviceId,"asc");
                    if(CollectionUtils.isNotEmpty(list)){
                        StringBuilder buffer = new StringBuilder();
                        for(int i=0;i<list.size();i++){
                            ProposeRemark remark = list.get(i);
                            buffer.append(remark.getCreateName()+"(");
                            buffer.append(DateUtils.format(remark.getCreateDate(),"yyyy-MM-dd HH:mm:ss")+")");
                            buffer.append("\r\n");
                            buffer.append(remark.getRemark()+";");
                            if(i!=(list.size()-1)){
                                buffer.append("\r\n");
                            }
                        }
                        cell.setCellValue(buffer.toString());
                    }else {
                        cell.setCellValue("");
                    }
                }else{
                    cell.setCellValue(value.toString());
                }
            }
        });
        return proposeList;
    }

    /**
     * 导出未录入建议人员
     * @param map
     * @param outputStream
     * @return
     */
    @Override
    public List<Map> exportUserNoPropose(Map map, OutputStream outputStream) {
        Object object=map.get("timeRange");
        String timeRange="";
        if(!ObjectUtils.isEmpty(object)){
            timeRange=object.toString();
        }
        Integer timeAdjust=0;
        if(StringUtils.isNotEmpty(timeRange)){
            if(timeRange.equals(1)){
                timeAdjust=0;
            }else if(timeRange.equals(2)){
                timeAdjust=-1;
            }else {
                timeAdjust=-2;
            }
        }else{
            timeAdjust=0;
        }
        map.put("timeAdjust",timeAdjust);
        List<Map> list = userMapper.queryDeptByCompany(map);
        String [] titles = {"部门名称","未录入建议人员"};
        String [] obj = {"deptName","userName"};
        ExcelUtil.exportExcel("未录入建议人员",titles,obj,list,outputStream,"yyyy-MM-dd",(sheet, rowIndex, cellIndex, row, cell, field, value)->{
            if(value!=null){
                cell.setCellValue(value.toString());
            }
        });
        return list;
    }

    /**
     * 处理多人待办
     * @param propose
     */
    private Boolean dealItem(Propose propose){
        try {
            Boolean flag = false;
            //获取建议类型的负责人
            List<User> list = proposeRelationMapper.queryProposeUsers(propose.getProposeType());
            if(list.size()>=1){
                //获取存在的待办(可能有null指针异常)
                String item = propose.getItemId();
                if(StringUtils.isEmpty(item)){
                    return false;
                }
                String [] ids = item.split(",");
                if(ids.length>0){
                    for (int i = 0 ; i<ids.length;i++){
                        propose.setItemId(ids[i]);
                        Boolean uu = finishItem(propose);
                        if(!uu){
                            //未成功处理代办
                            flag = false;
                            break;
                        }else{
                            flag = true;
                        }
                    }
                }
            }else{
                //老数据处理(无负责人)
                Boolean cc = finishItem(propose);
                if(cc){
                    flag = true;
                }
            }
            return flag;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将待办事项变成已办事项
     * @param entity
     */
    private Boolean finishItem(Propose entity){
        try {
            if(entity.getItemId()!=null){
                Items items = new Items();
                String itemId = entity.getItemId();
                if(itemId.indexOf(",")!=-1){
                    itemId = itemId.substring(0,itemId.indexOf(","));
                }
                items.setId(Integer.parseInt(itemId));
                items.setTransactionState(Const.ITEM_Y);
                itemsService.finishItems(items);
                return true;
            }else{
                return false;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }
    //确认待办
    private Items addConfirmItem(Propose entity, User user){
        Items items = new Items();
        items.setItemName(entity.getProblemDescription()+"-等待确认");
        items.setItemContent("您的建议需要确认");
        items.setWorkType("建议处理确认");
        //发起人
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/propose/propose_list?flag=2&id="+entity.getId());
        items.setFinishAddress("/propose/propose_list?flag=3&id="+entity.getId());
        //接收人
        items.setAcceptWorker(entity.getUserId()) ;
        items.setAcceptDept(entity.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items ;
    }
    //指定给其他人待办
    private Items addOtherPersonItem(Propose entity, User user){
        Items items = new Items();
        items.setItemName(entity.getProblemDescription()+"-等待处理");
        items.setItemContent("您有建议需要处理");
        items.setWorkType("建议处理");
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/propose/propose_list?flag=1&id="+entity.getId());
        items.setFinishAddress("/propose/propose_list?flag=3&id="+entity.getId());
        items.setAcceptWorker(entity.getAppointPerson()) ;
        items.setAcceptDept(null);
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items ;
    }

    /**
     * 添加建议推送待办
     * @param entity 建议实体类
     * @param id 发起人id
     * @param deptId 发起人部门id
     * @param user 接收人
     * @return
     */
    private Items addDealItem(Propose entity,Integer id,Integer deptId,User user){
        Items items = new Items();
        items.setItemName(entity.getProblemDescription()+"-等待处理");
        items.setItemContent("您有建议需要处理");
        items.setWorkType("建议处理");
        items.setInitiatorWorker(id);
        items.setInitiatorDept(deptId);
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/propose/propose_list?flag=1&id="+entity.getId());
        items.setFinishAddress("/propose/propose_list?flag=3&id="+entity.getId());
        items.setAcceptWorker(user.getId()) ;
        items.setAcceptDept(null);
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items ;
    }

    /**
     * 保存建议意见
     * @param adviceId
     * @param remark
     */
    private void saveProposeRemark(Integer adviceId,String remark,Integer state){
        try {
            ProposeRemark obj = new ProposeRemark();
            String result = "";
            if(state==1){
                result = "已处理";
            }else if(state==2){
                result = "处理中";
            }else if(state==3){
                result = "指定给其他人";
            }else{
                result = "其他";
            }
            obj.setHandleResult(result);
            obj.setAdviceId(adviceId);
            if(StringUtils.isNotEmpty(remark)){
                obj.setRemark(remark);
            }else{
                obj.setRemark(" ");
            }
            User user = AppUtil.getUser();
            obj.setCreateId(user.getId());
            obj.setCreateName(user.getName());
            obj.setCreateDate(new Date());
            obj.setUpdateId(user.getId());
            obj.setUpdateDate(new Date());
            proposeRemarkMapper.saveProposeRemark(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void exportExcel(String title, String[] headers, String[] fields, List mapList, OutputStream out, String pattern, ExcelExportConvert excelConvert) {
        //声明一个工作簿
        int size = mapList.size();
        final int sheetNum = (int) Math.ceil(size / 60000f);
        HSSFWorkbook workbook = new HSSFWorkbook();
        for (int n = 0; n < sheetNum; n++) {
            //生成一个表格
            HSSFSheet sheet = workbook.createSheet(title + n);
            //生成一个样式，用来设置标题样式
            HSSFCellStyle style = workbook.createCellStyle();
            //设置这些样式
            style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.SKY_BLUE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setAlignment(HorizontalAlignment.CENTER);
            //生成一个字体
            HSSFFont font = workbook.createFont();
            font.setColor(HSSFColor.HSSFColorPredefined.VIOLET.getIndex());
            font.setBold(true);
            //把字体应用到当前的样式
            style.setFont(font);
            // 生成并设置另一个样式,用于设置内容样式
            HSSFCellStyle style2 = workbook.createCellStyle();
            style2.setWrapText(true);
            //产生表格标题行
            HSSFRow row = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                HSSFCell cell = row.createCell(i);
                cell.setCellStyle(style);
                HSSFRichTextString text = new HSSFRichTextString(headers[i]);
                cell.setCellValue(text);
                //设置表格默认列宽度为15个字符
                sheet.setColumnWidth(i,256*20);
                //处理意见长度比较长
                if(i==headers.length-1){
                    sheet.setColumnWidth(i,256*56);
                }
            }
            for (int i = n * 60000; i < size && i < (n + 1) * 60000; i++) {
                int index=i - 60000 * n;
                Map<String, Object> map = (Map<String, Object>) mapList.get(i);
                try {
                    row = sheet.createRow(index + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                for (int j = 0; j < fields.length; j++) {
                    HSSFCell cell = row.createCell(j);
                    cell.setCellStyle(style2);
                    excelConvert.convert(sheet, index, j, row, cell, fields[j], map.get(fields[j]));
                }
            }
        }
        try {
            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
