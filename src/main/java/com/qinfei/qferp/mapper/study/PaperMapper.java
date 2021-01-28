package com.qinfei.qferp.mapper.study;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.study.Paper;
import com.qinfei.qferp.entity.study.Question;
import com.qinfei.qferp.entity.sys.SysConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: PaperMapper
 * @Description: 考试试卷表
 * @Author: Xuxiong
 * @Date: 2020/03/13 0020 15:54
 * @Version: 1.0
 */
public interface PaperMapper extends BaseMapper<Paper, Integer> {
    //新增
    int save(Paper paper);

    //更新试卷状态
    int updateStateById(@Param("state") byte state, @Param("updateId") int updateId, @Param("id") int id);

    //修改
    int updateById(Paper paper);

    //根据参数获取试卷总数
    int getCountByParam(Map<String, Object> param);

    //根据参数获取试卷列表
    List<Paper> listPaperByParam(Map<String, Object> param);

    //查询自己测试的试卷列表
    int getUserExamTotal(Map<String, Object> param);

    //查询自己测试的试卷列表
    List<Map<String, Object>> listUserExam(Map<String, Object> param);

}
