package com.qinfei.qferp.service.dto;

import com.qinfei.qferp.service.impl.performance.PerformanceHistoryService;

import java.util.List;

/**
 * Created by 严鸿豪 on 2019/4/16 11:24.
 */
public class HistoryNodes {
    private Integer plateId;
    private Integer level;
    private Integer parentId;
    private Integer order;
    private float proportion;
    private String content;
    private String target;
    private String demand;
    private List<HistoryNodes> child;
    private int childSize;

    public Integer getPlateId() {
        return plateId;
    }

    public void setPlateId(Integer plateId) {
        this.plateId = plateId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public int getChildSize() {
        return childSize;
    }

    public void setChildSize(int childSize) {
        this.childSize = childSize;
    }

    public float getProportion() {
        return proportion;
    }

    public void setProportion(float proportion) {
        this.proportion = proportion;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDemand() {
        return demand;
    }

    public void setDemand(String demand) {
        this.demand = demand;
    }

    public List<HistoryNodes> getChild() {
        return child;
    }

    public void setChild(List<HistoryNodes> child) {
        this.child = child;
    }
}