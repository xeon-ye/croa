package com.qinfei.qferp.service.announcementinform;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.announcementinform.MediaPass;
import com.qinfei.qferp.entity.sys.Dept;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface IMediaPassService {


    PageInfo<MediaPass> selectByPrimaryKey(Map map, Pageable pageable);

    MediaPass add(MediaPass mediaPass,Integer[] deptIds);

    void delById(MediaPass entity);

    MediaPass getById(Integer id);

    MediaPass edit(MediaPass entity,Integer[] deptIds);

    void editoperationDept(List<Integer> list);

    List<Dept> queryDeptByAccountId(Integer id);

    void insertoperationDept (List<Map> file);

    @Transactional
    List<Dept> delDeptAccountDept(Integer operationDeptId, Integer deptId);

    void  announcementConfirming( MediaPass mediaPass);

//    List<Dept> insertAccountDept(Integer operationDeptId,Integer deptId);

    Map<String, Object> getResourcePermission(HttpServletRequest request);

}
