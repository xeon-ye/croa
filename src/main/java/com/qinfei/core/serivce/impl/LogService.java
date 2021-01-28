package com.qinfei.core.serivce.impl;

import com.qinfei.core.entity.Log;
import com.qinfei.core.mapper.LogMapper;
import com.qinfei.core.serivce.ILogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 日志管理
 *
 * @author QinFei Gzw
 */
@Service
@Transactional(readOnly = true)
@CacheConfig
public class LogService implements ILogService {

    @Autowired
    LogMapper logMapper;

    @Transactional
    public boolean save(Log log) {
        logMapper.save(log);
        return true;
    }

    public PageInfo<Log> all(Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Log> list = logMapper.logs();
        return (PageInfo<Log>) new PageInfo(list);
    }


    public Log get(Integer id) {
        return logMapper.get(id);
    }

    public boolean del(Integer id) {
        logMapper.delete(id);
        return true;
    }

    public PageInfo<Log> search(Log log, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Log> list = logMapper.search(log,"id desc");
        return (PageInfo<Log>) new PageInfo(list);
    }



}
