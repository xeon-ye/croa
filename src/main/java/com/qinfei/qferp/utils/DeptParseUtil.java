package com.qinfei.qferp.utils;

import com.qinfei.qferp.entity.sys.Dept;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DeptParseUtil {
    private List<Dept> nodes = new ArrayList<>();
    private final Queue<Dept> deptsQueue = new LinkedList<Dept>();
    public DeptParseUtil(List<Dept> allDepts){
        if(allDepts != null){
            this.nodes = allDepts;
        }
    }

    /**
     * 方法入口
     * @param dept
     */
    public void parse(Dept dept){
        parseDepts(dept);
    }

    private void parseDepts(Dept root){
        if(root.getId() == null){
            return;
        }
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getParentId().intValue() == root.getId().intValue() && nodes.get(i).getParentId().intValue() != nodes.get(i).getId().intValue()) {
//                root.getDepts().add(nodes.get(i));
                root.getChildDepts().add(nodes.get(i));
                deptsQueue.offer(nodes.get(i));
            }
        }
        if (deptsQueue.size() == 0) {
            return;
        }
        parseDepts(deptsQueue.poll());
    }

}
