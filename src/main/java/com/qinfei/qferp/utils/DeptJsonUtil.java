package com.qinfei.qferp.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qinfei.qferp.entity.sys.Dept;

import java.util.ArrayList;
import java.util.List;

public class DeptJsonUtil {

    private List<Dept> deptList;
    private final JSONArray array = new JSONArray() ;
    public List<Object> list = new ArrayList<Object>();

    public JSONArray jsonList(List<Dept> paramList){
        this.deptList = paramList;
        for (Dept x : paramList) {
            JSONObject obj = new JSONObject() ;
            if(x.getParentId()==0){
                obj.put("value", x.getId());
                obj.put("name", x.getName());
                obj.put("level", x.getLevel());
                obj.put("code", x.getCode());
                obj.put("type", x.getType());
                obj.put("pid", x.getParentId());
                obj.put("children", treeChild(x.getId()));
                obj.put("mgrName",x.getMgrName());
                obj.put("mgrId",x.getMgrId());
                obj.put("companyCode",x.getCompanyCode());
                obj.put("companyName",x.getCompanyCodeName());
                obj.put("mgrLeaderId",x.getMgrLeaderId());
                obj.put("mgrLeaderName",x.getMgrLeaderName());
                array.add(obj) ;
            }
        }
        return array;
    }

    public JSONArray jsonDeptList(List<Dept> paramList){
        this.deptList = paramList;
        Dept x = getLeastLevelDept(paramList);
        JSONObject obj = new JSONObject();
        obj.put("value", x.getId());
        obj.put("name", x.getName());
        obj.put("level", x.getLevel());
        obj.put("code", x.getCode());
        obj.put("type", x.getType());
        obj.put("pid", x.getParentId());
        obj.put("children", treeChild(x.getId()));
        obj.put("mgrName",x.getMgrName());
        obj.put("mgrId",x.getMgrId());
        obj.put("companyCode",x.getCompanyCode());
        obj.put("companyName",x.getCompanyCodeName());
        obj.put("mgrLeaderId",x.getMgrLeaderId());
        obj.put("mgrLeaderName",x.getMgrLeaderName());
        array.add(obj);
        return array;
    }

    //获取list中部门等级最小的部门
    public Dept getLeastLevelDept(List<Dept> paramList){
        Dept d = paramList.get(0);
        for (Dept dept : paramList) {
            if(dept.getLevel()<d.getLevel()){
                d = dept;
            }
        }
        return d;
    }

    private JSONArray treeChild(int id){
        JSONArray arrays = new JSONArray() ;
        for(Dept a:deptList){
            JSONObject childObj = new JSONObject() ;
            if(a.getParentId() == id){
                childObj.put("value", a.getId());
                childObj.put("name", a.getName());
                childObj.put("code", a.getCode());
                childObj.put("type", a.getType());
                childObj.put("level", a.getLevel());
                childObj.put("pid", a.getParentId());
                childObj.put("children", treeChild(a.getId()));
                childObj.put("mgrName",a.getMgrName());
                childObj.put("mgrId",a.getMgrId());
                childObj.put("companyCode",a.getCompanyCode());
                childObj.put("companyName",a.getCompanyCodeName());
                childObj.put("mgrLeaderId",a.getMgrLeaderId());
                childObj.put("mgrLeaderName",a.getMgrLeaderName());
                arrays.add(childObj);
            }
        }
        return arrays;
    }

}
