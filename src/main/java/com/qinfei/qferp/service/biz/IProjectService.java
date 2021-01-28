package com.qinfei.qferp.service.biz;

import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.biz.Project;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface IProjectService {

	PageInfo<Map> listPg(int pageNum, int pageSize, Map map);

    List<Map> initNodeConfig(Map map);

    Project add(Project entity, Map map);

    @Transient
    Project edit(Project entity, Map map);

    Map view(Integer id);

    Project getById(Integer id);

    @Transactional
    void enableOrDisable(Project project, Integer flag);

    @Transactional
    void del(Integer id);

    Project update(Project project);

    PageInfo<Map> queryArticlesByProjectId(int pageNum, int pageSize, Map map);

    Map querySumByProjectId(Map map);

    //抄送确认
    void confirm(Integer itemId);
}
