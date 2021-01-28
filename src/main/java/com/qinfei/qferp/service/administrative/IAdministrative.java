package com.qinfei.qferp.service.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.AdministrativeLeave;
import com.github.pagehelper.PageInfo;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IAdministrative {
    //获取行政信息
    PageInfo<Administrative> administrativeList(Map<String, Object> params, Pageable pageable);

    PageInfo<Administrative> administrativeList1(Map<String, Object> params, Pageable pageable,Integer type);



    List<Administrative> exportList(Map<String, Object> params, OutputStream outputStream);

    List<Map> exportContent(Map map,OutputStream outputStream);

    Administrative getById(Integer id);

}
