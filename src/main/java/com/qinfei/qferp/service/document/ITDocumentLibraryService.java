package com.qinfei.qferp.service.document;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.document.TDocumentLibrary;
import com.qinfei.qferp.entity.document.TDocumentType;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ITDocumentLibraryService {

    JSONArray libraryType();

    PageInfo<TDocumentLibrary> selectLibraryList(Map<String,Object> map, Pageable pageable);

    /**
     * 建议制度跳转所需接口
     * @param map
     * @return
     */
    List<TDocumentLibrary> getDocumentLibraryList(Map map);

    Map<String,Object> list(Map<String,Object> map);

    int getLibraryTotal(Map<String,Object> map);

    ResponseData releaseUser();

    ResponseData addLibrary(TDocumentLibrary tDocumentLibrary,MultipartFile[] files);

    List<Map<String,Object>>listpermissions(String signStr,String name);

    ResponseData selectLibrary(Integer id);

    ResponseData addType(Map<String,Object> map);

    ResponseData editTypeName(Map<String,Object> map);

    ResponseData delType(String typeId);

    ResponseData selectTypeFlag(Map<String ,Object> map);

    ResponseData delLibrary(Integer id);

    TDocumentLibrary selectLibraryview(Integer id);

    void updateLibraryReady(Integer id);

    ResponseData updatefailure(Integer id);

    ResponseData deleteFile(String file , String fileLink , Integer id);

    ResponseData CheckList(Integer id);



}
