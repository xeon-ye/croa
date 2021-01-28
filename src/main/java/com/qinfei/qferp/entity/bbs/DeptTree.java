package com.qinfei.qferp.entity.bbs;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ztree 的数据结构
 */
@Getter
@Setter
public class DeptTree implements Serializable {
    private Integer id;
    private Integer pid;
    private String name;
    private String icon;
    private List<DeptTree> children=new ArrayList<>();

    @Override
    public String toString() {
        return "DeptTree{" +
                "id=" + id +
                ", pid=" + pid +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", children=" + children +
                '}';
    }
}
