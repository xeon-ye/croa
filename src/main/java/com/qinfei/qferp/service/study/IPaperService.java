package com.qinfei.qferp.service.study;

import com.qinfei.qferp.entity.study.Paper;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * @CalssName: IPaperService
 * @Description: 试卷接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 14:38
 * @Version: 1.0
 */
public interface IPaperService {
    String CACHE_KEY = "paper";

    //新增试卷
    void save(Paper paper);

    //修改试卷状态
    void updateStateById(byte state, int id);

    //修改试卷
    void update(Paper paper);

    //根据参数获取试卷总数
    int getCountByParam(Map<String, Object> param);

    //分页获取试卷内容
    PageInfo<Paper> listPaper(Map<String, Object> param, Pageable pageable);

    //获取试卷信息(试卷编辑、试卷考试调用)
    Map<String, Object> getPaperDetailById(int paperId, boolean orderFlag, boolean examFlag);

    //查询自己测试的试卷列表
    int getUserExamTotal(Map<String, Object> param);

    //查询自己测试的试卷列表
    PageInfo<Map<String, Object>> listUserExam(Map<String, Object> param, Pageable pageable);
}
