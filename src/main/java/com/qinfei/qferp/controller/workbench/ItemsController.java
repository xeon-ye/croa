package com.qinfei.qferp.controller.workbench;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.service.workbench.IItemsService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
@Slf4j
@Controller
@RequestMapping("/items")
class ItemsController {
    @Autowired
    IItemsService iItemsService;

    /**
     * 查询事项数据
     * @param params
     * @param pageable
     * @return
     */
    @ResponseBody
    @RequestMapping("/list")
//    @Log(opType = OperateType.QUERY, module = "系统管理|查询所有的代办事项", note = "查询所有的代办事项")
    public PageInfo<Map> list(@RequestParam Map params,@PageableDefault(size = 20) Pageable pageable){
        return iItemsService.list(params,pageable);
    }

    @ResponseBody
    @RequestMapping("/batchFinishItems")
//    @Log(opType = OperateType.QUERY, module = "系统管理|查询所有的代办事项", note = "查询所有的代办事项")
    public ResponseData batchFinishItems(@RequestParam("ids") String ids){
        try{
            ResponseData data = ResponseData.ok() ;
            iItemsService.batchFinishItems(ids) ;
            data.putDataValue("message","操作成功");
            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 根据itemId查询待办状态
     * @param itemId
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryItemStateById")
    public int queryItemStateById(@RequestParam("itemId") Integer itemId) {
        return iItemsService.queryItemStateById(itemId);
    }
    /**
     * 完成待办
     * @param itemId
     * @return
     */
    @ResponseBody
    @RequestMapping("/finishItem")
    public ResponseData finishItem(@RequestParam("itemId") Integer itemId){
        try{
            ResponseData data = ResponseData.ok();
            Items items = new Items();
            items.setId(itemId);
            iItemsService.finishItems(items);
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }
}
