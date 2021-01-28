package com.qinfei.core.data;

import com.qinfei.core.entity.Dict;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("SameParameterValue")
public enum DictiEnum {
    薪资范围("薪资范围", "01", false), 学历("学历", "02", false), 部门("部门", "03", false);
    // 成员变量  
    private String typeName;
    private String typeCode;
    private boolean tree;
    //数据字典
    private List<Dict> dicties = new ArrayList<Dict>();

    // 构造方法
    DictiEnum(String name, String code, boolean tree) {
        this.typeName = name;
        this.typeCode = code;
        this.tree = tree;
    }

    //加载数据字典
    public void load(List<Dict> dicties) {
        //设置只读
        this.dicties = Collections.unmodifiableList(dicties);
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public boolean isTree() {
        return tree;
    }

    public void setTree(boolean tree) {
        this.tree = tree;
    }

    public List<Dict> getDicties() {
        return dicties;
    }
}