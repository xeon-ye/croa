package com.qinfei.qferp.service.impl.performance;

import com.qinfei.qferp.entity.performance.PerformanceHistory;
import com.qinfei.qferp.entity.performance.PerformanceScheme;
import com.qinfei.qferp.mapper.performance.PerformanceHistoryMapper;
import com.qinfei.qferp.mapper.performance.PerformanceSchemeMapper;
import com.qinfei.qferp.service.dto.HistoryNodes;
import com.qinfei.qferp.service.dto.PlateHistoryOutDto;
import com.qinfei.qferp.service.performance.IPerformanceHistoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.summarizingInt;
import static java.util.stream.Collectors.toList;

@Service
public class PerformanceHistoryService implements IPerformanceHistoryService {

    @Autowired
    private PerformanceHistoryMapper performanceHistoryMapper;
    @Autowired
    private PerformanceSchemeMapper performanceSchemeMapper;

    @Override
    public List<PlateHistoryOutDto> selectChild(Integer plateId, Integer schId) {
        if (Objects.isNull(plateId)) return null;

        return performanceHistoryMapper.listPlateBySchIdAndParentId(schId, plateId)
                .stream()
                .map(e -> {
                    PlateHistoryOutDto plateOutDto = new PlateHistoryOutDto();
                    plateOutDto.setPlate(e);
                    List<PerformanceHistory> ls = performanceHistoryMapper
                            .listPlateBySchIdAndParentId(schId, e.getPlateId());
                    plateOutDto.setChilds(ls);
                    return plateOutDto;
                }).collect(toList());
    }

    @Override
    public PerformanceHistory selectBySchIdAndPlateId(Integer plateId, Integer schId, Integer plateLevel) {
        return performanceHistoryMapper.listPlateByPlateIdAndSchId(schId, plateId, plateLevel);
    }

    @Override
    @Transactional
    public String del(Integer schId) {
        List<PerformanceScheme> list = performanceSchemeMapper.selectBySchId(schId);
        String message= "";
        if(CollectionUtils.isNotEmpty(list)){
            StringBuffer buffer = new StringBuffer();
            for(PerformanceScheme scheme :list){
                buffer.append(scheme.getProName());
                buffer.append("|");
            }
            if(!StringUtils.isEmpty(buffer.toString())){
                message = String.format("抱歉，该方案已关联【%s】计划，不能删除，请解除绑定再删除",buffer.toString());
            }
        }else {
           performanceHistoryMapper.delSchmeById(schId);
           performanceSchemeMapper.delSchmeById(schId);
        }
        return message;
    }

    //找到所有子节点
    @Override
    public List<HistoryNodes> listPlate(Integer schId) {
        return performanceHistoryMapper.selectBySchIdAndPlateLv(schId, 0).stream()
                .map(history -> {
                    int parentId = history.getPlateId();

                    HistoryNodes top = new HistoryNodes();
                    top.setPlateId(history.getPlateId());
                    top.setLevel(history.getPlateLevel());
                    top.setParentId(history.getPlateParent());
                    top.setOrder(history.getPlateOrder());
                    top.setContent(history.getPlateContent());
                    top.setProportion(history.getPlateProportion());
                    top.setTarget(history.getPlateTarget());
                    top.setDemand(history.getPlateDemand());
                    //找到lv 1节点
                    List<HistoryNodes> nodeLv1List = performanceHistoryMapper.listPlateBySchIdAndParentId(schId, parentId).stream()
                            .map(lv1 -> {
//                                int lv1ParentId = lv1.getPlateId();
                                HistoryNodes nodeLv1 = new HistoryNodes();
                                nodeLv1.setPlateId(lv1.getPlateId());
                                nodeLv1.setLevel(lv1.getPlateLevel());
                                nodeLv1.setParentId(lv1.getPlateParent());
                                nodeLv1.setOrder(lv1.getPlateOrder());
                                nodeLv1.setContent(lv1.getPlateContent());
                                nodeLv1.setProportion(lv1.getPlateProportion());
                                nodeLv1.setTarget(lv1.getPlateTarget());
                                nodeLv1.setDemand(lv1.getPlateDemand());
                                //找到lv 2节点
//                                List<HistoryNodes> nodeLv2 = performanceHistoryMapper
//                                        .listPlateBySchIdAndParentId(schId, lv1ParentId)
//                                        .stream().map(lv2 -> {
//                                            HistoryNodes child = new HistoryNodes();
//                                            child.setProportion(lv2.getPlateProportion());
//                                            child.setContent(lv2.getPlateContent());
//                                            child.setTarget(lv2.getPlateTarget());
//                                            child.setDemand(lv2.getPlateDemand());
//                                            return child;
//                                        }).collect(toList());
//                                nodeLv1.setChild(nodeLv2);
//                                nodeLv1.setChildSize(nodeLv2.size());
                                return nodeLv1;
                            }).collect(Collectors.toList());
                    top.setChildSize(nodeLv1List.size());
                    top.setChild(nodeLv1List);
//                    int collect = nodeLv1List.stream().mapToInt(HistoryNodes::getChildSize).sum();
//                    top.setChildSize(collect);
                    return top;
                }).collect(Collectors.toList());
    }
}
