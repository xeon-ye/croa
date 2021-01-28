package com.qinfei.qferp.controller.inventory;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.inventory.Goods;
import com.qinfei.qferp.entity.inventory.ReceiveRepair;
import com.qinfei.qferp.service.inventory.IReceiveRepairService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * 物品报修控制器
 */
@RequestMapping("/repair")
@Controller
public class RepairController {
    @Autowired
    private IReceiveRepairService repairService;

    /**
     * 获取物品报修分页数据数量
     * @param map
     * @return
     */
    @RequestMapping("/getPageCount")
    @ResponseBody
    public ResponseData getPageCount(@RequestParam Map map){
            ResponseData data = ResponseData.ok();
            Integer total = repairService.getPageCount(map);
            data.putDataValue("total",total);
            return data;
    }

    /**
     * 获取物品报修分页数据
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<Map> listPg(@RequestParam Map map, Pageable pageable){
        try {
            return repairService.listPg(map,pageable);
        } catch (QinFeiException e) {
            return new PageInfo<>();
        }catch (Exception e){
            return new PageInfo<>();
        }
    }

    /**
     * 获取物品报修code
     * @return
     */
    @RequestMapping("/getRepairCode")
    @ResponseBody
    public ResponseData getRepairCode(){
        ResponseData data = ResponseData.ok();
        String code = repairService.getRepairCode();
        data.putDataValue("code",code);
        return data;
    }

    /**
     * 根据报修id查询
     * @param id
     * @return
     */
    @RequestMapping("/getById")
    @ResponseBody
    public ResponseData getById(@RequestParam("id")Integer id){
        ResponseData data = ResponseData.ok();
        ReceiveRepair repair = repairService.queryById(id);
        data.putDataValue("entity",repair);
        return data;
    }

    /**
     * 添加物品报修
     * @param receiveRepair
     * @return
     */
    @RequestMapping("/saveRepair")
    @ResponseBody
    public ResponseData saveRepair(ReceiveRepair receiveRepair){
        ResponseData data = ResponseData.ok();
        ReceiveRepair repair = repairService.addRepair(receiveRepair);
        data.putDataValue("entity",repair);
        data.putDataValue("message","操作成功");
        return data;
    }

    /**
     * 修改物品报修
     * @return
     */
    @RequestMapping("/editRepair")
    @ResponseBody
    public ResponseData editRepair(ReceiveRepair receiveRepair){
        ResponseData data = ResponseData.ok();
        repairService.editRepair(receiveRepair);
        data.putDataValue("message","操作成功");
        return data;
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @RequestMapping("/deleteRepair")
    @ResponseBody
    public ResponseData deleteRepair(@RequestParam("id")Integer id){
        ResponseData data = ResponseData.ok();
        try {
            data.putDataValue("message","操作成功");
            repairService.deleteRepair(id);
            return data;
        } catch (QinFeiException e) {
           return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，删除物品使用报修出错啦，请联系技术人员");
        }
    }
}
