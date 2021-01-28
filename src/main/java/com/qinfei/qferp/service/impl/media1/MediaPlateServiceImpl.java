package com.qinfei.qferp.service.impl.media1;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.media1.MediaPlate;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.media1.MediaPlateMapper;
import com.qinfei.qferp.service.media1.IMediaPlateService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaPlateServiceImpl
 * @Description 媒体板块接口
 * @Author xuxiong
 * @Date 2019/6/26 0026 11:23
 * @Version 1.0
 */
@Service
@Slf4j
public class MediaPlateServiceImpl implements IMediaPlateService {
    @Autowired
    private MediaPlateMapper mediaPlateMapper;

    @Override
    public void save(MediaPlate mediaPlate) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            validMediaPlate(mediaPlate, true);
            mediaPlateMapper.save(mediaPlate);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "添加媒体板块异常，请联系技术人员！");
        }
    }

    @Override
    public void update(MediaPlate mediaPlate) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            validMediaPlate(mediaPlate, false);
            mediaPlate.setPlateTypeId(0);
            mediaPlateMapper.updateById(mediaPlate);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "修改媒体板块异常，请联系技术人员！");
        }
    }

    @Override
    public void updateState(int id, int state) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            mediaPlateMapper.updateState(state, id);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "修改媒体板块状态异常，请联系技术人员！");
        }
    }

    @Override
    public PageInfo<MediaPlate> listPlate(Map<String, Object> param, Pageable pageable) {
        PageInfo<MediaPlate> pageInfo = null;
        try{
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            pageInfo = new PageInfo<>(mediaPlateMapper.listPlateByParam(param));
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public ResponseData mediaAllPlateList() {
       ResponseData data = ResponseData.ok();
        Map<String, Object> param = new HashMap<>();
       List<MediaPlate> list = mediaPlateMapper.listPlateByParam(param);
       data.putDataValue("list",list);
       return  data;
    }

    @Override
    public List<MediaPlate> listByPlateTypeId(Integer plateTypeId) {
        return mediaPlateMapper.listByPlateTypeId(plateTypeId);
    }

    @Override
    public List<MediaPlate> listMediaPlateByUserId(Integer userId) {
        return mediaPlateMapper.listMediaPlateByUserId(userId);
    }

    @Override
    public MediaPlate getByMediaId(Integer mediaId) {
        return mediaPlateMapper.getByMediaId(mediaId);
    }

    @Override
    public List<MediaPlate> queryMediaPlate() {
        return mediaPlateMapper.queryMediaPlate();
    }

    //校验媒体板块信息
    private void validMediaPlate(MediaPlate mediaPlate, boolean saveFlag){
        if(saveFlag){
            mediaPlate.setVersions(1);
        }else {
            if(mediaPlate.getId() == null){
                throw new QinFeiException(1002, "媒体板块ID不能为空！");
            }
        }
        if(StringUtils.isEmpty(mediaPlate.getName())){
            throw new QinFeiException(1002, "媒体板块名称不能为空！");
        }
        if(mediaPlate.getParentType() == null){
            throw new QinFeiException(1002, "父级板块类型不能为空！");
        }
        if(mediaPlate.getStandarPlatformFlag() == null){
            throw new QinFeiException(1002, "是否标准平台不能为空！");
        }
        if(mediaPlate.getPercent() == null){
            throw new QinFeiException(1002, "提成百分比不能为空！");
        }
        if(mediaPlate.getIsStation() == null){
            throw new QinFeiException(1002, "是否站内不能为空！");
        }
        //校验名称是否存在
        if(mediaPlateMapper.checkMediaPlate(mediaPlate.getName(), mediaPlate.getId()) > 0){
            throw new QinFeiException(1002, "媒体板块名称已存在！");
        }
    }

}
