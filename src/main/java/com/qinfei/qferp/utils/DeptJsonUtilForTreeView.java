package com.qinfei.qferp.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qinfei.qferp.entity.sys.Dept;

import java.util.ArrayList;
import java.util.List;

public class DeptJsonUtilForTreeView {

    private List<Dept> deptList;
    private final JSONArray array = new JSONArray();
    public List<Object> list = new ArrayList<Object>();

    public JSONArray jsonList(List<Dept> paramList) {
        this.deptList = paramList;
        for (Dept x : paramList) {
            JSONObject obj = new JSONObject();
            if (x.getParentId() == 0) {
                obj.put("id", x.getId());
                obj.put("text", x.getName());
                obj.put("level", x.getLevel());
                obj.put("parentId", x.getParentId());
                obj.put("nodes", treeChild(x.getId()));
                array.add(obj);
            }
        }
//        System.out.println("******************array="+array);
        return array;
    }

    public JSONArray jsonListX(List<Dept> paramList) {
        this.deptList = paramList;
        for (Dept x : paramList) {
            JSONObject obj = new JSONObject();
            obj.put("id", x.getId());
            obj.put("text", x.getName());
            obj.put("level", x.getLevel());
            obj.put("parentId", x.getParentId());
            obj.put("nodes", treeChild(x.getId()));
            array.add(obj);
        }
//        System.out.println("******************array="+array);
        return array;
    }

    /**
     * 获取指定部门下的树形数据；
     *
     * @param paramList：部门集合数据；
     * @param deptId：部门ID；
     * @return ：树形结构数据；
     */
    public JSONArray jsonList(List<Dept> paramList, int deptId) {
        this.deptList = paramList;
        for (Dept x : paramList) {
            JSONObject obj = new JSONObject();
            if (x.getId() == deptId) {
                obj.put("id", x.getId());
                obj.put("text", x.getName());
                obj.put("level", x.getLevel());
                obj.put("parentId", x.getParentId());
                obj.put("nodes", treeChild(x.getId()));
                array.add(obj);
            }
        }
//        System.out.println("******************array="+array);
        return array;
    }


    private JSONArray treeChild(int id) {
        JSONArray arrays = new JSONArray();
        for (Dept a : deptList) {
            JSONObject childObj = new JSONObject();
            if (a.getParentId() == id) {
                childObj.put("id", a.getId());
                childObj.put("text", a.getName());
                childObj.put("level", a.getLevel());
                childObj.put("parentId", a.getParentId());
                childObj.put("nodes", treeChild(a.getId()));
                arrays.add(childObj);
            }
        }
//        System.out.println("******************arrays="+arrays);
        return arrays;


    }

}
