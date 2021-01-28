package com.qinfei.qferp.service.impl.media1;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.media.MediaForm;
import com.qinfei.qferp.entity.media1.MediaExtendFieldJson;
import com.qinfei.qferp.entity.media1.MediaForm1;
import com.qinfei.qferp.mapper.media1.MediaForm1Mapper;
import com.qinfei.qferp.service.media1.IMediaForm1Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaForm1ServiceImpl
 * @Description 媒体扩展表单接口
 * @Author xuxiong
 * @Date 2019/6/26 0026 10:14
 * @Version 1.0
 */
@Service
public class MediaForm1ServiceImpl implements IMediaForm1Service {
    @Autowired
    private MediaForm1Mapper mediaForm1Mapper;

    @Transactional
    @Override
    public void deleteBatch(List<Integer> ids) {
        mediaForm1Mapper.deleteBatch(ids);
    }

    @Transactional
    @Override
    public void save(MediaForm1 mediaForm) {
        if(mediaForm1Mapper.getMediaFormCount(mediaForm) > 0){
            throw new QinFeiException(1002, "当前媒体板块已存在相同的列名");
        }
        mediaForm1Mapper.insert(mediaForm);
    }

    @Transactional
    @Override
    public void update(MediaForm1 mediaForm) {
        if(mediaForm1Mapper.getMediaFormCount(mediaForm) > 0){
            throw new QinFeiException(1002, "当前媒体板块已存在相同的列名");
        }
        mediaForm1Mapper.updateById(mediaForm);
    }

    @Override
    public List<MediaForm1> findAllByMediaPlateId(Integer mediaPlateId) {
        List<MediaForm1> mediaForm1List = listMediaFormByPlateId(mediaPlateId);
        if(CollectionUtils.isNotEmpty(mediaForm1List)){
            for(MediaForm1 mediaForm1 : mediaForm1List){
                if(mediaForm1.getDataType() != null && "sql".equals(mediaForm1.getDataType()) && StringUtils.isNotEmpty(mediaForm1.getDbSql())){
                    mediaForm1.setDatas(mediaForm1Mapper.dictSQL(mediaForm1.getDbSql()));
                }
            }
        }
        return mediaForm1List;
    }

    @Override
    public PageInfo<Map> list(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> mapList = mediaForm1Mapper.listByMediaPlateId(map);
        return new PageInfo<>(mapList);
    }

    @Override
    public List<MediaForm1> listPriceTypeByPlateId(Integer mediaPlateId) {
        return mediaForm1Mapper.listPriceTypeByPlateId(mediaPlateId);
    }

    @Override
    public List<MediaForm1> listAllPriceType() {
        return mediaForm1Mapper.listAllPriceType();
    }

    @Override
    public List<MediaForm1> listMediaFormByPlateId(Integer mediaPlateId) {
        return mediaForm1Mapper.listMediaFormByPlateId(mediaPlateId);
    }

    @Override
    public Map<Integer, Map<String, MediaForm1>> getAllMediaForm() {
        List<MediaForm1> mediaForm1List = mediaForm1Mapper.listAllMediaForm();
        Map<Integer, Map<String, MediaForm1>> result = new HashMap<>();
        if(CollectionUtils.isNotEmpty(mediaForm1List)){
            for(MediaForm1 mediaForm1 : mediaForm1List){
                if(result.get(mediaForm1.getMediaPlateId()) == null){
                    Map<String, MediaForm1> mediaForm1Map = new HashMap<>();
                    result.put(mediaForm1.getMediaPlateId(), mediaForm1Map);
                }
                if(StringUtils.isNotEmpty(mediaForm1.getDataType()) && "sql".equals(mediaForm1.getDataType()) && StringUtils.isNotEmpty(mediaForm1.getDbSql())){
                    mediaForm1.setCellValueMap(sql2Map(mediaForm1.getDbSql()));
                }else if(StringUtils.isNotEmpty(mediaForm1.getDataType()) && "json".equals(mediaForm1.getDataType()) && StringUtils.isNotEmpty(mediaForm1.getDbJson())){
                    mediaForm1.setCellValueMap(json2Map(mediaForm1.getDbJson()));
                }
                result.get(mediaForm1.getMediaPlateId()).put(mediaForm1.getCellCode(), mediaForm1);
            }
        }
        return result;
    }

    @Override
    public Map<Integer, Map<String, String>> getFormRelete() {
        List<MediaForm> mediaFormList = mediaForm1Mapper.listAllOldMediaForm();
        Map<Integer, Map<String, String>> result = new HashMap<>();
        if(CollectionUtils.isNotEmpty(mediaFormList)){
            for(MediaForm mediaForm : mediaFormList){
                if(result.get(mediaForm.getMediaTypeId()) == null){
                    Map<String, String> mediaFormMap = new HashMap<>();
                    result.put(mediaForm.getMediaTypeId(), mediaFormMap);
                }
                result.get(mediaForm.getMediaTypeId()).put(mediaForm.getName().trim(),mediaForm.getCode().trim());
            }
        }
        return result;
    }

    /**
     * t_media_form1表中JSON字符串转MAP
     * @param json
     * @return
     */
    private Map<String,String> json2Map(String json){
        Map<String,String> temp = null;
        List<MediaExtendFieldJson> mediaExtendFieldJsonList = JSON.parseArray(json,MediaExtendFieldJson.class);
        if(CollectionUtils.isNotEmpty(mediaExtendFieldJsonList)){
            temp = new HashMap<>();
            for(MediaExtendFieldJson mediaExtendFieldJson : mediaExtendFieldJsonList){
                temp.put(mediaExtendFieldJson.getValue(), mediaExtendFieldJson.getText());
            }
        }
        return temp;
    }

    /**
     * t_media_form1表中sql字符串转MAP
     * @param sql
     * @return
     */
    private Map<String,String> sql2Map(String sql){
        Map<String,String> temp = null;
        List<Map<String, Object>> list =  mediaForm1Mapper.dictSQL(sql);
        if(CollectionUtils.isNotEmpty(list)){
            temp = new HashMap<>();
            for(Map map1 : list){
                temp.put(String.valueOf(map1.get("id")), String.valueOf(map1.get("name")));
            }
        }
        return temp;
    }
}
