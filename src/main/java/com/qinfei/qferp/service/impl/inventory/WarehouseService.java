package com.qinfei.qferp.service.impl.inventory;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.inventoryStock.Warehouse;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.inventory.WarehouseMapper;
import com.qinfei.qferp.service.inventory.IWarehouseService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WarehouseService implements IWarehouseService {
    @Autowired
    private WarehouseMapper warehouseMapper;


    @Override
    @Transactional
    public ResponseData addWarehouse(Warehouse warehouse){
        User user = AppUtil.getUser();
        warehouse.setCreateId(user.getId());
        warehouse.setCreateName(user.getName());
        warehouse.setCreateTime(new Date());
        warehouse.setState(1);
        warehouse.setCompanyCode(user.getCompanyCode());
        warehouseMapper.addWareHouse(warehouse);
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", "操作成功");
        data.putDataValue("entity", warehouse);
        return data;

    }

    @Override
    public PageInfo<Warehouse> warehouseList(Map map, Pageable pageable){
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        List<Warehouse> list = warehouseMapper.warehouseList(map);
        return (PageInfo<Warehouse>) new PageInfo(list);

    }

    @Override
    public List<Warehouse> getSameNameList(Map map) {
        User user = AppUtil.getUser();
        if(user==null){
            throw new QinFeiException(1002,"登录失效，请先登录");
        }
        map.put("companyCode",user.getCompanyCode());
        return warehouseMapper.getSameNameList(map);
    }

    @Override
    public Warehouse editAjax(Integer id) {
        if(id==null){
            throw new QinFeiException(1002,"仓库id不存在");
        }
        return warehouseMapper.editAjax(id);
    }

    @Override
    public String getWareNameById(Integer id) {
        if(id==null){
            throw new QinFeiException(1002,"仓库id不存在");
        }
        return warehouseMapper.getWareNameById(id);
    }

    @Override
    public Integer getCountByWareId(Integer id) {
        return warehouseMapper.getCountByWareId(id);
    }

    @Override
    public Integer getPageCount(Map map) {
        User user= AppUtil.getUser();
        if(user==null){
            throw new QinFeiException(1002,"登录失效，请先登录");
        }
        map.put("companyCode",user.getCompanyCode());
        return warehouseMapper.getPageCount(map);
    }

    @Override
    public PageInfo<Map> listPg(Map map,Pageable pageable) {
        User user= AppUtil.getUser();
        if(user==null){
            throw new QinFeiException(1002,"登录失效，请先登录");
        }
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        map.put("companyCode",user.getCompanyCode());
        List<Map> list=warehouseMapper.listPg(map);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional
    public void editWarehouse(Warehouse warehouse){
        try{
            warehouseMapper.editWarehouse(warehouse);
        }catch(Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，编辑仓库信息出错啦，请联系技术人员");
        }
    }
    @Override
    @Transactional
    public ResponseData delWarehouse(Integer id){
        try {
            ResponseData data = ResponseData.ok();
            if (id==null){
                throw new QinFeiException(1002, "没有获取到仓库id！");
            }
            User user= AppUtil.getUser();
            Warehouse warehouse = new Warehouse();
            warehouse.setId(id);
            warehouse.setUpdateTime(new Date());
            warehouse.setUpdateUserId(user.getId());
            warehouse.setState(IConst.STATE_DELETE);
            warehouseMapper.updateWarehouse(warehouse);
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，删除仓库信息出错啦，请联系技术人员");
        }
    }
}
