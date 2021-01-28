package com.qinfei.qferp.service.impl.sys;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.sys.DeptZw;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.sys.DeptZwMapper;
import com.qinfei.qferp.service.sys.IDeptZwService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @CalssName DeptZwService
 * @Description 部门政委服务接口
 * @Author xuxiong
 * @Date 2019/12/19 0019 14:22
 * @Version 1.0
 */
@Service
public class DeptZwService implements IDeptZwService{
    @Autowired
    private DeptZwMapper deptZwMapper;
    @Autowired
    IUserService userService;

    @Transactional
    @Override
    public void bindingDeptZw(DeptZw deptZw) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            deptZw.setCreateId(user.getId());
            deptZw.setUpdateId(user.getId());
            changeDeptZw(deptZw); //编辑部门政委信息
            //如果同步子部门政委信息
            if(deptZw.getDeptAsync()){
                String deptIds = userService.getChilds(deptZw.getDeptId());
                if (StringUtils.isNotEmpty(deptIds) && deptIds.indexOf("$,") > -1) {
                    deptIds = deptIds.substring(2);
                    if(deptIds.split(",").length > 1){
                        deptIds = deptIds.replace(deptZw.getDeptId()+",", "");
                    }else {
                        deptIds = deptIds.replace(deptZw.getDeptId()+"", "");
                    }
                }
                List<String> deptIdArr = Arrays.asList(deptIds.split(","));
                if(StringUtils.isNotEmpty(deptIds) && CollectionUtils.isNotEmpty(deptIdArr)){
                    deptZwMapper.batchUpdateStateByDeptId(-9, deptIds, user.getId());//将子部门政委全部删除
                    //获取父级部门设置的政委信息，同步到子部门
                    List<DeptZw> deptZwList = deptZwMapper.listDeptZwByParam(null, deptZw.getDeptId());
                    if(CollectionUtils.isNotEmpty(deptZwList)){
                        List<DeptZw> addDeptZwList = new ArrayList<>();
                        for(String deptId : deptIdArr){
                            for(DeptZw tempDeptZw : deptZwList){
                                DeptZw targetDeptZw = new DeptZw();
                                BeanUtils.copyProperties(tempDeptZw, targetDeptZw);
                                targetDeptZw.setDeptId(Integer.parseInt(deptId));
                                targetDeptZw.setCreateId(user.getId());
                                targetDeptZw.setUpdateId(user.getId());
                                addDeptZwList.add(targetDeptZw);
                            }
                        }
                        deptZwMapper.saveBatch(addDeptZwList);
                    }
                }
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "很抱歉，政委绑定出现异常，请联系技术人员！");
        }
    }

    //编辑部门政委信息
    private void changeDeptZw(DeptZw deptZw){
        if(CollectionUtils.isNotEmpty(deptZw.getAddUserList())){
            List<DeptZw> addDeptZwList = new ArrayList<>();
            for(Integer userId : deptZw.getAddUserList()){
                DeptZw targetDeptZw = new DeptZw();
                BeanUtils.copyProperties(deptZw, targetDeptZw);
                targetDeptZw.setUserId(userId);
                addDeptZwList.add(targetDeptZw);
            }
            if(CollectionUtils.isNotEmpty(addDeptZwList)){
                deptZwMapper.saveBatch(addDeptZwList);
            }
        }
        if(CollectionUtils.isNotEmpty(deptZw.getDelUserList())){
           deptZwMapper.batchUpdateState(-9, deptZw.getDeptId(),deptZw.getUpdateId(),deptZw.getDelUserList());
        }
    }

    @Override
    public List<User> listUserByDeptAndRole(Map<String, Object> param) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw  new QinFeiException(1001, "请先登录");
            }
            if(param.get("companyCode") == null){
                param.put("companyCode", user.getCompanyCode()); //如果前端没有传递公司代码，则取当前用户的
            }
            return deptZwMapper.listUserByDeptAndRole(param);
        }catch (Exception e){
            return new ArrayList<>();
        }
    }

    @Override
    public List<User> listZwUserByDeptAndRole(Map<String, Object> param) {
        List<User> result = new ArrayList<>();
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw  new QinFeiException(1001, "请先登录");
            }
            //如果前台没有传递指定部门，则查询当前政委所管理的所有部门下人员，否则，查看当前部门及其子部门（政委管理的）
            if(param.get("deptId") == null || "".equals(param.get("deptId"))){
                String deptCode = param.get("deptCode") != null ? String.valueOf(param.get("deptCode")) : null;
                List<Map<String, Object>> deptList = deptZwMapper.listDeptInfoByParam(null, user.getId(), deptCode);
                if(CollectionUtils.isNotEmpty(deptList)){
                    List<Integer> deptIds = new ArrayList<>();
                    for(Map<String, Object> dept : deptList){
                        deptIds.add(Integer.parseInt(String.valueOf(dept.get("id"))));
                    }
                    param.put("deptIds", deptIds);
                }
            }else {
                String deptIds = userService.getChilds(Integer.parseInt(String.valueOf(param.get("deptId"))));
                if (StringUtils.isNotEmpty(deptIds) && deptIds.indexOf("$,") > -1) {
                    deptIds = deptIds.substring(2);
                }
                param.put("deptIds", deptZwMapper.listChildDeptIdByUserId(deptIds, user.getId()));
                param.remove("deptId");
            }

            result = deptZwMapper.listZwUserByDeptAndRole(param);
        }catch (Exception e){
           e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<DeptZw> listUserByDeptId(Integer deptId) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw  new QinFeiException(1001, "请先登录");
            }
            return deptZwMapper.listDeptZwByParam(null, deptId);
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<User> listUserByParam(Integer deptId) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw  new QinFeiException(1001, "请先登录");
            }
            return deptZwMapper.listUserByParam(null, deptId);
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> listDeptTreeByZw(String deptCode, Integer mgrFlag) {
        List<Map<String, Object>> result = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        List<Map<String, Object>> deptList =  deptZwMapper.listDeptInfoByParam(null, user.getId(), deptCode);
        //如果是部门负责人
        if(mgrFlag != null && mgrFlag == 1 && user.getId().equals(user.getDept().getMgrId())){
            String zwChildDept = userService.getChilds(user.getDept().getId());
            if (StringUtils.isNotEmpty(zwChildDept) && zwChildDept.indexOf("$,") > -1) {
                zwChildDept = zwChildDept.substring(2);
            }
            List<Map<String, Object>> tempDeptList = deptZwMapper.listDeptInfoByDeptIds(zwChildDept);
            deptList.addAll(tempDeptList);
        }
        if(CollectionUtils.isNotEmpty(deptList)){
            List<Map<String, Object>> deptSet = new ArrayList<>();
            Map<Integer, Map<String, Object>> deptMap = new HashMap<>();
            for(Map<String, Object> dept : deptList){
                if(!deptMap.keySet().contains(Integer.parseInt(String.valueOf(dept.get("id"))))){
                    deptMap.put(Integer.parseInt(String.valueOf(dept.get("id"))), dept);
                    deptSet.add(dept);
                }
            }
            for(Map<String, Object> dept : deptSet){
                Integer deptId = Integer.parseInt(String.valueOf(dept.get("id")));
                Integer parentId = Integer.parseInt(String.valueOf(dept.get("parentId")));
                if(deptMap.get(parentId) != null){
                    if(deptMap.get(parentId).get("nodes") == null){
                        deptMap.get(parentId).put("nodes", new ArrayList<Map<String, String>>());
                    }
                    ((List)deptMap.get(parentId).get("nodes")).add(dept);
                    deptMap.get(deptId).put("isLeaf", 1); //如果已经并入父级，则当前为叶子节点
                }
            }
            for(Map<String, Object> dept : deptSet){
                Integer deptId = Integer.parseInt(String.valueOf(dept.get("id")));
                if(deptMap.get(deptId).get("isLeaf") == null || "1".equals(deptMap.get(deptId).get("isLeaf"))){
                    result.add(deptMap.get(deptId));
                }
            }
        }
        if(result.size() > 1){
            Map<String, Object> root = new HashMap<>();
            root.put("text", user.getDept().getCompanyCodeName());
            root.put("code", deptCode);
            root.put("companyCode", user.getDept().getCompanyCode());
            root.put("nodes", result);
            return Arrays.asList(root);
        }else {
            return result;
        }
    }
}
