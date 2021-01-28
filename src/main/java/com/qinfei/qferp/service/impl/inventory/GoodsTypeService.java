package com.qinfei.qferp.service.impl.inventory;
import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.inventory.Goods;
import com.qinfei.qferp.entity.inventory.GoodsType;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.inventory.GoodsMapper;
import com.qinfei.qferp.mapper.inventory.GoodsTypeMapper;
import com.qinfei.qferp.service.inventory.IGoodsTypeService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.flowable.common.engine.impl.el.function.VariableContainsAnyExpressionFunction;
import org.flowable.spring.boot.app.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoodsTypeService implements IGoodsTypeService {
    @Autowired
    private GoodsTypeMapper goodsTypeMapper;
    @Autowired
    private GoodsMapper goodsMapper;

    @Transactional
    @Override
    @Caching(evict = {@CacheEvict(value = "loadGoodsTypeList", key = "'companyCode='+#goodsType.companyCode")})
    public GoodsType saveGoodsType(GoodsType goodsType) {
        User user = AppUtil.getUser();
        String name="";
        try {
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            if(goodsType.getParentId()==0){
                name="产品分类";
            }else{
                name="产品";
            }
            if(StringUtils.isEmpty(goodsType.getName())){
                throw new QinFeiException(1002,name+"名称不能为空");
            }
            Map map = new HashMap();
            map.put("id",null);
            map.put("name",goodsType.getName());
            map.put("companyCode",goodsType.getCompanyCode());
            List<GoodsType> list = goodsTypeMapper.getSameNameList(map);
            if(CollectionUtils.isNotEmpty(list)){
                    throw new QinFeiException(1002,"存在相同的"+name);
            }
            Date currentDate = new Date();
            goodsType.setName(goodsType.getName().trim());
            goodsType.setState(0);
            goodsType.setUpdateUserId(user.getId());
            goodsType.setUpdateTime(currentDate);
            goodsTypeMapper.saveGoodsType(goodsType);
            return goodsType;
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，新增"+name+"出错啦,请联系技术人员");
        }
    }

    @Transactional
    @Override
    @Caching(evict = {@CacheEvict(value = "loadGoodsTypeList", key = "'companyCode='+#goodsType.companyCode")})
    public GoodsType updateGoodsType(GoodsType goodsType) {
        User user = AppUtil.getUser();
        String name="";
        try {
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            if(goodsType.getParentId()==0){
                name="产品分类";
            }else{
                name="产品";
            }
            if(StringUtils.isEmpty(goodsType.getName())){
                throw new QinFeiException(1002,name+"名称不能为空");
            }
            Map map = new HashMap();
            map.put("id",goodsType.getId());
            map.put("name",goodsType.getName());
            map.put("companyCode",goodsType.getCompanyCode());
            List<GoodsType> list = goodsTypeMapper.getSameNameList(map);
            if(CollectionUtils.isNotEmpty(list)){
                throw new QinFeiException(1002,"存在相同的"+name);
            }
            goodsType.setName(goodsType.getName().trim());
            goodsType.setUpdateUserId(user.getId());
            goodsType.setUpdateTime(new Date());
            goodsTypeMapper.updateGoodsType(goodsType);
            return goodsType;
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，编辑"+name+"出错啦，请联系技术人员");
        }
    }

    @Override
    public GoodsType getById(Integer id) {
        try {
            return goodsTypeMapper.getById(id);
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，根据id查询产品分类信息出错啦");
        }
    }

    @Override
    public Map getStockDataById(Map map) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            map.put("companyCode",user.getCompanyCode());
            return goodsTypeMapper.getStockDataById(map);
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉,根据产品查询库存数量出错啦,请联系技术人员");
        }
    }

    @Override
    public List<GoodsType> getGoodsTypeByCondition(Integer id, String name, String companyCode) {
        Map map = new HashMap();
        map.put("id",id);
        map.put("name",name);
        map.put("companyCode",companyCode);
        return goodsTypeMapper.getSameNameList(map);
    }

    @Override
    public List<GoodsType> checkGoodsTypeData(Integer id) {
        Map map=new HashMap();
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        map.put("parentId",id);
        return goodsTypeMapper.checkGoodsTypeData(map);
    }

    @Override
    public void editStockAmount(Map map) {
        try {
            goodsTypeMapper.editStockAmount(map);
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，修改库存最值出错啦");
        }
    }

    @Transactional
    @Override
    @Caching(evict = {@CacheEvict(value = "loadGoodsTypeList", allEntries = true)})
    public void del(Integer id) {
        User user = AppUtil.getUser();
        try {
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            if(goodsTypeMapper.getById(id) == null){
                throw new QinFeiException(1002, "产品分类不存在！");
            }
            goodsTypeMapper.del(id, IConst.STATE_DELETE);
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，删除产品分类出错啦");
        }
    }

    /**
     * 查询产品分页数量
     * @param map
     * @return
     */
    @Override
    public Integer getPageCount(Map map) {
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        return goodsTypeMapper.getPageCount(map);
    }

    @Override
    public PageInfo<GoodsType> getGoodsTypeInfo(Map map, Pageable pageable) {
        User user = AppUtil.getUser();
        try {
            if(user==null){
                throw new QinFeiException(1002, "请先登录！");
            }
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            map.put("companyCode",user.getDept().getCompanyCode());
            List<GoodsType> list = goodsTypeMapper.getGoodsTypeInfo(map);
            if(list.size()<0){
                return new PageInfo<>();
            }
            PageInfo<GoodsType> pageInfo =new PageInfo<>(list);
            return pageInfo;
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，查询产品分类列表数据出错啦");
        }
    }

    @Override
    public Integer getGoodsPageCount(Map map) {
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        return goodsTypeMapper.getGoodsPageCount(map);
    }

    @Override
    public PageInfo<GoodsType> getGoodsInfo(Map map, Pageable pageable) {
        User user = AppUtil.getUser();
        try {
            if(user==null){
                throw new QinFeiException(1002, "请先登录！");
            }
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            map.put("companyCode",user.getDept().getCompanyCode());
            List<GoodsType> list = goodsTypeMapper.getGoodsInfo(map);
            if(list.size()<0){
                return new PageInfo<>();
            }
            PageInfo<GoodsType> pageInfo =new PageInfo<>(list);
            return pageInfo;
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，查询产品列表数据出错啦");
        }
    }

    @Override
    @Cacheable(value = "loadGoodsTypeList",key = "'companyCode='+#companyCode")
    public List<Map> loadGoodsTypeInfo(String companyCode) {
        User user = AppUtil.getUser();
        try {
            if(user==null){
                throw new QinFeiException(1002, "请先登录！");
            }
            return goodsTypeMapper.loadGoodsTypeInfo(companyCode);
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002,"抱歉,加载产品分类数据出错啦");
        }
    }

    @Override
    public List<Map> loadGoodsTypeByParentId(Integer parentId, String companyCode) {
        try {
            return goodsTypeMapper.loadGoodsTypeByParentId(parentId,companyCode);
        } catch (Exception e) {
            throw new QinFeiException(1002,"抱歉,加载产品分类数据出错啦");
        }
    }

    @Override
    public PageInfo<Map> getStockMaxWarnData(Map map) {
        try {
            User user=AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            map.put("companyCode",user.getCompanyCode());
            List<Integer> list=goodsTypeMapper.getStockIds(map);
            map.put("list",list);
            if(CollectionUtils.isNotEmpty(list)){
                List<Map> stockMaxWarnData = goodsTypeMapper.getStockMaxWarnData(map);
                return new PageInfo<>(stockMaxWarnData);
            }else {
                return new PageInfo<>();
            }
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，库存最大值预警出错啦，请联系技术人员");
        }
    }

    @Override
    public PageInfo<Map> getStockMinWarnData(Map map) {
        try {
            User user=AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            map.put("companyCode",user.getCompanyCode());
            List<Integer> list=goodsTypeMapper.getStockIds(map);
            map.put("list",list);
            if(CollectionUtils.isNotEmpty(list)){
                List<Map> stockMinWarnData = goodsTypeMapper.getStockMinWarnData(map);
                return new PageInfo<>(stockMinWarnData);
            }else {
                return new PageInfo<>();
            }
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，库存最小值预警出错啦，请联系技术人员");
        }
    }
}
