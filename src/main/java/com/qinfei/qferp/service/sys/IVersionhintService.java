package com.qinfei.qferp.service.sys;

import com.qinfei.qferp.entity.sys.VersionHint;
import com.qinfei.qferp.entity.sys.VersionHintRelate;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: IVersionhintService
 * @Description: 系统版本提示表接口
 * @Author: Xuxiong
 * @Date: 2020/1/20 0020 17:01
 * @Version: 1.0
 */
public interface IVersionhintService {
    String CACHE_KEY = "VersionHint";

    //新增
    @CacheEvict(value = CACHE_KEY,  allEntries = true)
    void save(VersionHint versionHint);

    // 提示通知
    @CacheEvict(value = CACHE_KEY,  allEntries = true)
    void notice(int id);

    //删除提示
    @CacheEvict(value = CACHE_KEY,  allEntries = true)
    void del(int id);

    //分页查询列表
    PageInfo<VersionHint> list(Map<String, Object> map, Pageable pageable);

    //更新阅读，直接将当前用户所有的版本提示更新
    @CacheEvict(value = CACHE_KEY,  allEntries = true)
    void updateReadFlag();

    //查询所有关系
    @Cacheable(value = CACHE_KEY,  key = "'new'")
    Map<String, List<Map<String, Object>>> listAllVersionHint();

    //查询历史版本提示
    List<Map<String, Object>> historyVersionHint(Map<String, Object> param);

    //部门树
    List<Map<String,Object>> listDeptTree();
}
