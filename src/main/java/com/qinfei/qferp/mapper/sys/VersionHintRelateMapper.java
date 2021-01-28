package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.sys.VersionHint;
import com.qinfei.qferp.entity.sys.VersionHintRelate;
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
public interface VersionHintRelateMapper extends BaseMapper<VersionHintRelate, Integer> {
    //添加系统更新系统
    int save(VersionHintRelate versionHintRelate);

    //批量添加
    int saveBatch(List<VersionHintRelate> versionHintRelateList);

    //根据提示Id删除所有记录
    int updateStateByHintId(@Param("state") Byte state, @Param("hintId") Integer hintId);

    //修改阅读情况
    int updateReadFlagById(@Param("readFlag") Byte readFlag, @Param("userId") Integer userId);

    //修改状态
    int updateStateById(@Param("state") Byte state,@Param("id") Integer id);

    //批量修改状态
    int batchUpdateStateByIds(@Param("state") Byte state,  @Param("ids") List<Integer> ids);

    //根据hintId获取列表
    List<VersionHintRelate> listRelateByHintId(@Param("hintId") Integer hintId);
}
