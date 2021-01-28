package com.qinfei.qferp.service.impl.media1;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.media1.MediaBrand;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.media1.MediaBrandMapper;
import com.qinfei.qferp.service.media1.IMediaBrandService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 媒体品牌服务实现类
 *
 * @author tsf
 * @since 2020-10-21 09:30:21
 */
@Service
public class MediaBrandService implements IMediaBrandService {
    @Autowired
    private MediaBrandMapper mediaBrandMapper;

    /**
     * 新增数据
     * @param mediaBrand 实例对象
     * @return 实例对象
     */
    @Transactional
    @Override
    public void save(MediaBrand mediaBrand) {
        try{
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002, "请先登录！");
            }
            this.mediaBrandMapper.saveMediaBrand(mediaBrand);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "新增媒体品牌表数据异常！");
        }   
    }

    /**
     * 修改数据
     * @param mediaBrand 实例对象
     * @return 实例对象
     */
    @Transactional
    @Override
    public void update(MediaBrand mediaBrand) {
       try{
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002, "请先登录！");
            }
            this.mediaBrandMapper.update(mediaBrand);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "编辑媒体品牌表数据异常！");
        }  
    }

    @Override
    public PageInfo<Map> listPg(Map map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        List<Map> list = mediaBrandMapper.listPg(map);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Map> listPgForView(Map map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        List<Map> list = mediaBrandMapper.listPgForView(map);
        return new PageInfo<>(list);
    }

    @Override
    public MediaBrand getById(String id) {
        return mediaBrandMapper.getById(id);
    }
}