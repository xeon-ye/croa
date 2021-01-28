package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.sys.VersionHint;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: VersionHintMapper
 * @Description: 系统版本提示表
 * @Author: Xuxiong
 * @Date: 2020/1/20 0020 16:00
 * @Version: 1.0
 */
public interface VersionHintMapper extends BaseMapper<VersionHint, Integer> {
    //添加系统更新系统
    int save(VersionHint versionHint);

    //更新内容
    int updateContentById(@Param("content") String content, @Param("updateId") Integer updateId, @Param("id") Integer id);

    //更新状态
    int updateStateById(@Param("state") Byte state, @Param("updateId") Integer updateId, @Param("id") Integer id);

    //查询提示
    List<VersionHint> listHintByParam(Map<String, Object> param);

    //部门树
    List<Map<String, Object>> listDeptTree();

    //获取未关闭的所有提示列表
    List<Map<String, Object>> listAllVersionHint();

    //
    List<Map<String, Object>> listHistoryVersionHint(Map<String, Object> param);
}
