package com.qinfei.qferp.service.dto;

import com.qinfei.qferp.entity.performance.PerformancePlate;

import java.util.List;

/**
 * 返回plate和所有子节点
 */
public class PlateOutDto {
    private PerformancePlate plate;
    private List<PerformancePlate> childs;

    public PerformancePlate getPlate() {
        return plate;
    }

    public void setPlate(PerformancePlate plate) {
        this.plate = plate;
    }

    public List<PerformancePlate> getChilds() {
        return childs;
    }

    public void setChilds(List<PerformancePlate> childs) {
        this.childs = childs;
    }
}
