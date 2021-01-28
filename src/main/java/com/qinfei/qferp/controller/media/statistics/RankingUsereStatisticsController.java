package com.qinfei.qferp.controller.media.statistics;

import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.service.impl.mediauser.statistics.RankingUserStatisticsService;
import com.qinfei.qferp.service.mediauser.statistics.IMediaUserStatisticsService;
import com.qinfei.qferp.service.mediauser.statistics.IRankingUserStatisticsService;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rankingUsereStatistics")
class RankingUsereStatisticsController {
    @Autowired
    RankingUserStatisticsService rankingUserStatisticsService;

    /**
     * 获取前30名员工的利润信息
     * @return
     */
    @RequestMapping("/getSalesmanRanking")
    @ResponseBody
    @ApiOperation(value = "获取前30名员工的利润信息", notes = "获取前30名员工的利润信息\n" +"return data ;")
    @Log(opType = OperateType.QUERY, module = "业务统计", note = "获取前30名员工的利润信息")
    public Map<String, Object> getSalesmanRanking(@RequestParam Map map){
        return rankingUserStatisticsService.getSalesmanRanking(map);
    }

    /**
     * 获取所有部门的利润信息
     * @return
     */
    @RequestMapping("/getDeptRanking")
    @ResponseBody
    @ApiOperation(value = "获取所有部门的利润信息", notes = "获取所有部门的利润信息\n" +"return data ;")
    @Log(opType = OperateType.QUERY, module = "业务统计", note = "获取所有部门的利润信息")
    public List<Map<String, Object>> getDeptRanking(@RequestParam Map<String, Object> map){
        return rankingUserStatisticsService.getDeptRanking(map);
    }


    /**
     * 获取当前登录人的排名信息
     * @return
     */
    @RequestMapping("/getSelfRanking")
    @ResponseBody
    @ApiOperation(value = "获取当前登录人的排名信息", notes = "获取当前登录人的排名信息\n" +"return data ;")
    @Log(opType = OperateType.QUERY, module = "业务统计", note = "获取当前登录人的排名信息")
    public String getSelfRanking(@RequestParam Map map){
        return rankingUserStatisticsService.getSelfRanking(map);
    }

}
