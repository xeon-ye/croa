package com.qinfei.qferp.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @CalssName EasyExcelDataListener
 * @Description 数据读取监听
 * @Author xuxiong
 * @Date 2019/10/28 0028 14:07
 * @Version 1.0
 */
@Setter
@Getter
public class EasyExcelDataListener extends AnalysisEventListener {
    private List<Object> datas = new ArrayList<>();

    @Override
    public void invoke(Object o, AnalysisContext analysisContext) {
        this.datas.add(o);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
    }
}
