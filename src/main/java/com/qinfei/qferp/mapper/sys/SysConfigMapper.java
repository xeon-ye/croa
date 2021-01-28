package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.sys.SysConfig;
import com.qinfei.qferp.entity.sys.VersionHint;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: SysConfigMapper
 * @Description: 系统配置参数
 * @Author: Xuxiong
 * @Date: 2020/03/13 0020 15:54
 * @Version: 1.0
 */
public interface SysConfigMapper extends BaseMapper<SysConfig, Integer> {
    //添加系统参数配置
    int save(SysConfig sysConfig);

    //更新内容
    int updateById(SysConfig sysConfig);

    //更新状态
    int updateStateById(@Param("state") Byte state, @Param("updateId") Integer updateId, @Param("id") Integer id);

    //根据Key获取有效数据
    SysConfig getOneConfigByKey(@Param("configKey") String configKey, @Param("id") Integer id);

    //跟据id 更改数据
    int updateConfigValue(@Param("id") Integer id , @Param("updateId") Integer updateId, @Param("newUser") String newUser);

    //查询提示
    List<SysConfig> listConfigByParam(Map<String, Object> param);

    //获取未关闭的所有提示列表
    List<SysConfig> listAllSysConfig();

    //获取系统所有用户
    List<Map<String, Object>> listAllUser();

    //获取系统所有部门
    List<Map<String, Object>> listAllDept();

    //获取所有角色
    List<Map<String, Object>> listAllRole();

    //获取媒体板块
    List<Map<String, Object>> listAllMediaPlate();
}
