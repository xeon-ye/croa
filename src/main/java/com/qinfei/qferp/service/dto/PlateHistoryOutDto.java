package com.qinfei.qferp.service.dto;

import com.qinfei.qferp.entity.performance.PerformanceHistory;

import java.util.List;

/**
 * 返回plate和所有子节点
 */
public class PlateHistoryOutDto {
    private PerformanceHistory plate;
    private List<PerformanceHistory> childs;

    public PerformanceHistory getPlate() {
        return plate;
    }

    public void setPlate(PerformanceHistory plate) {
        this.plate = plate;
    }

    public List<PerformanceHistory> getChilds() {
        return childs;
    }

    public void setChilds(List<PerformanceHistory> childs) {
        this.childs = childs;
    }
}
