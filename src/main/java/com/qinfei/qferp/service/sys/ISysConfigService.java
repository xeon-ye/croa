package com.qinfei.qferp.service.sys;

import com.qinfei.qferp.entity.sys.SysConfig;
import com.qinfei.qferp.entity.sys.VersionHint;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: ISysConfigService
 * @Description: 系统配置参数接口
 * @Author: Xuxiong
 * @Date: 2020/1/20 0020 17:01
 * @Version: 1.0
 */
public interface ISysConfigService {
    String CACHE_KEY = "SysConfig";

    //新增
    @CacheEvict(value = CACHE_KEY,  allEntries = true)
    SysConfig save(SysConfig sysConfig);

    //新增
    @CacheEvict(value = CACHE_KEY,  allEntries = true)
    void update(SysConfig sysConfig);

    //修改状态
    @CacheEvict(value = CACHE_KEY,  allEntries = true)
    void updateStateById(int id, byte state);

    //分页查询列表
    PageInfo<SysConfig> list(Map<String, Object> map, Pageable pageable);

    //更新阅读，直接将当前用户所有的版本提示更新
    @Cacheable(value = CACHE_KEY,  key = "'new'")
    Map<String, Map<String, Object>> getAllConfig();

    //根据配置类型获取配置值可选列表
    List<Map<String, Object>> listTableData(String configType);

    //根据配置项key获取配置
    @Cacheable(value = CACHE_KEY, key = "'configKey=' + #configKey")
    SysConfig getOneConfigByKey(String configKey);
}
