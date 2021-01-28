package com.qinfei.qferp.service.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by yanhonghao on 2019/4/25 10:12.
 */
@Setter
@Getter
public class PerformancePKProfitDto {
    private int id;
    private int pkType;
    private String name;
    @JSONField(format = "yyyy-MM-dd")
    private Date startDate;
    @JSONField(format = "yyyy-MM-dd")
    private Date endDate;
    private List<PKBizPeople> peopleList;
    @JSONField(format = "yyyy")
    private Date startYear;
    private Collection topPeople;
    private String backgroundImg;

    @Setter
    @Getter
    @Builder
    public static class PKBizPeople {
        private int leftId;
        private String leftName;
        private int rightId;
        private String rightName;
        private int leftWin;
        private float leftProfit;
        private float rightProfit;
        private String leftRate;
        private String leftAvatar;
        private String rightAvatar;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPeopleDto {
        private int id;
        private String name;
        private float profit;
    }
}
