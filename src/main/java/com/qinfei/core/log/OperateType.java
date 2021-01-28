package com.qinfei.core.log;

/**
 * Created by Administrator on 2018/1/12.
 * 操作类型
 * @author QinFei by gzw
 */

public enum  OperateType {

    ADD("添加"),DELETE("删除"),UPDATE("修改"),QUERY("查询");

    private String name ;


    OperateType(String name){
        this.name = name ;

    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
