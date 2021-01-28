package com.qinfei.qferp.controller.inventory;
import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.inventoryStock.Warehouse;
import com.qinfei.qferp.service.inventory.IWarehouseService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 *  进销存 --仓库
 */
@Slf4j
@Controller
@RequestMapping("/warehouse")
public class WarehouseController {
    @Autowired
    private IWarehouseService warehouseService;

    /**
     * 仓库新增
     */
    @RequestMapping("/addWarehouse")
    @ResponseBody
    public ResponseData addWarehouse (@RequestBody Warehouse warehouse){
        return warehouseService.addWarehouse(warehouse);
    }

    /**
     * 仓库列表
     */
    @RequestMapping("/warehouseList")
    @ResponseBody
    public PageInfo<Warehouse> warehouseList(@RequestParam Map<String,Object>map, @PageableDefault() Pageable pageable){
        return warehouseService.warehouseList(map,pageable);

    }

    /**
     * 判断仓库名称是否相同
     * @param map
     * @return
     */
    @RequestMapping("/getSameNameList")
    @ResponseBody
    public Integer getSameNameList(@RequestParam Map<String,Object>map){
        List<Warehouse> list = warehouseService.getSameNameList(map);
        return list.size();
    }

    /**
     * 根据id查询仓库信息
     * @param id
     * @return
     */
    @RequestMapping("/editAjax")
    @ResponseBody
    public ResponseData editAjax(@RequestParam("id")Integer id){
        try {
            Warehouse entity=warehouseService.editAjax(id);
            ResponseData data=ResponseData.ok();
            data.putDataValue("entity",entity);
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            return ResponseData.customerError(1002,"抱歉，根据id查询仓库信息出错啦，请联系技术人员");
        }
    }

    /**
     * 查询仓库是否被使用
     * @param id
     * @return
     */
    @RequestMapping("/getCountByWareId")
    @ResponseBody
    public ResponseData getCountByWareId(@RequestParam("id")Integer id){
        Integer total = warehouseService.getCountByWareId(id);
        ResponseData data = ResponseData.ok();
        data.putDataValue("total",total);
        return data;
    }

    /**
     * 获取仓库分页数量
     * @param map
     * @return
     */
    @RequestMapping("/getPageCount")
    @ResponseBody
    public ResponseData getPageCount(@RequestParam Map<String,Object>map){
        ResponseData data = ResponseData.ok();
        Integer total = warehouseService.getPageCount(map);
        data.putDataValue("total",total);
        return data;
    }

    /**
     * 获取仓库分页数据
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<Map> listPg(@RequestParam Map<String,Object>map, @PageableDefault() Pageable pageable){
        return warehouseService.listPg(map,pageable);
    }

    /**
     * 仓库编辑
     * @param warehouse
     * @return
     */
    @RequestMapping("/editWarehouse")
    @ResponseBody
    public ResponseData editWarehouse (Warehouse warehouse){
        try {
            ResponseData data=ResponseData.ok();
            warehouseService.editWarehouse(warehouse);
            data.putDataValue("message","操作成功");
            return data;
        } catch (Exception e) {
            throw new QinFeiException(1002,"抱歉，编辑仓库信息出错啦，请联系技术人员");
        }
    }

    /**
     * 删除仓库信息
     * @param id
     * @return
     */
    @RequestMapping("/delWarehouse")
    @ResponseBody
    public ResponseData delWarehouse(@RequestParam("id") Integer id){
        return warehouseService.delWarehouse(id);
    }
}
