package com.qinfei.qferp.service.performance;

import com.qinfei.qferp.entity.performance.PerformanceHistory;
import com.qinfei.qferp.service.dto.HistoryNodes;
import com.qinfei.qferp.service.dto.PlateHistoryOutDto;

import java.util.List;

public interface IPerformanceHistoryService {
    List<PlateHistoryOutDto> selectChild(Integer plateId, Integer schId);

    PerformanceHistory selectBySchIdAndPlateId(Integer plateId, Integer schId, Integer plateLevel);

    String del(Integer schId);

    /**
     * 根据方案id查询所有绩效模板
     *
     * @param schId
     * @return
     */
    List<HistoryNodes> listPlate(Integer schId);
}
