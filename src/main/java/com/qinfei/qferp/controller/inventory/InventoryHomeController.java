package com.qinfei.qferp.controller.inventory;


import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.inventory.IInventoryHomeService;
import com.qinfei.qferp.utils.AppUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 进销存首页控制器
 * @author tsf
 */
@Slf4j
@Controller
@RequestMapping("/inventoryHome")
public class InventoryHomeController {
    @Autowired
    private IInventoryHomeService inventoryHomeService;

    /**
     * 物品采购订单统计
     * @param map
     * @return
     */
    @RequestMapping("/purchaseOrderStatistics")
    @ResponseBody
    public ResponseData purchaseOrderStatistics(@RequestParam Map map){
        try {
            ResponseData data=ResponseData.ok();
            if(hasInventoryAuthority()){
                Map entity = inventoryHomeService.purchaseOrderStatistics(map);
                data.putDataValue("entity",entity);
                return data;
            }else{
               return null;
            }
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e){
            return ResponseData.customerError(1002,"抱歉，物品采购订单统计出错啦，请联系技术人员");
        }
    }

    /**
     * 商品库存分析数据
     * @param map
     * @return
     */
    @RequestMapping("/stockAnalysis")
    @ResponseBody
    public ResponseData stockAnalysis(@RequestParam Map map){
        try {
            ResponseData data=ResponseData.ok();
            if(hasInventoryAuthority()){
                Map entity = inventoryHomeService.stockAnalysis(map);
                data.putDataValue("entity",entity);
                return data;
            }else{
                return null;
            }
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e){
            return ResponseData.customerError(1002,"抱歉，商品库存分析数据出错啦，请联系技术人员");
        }
    }

    /**
     * 出入库统计
     * @param map
     * @return
     */
    @RequestMapping("/outBoundStatistics")
    @ResponseBody
    public ResponseData outBoundStatistics(@RequestParam Map map){
        try {
            ResponseData data=ResponseData.ok();
            if(hasInventoryAuthority()){
                Map entity = inventoryHomeService.outBoundStatistics(map);
                data.putDataValue("entity",entity);
                return data;
            }else{
                return null;
            }
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e){
            return ResponseData.customerError(1002,"抱歉，出入库统计出错啦，请联系技术人员");
        }
    }

    /**
     * 是否有查看进销存首页的权限
     * @return
     */
    private Boolean hasInventoryAuthority(){
        User user = AppUtil.getUser();
        List<Role> roles=user.getRoles();
        Boolean flag=false;
        if(CollectionUtils.isNotEmpty(roles)){
            for(Role role:roles){
                if("CK".equals(role.getType()) && "ZY".equals(role.getCode())){
                    flag=true;
                }
            }
        }
        return flag;
    }
}
