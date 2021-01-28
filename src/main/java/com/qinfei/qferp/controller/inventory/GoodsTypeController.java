package com.qinfei.qferp.controller.inventory;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.inventory.GoodsType;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.inventory.IGoodsTypeService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.util.Map;

/**
 * 产品分类控制器
 * @author tsf
 */
@Slf4j
@Controller
@RequestMapping("/goodsType")
public class GoodsTypeController {
    @Autowired
    private IGoodsTypeService goodsTypeService;

    /**
     * 产品分类列表数量
     * @param map
     * @return
     */
    @RequestMapping("/getPageCount")
    @ResponseBody
    public ResponseData getPageCount(@RequestParam Map map){
            ResponseData data=ResponseData.ok();
            Integer total = goodsTypeService.getPageCount(map);
            data.putDataValue("total",total);
            return data;
    }

    /**
     * 产品分类列表
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<GoodsType> listPg(@RequestParam Map map, Pageable pageable){
        try {
            return goodsTypeService.getGoodsTypeInfo(map,pageable);
        } catch (QinFeiException e) {
            e.printStackTrace();
            return new PageInfo<>();
        }catch (Exception e){
            return new PageInfo<>();
        }
    }

    /**
     * 修改库存最大值，最小值
     * @param map
     */
    @RequestMapping("/editStockAmount")
    @ResponseBody
    public ResponseData editStockAmount(@RequestParam Map map){
        try {
            ResponseData data=ResponseData.ok();
            data.putDataValue("message","操作成功");
            goodsTypeService.editStockAmount(map);
            return data;
        } catch (Exception e) {
            return ResponseData.customerError(1002,"抱歉，修改库存最值出错啦");
        }
    }

    /**
     * 产品分页列表
     * @param map
     * @return
     */
    @RequestMapping("/getGoodsPageCount")
    @ResponseBody
    public ResponseData getGoodsPageCount(@RequestParam Map map){
        ResponseData data = ResponseData.ok();
        Integer total = goodsTypeService.getGoodsPageCount(map);
        data.putDataValue("total",total);
        return data;
    }

    /**
     * 产品分页列表
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/getGoodsPage")
    @ResponseBody
    public PageInfo<GoodsType> getGoodsPage(@RequestParam Map map, Pageable pageable){
        try {
            return goodsTypeService.getGoodsInfo(map,pageable);
        } catch (QinFeiException e) {
            return new PageInfo<>();
        }catch (Exception e){
            return new PageInfo<>();
        }
    }

    @RequestMapping("/save")
    @ResponseBody
    public ResponseData save(GoodsType goodsType){
        ResponseData data = ResponseData.ok();
        String name="";
        if(goodsType.getParentId()==0){
            name="产品分类";
        }else {
            name="产品";
        }
        try {
            goodsType.setCompanyCode(AppUtil.getUser().getDept().getCompanyCode());
            goodsTypeService.saveGoodsType(goodsType);
            data.putDataValue("message","操作成功");
            return data;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉,添加"+name+"出错啦,请联系技术人员");
        }
    }

    @RequestMapping("/editAjax")
    @ResponseBody
    public ResponseData editAjax(@RequestParam("id")Integer id){
        ResponseData data = ResponseData.ok();
        try {
            GoodsType goodsType = goodsTypeService.getById(id);
            data.putDataValue("entity",goodsType);
            return data;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉,查看产品分类信息出错啦,请联系技术人员");
        }
    }

    @RequestMapping("/getStockDataById")
    @ResponseBody
    public ResponseData getStockDataById(@RequestParam Map map){
        ResponseData data = ResponseData.ok();
        try {
            Map obj = goodsTypeService.getStockDataById(map);
            data.putDataValue("entity",obj);
            return data;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉,根据产品查询库存数量出错啦,请联系技术人员");
        }
    }

    @RequestMapping("/edit")
    @ResponseBody
    public ResponseData edit(GoodsType goodsType){
        ResponseData data = ResponseData.ok();
        String name="";
        if(goodsType.getParentId()==0){
            name="产品分类";
        }else {
            name="产品";
        }
        try {
            goodsType.setCompanyCode(AppUtil.getUser().getDept().getCompanyCode());
            goodsTypeService.updateGoodsType(goodsType);
            data.putDataValue("message","操作成功");
            return data;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉,修改"+name+"出错啦,请联系技术人员");
        }
    }

    @RequestMapping("/del")
    @ResponseBody
    public ResponseData del(@RequestParam("id")Integer id){
        ResponseData data = ResponseData.ok();
        try {
            goodsTypeService.del(id);
            data.putDataValue("message","操作成功");
            return data;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉,删除产品分类出错啦,请联系技术人员");
        }
    }

    @RequestMapping("/loadGoodsTypeInfo")
    @ResponseBody
    public ResponseData loadGoodsTypeInfo(){
        try {
            ResponseData data = ResponseData.ok();
            List<Map> list = goodsTypeService.loadGoodsTypeInfo(AppUtil.getUser().getDept().getCompanyCode());
            data.putDataValue("list",list);
            return data;
        } catch (QinFeiException e) {
            throw new QinFeiException(e.getCode(),e.getMessage());
        }catch (Exception e){
           e.printStackTrace();
           throw new QinFeiException(1002,"抱歉,加载产品分类数据出错啦,请联系技术人员");
        }
    }

    @RequestMapping("/loadGoodsTypeByParentId")
    @ResponseBody
    public ResponseData loadGoodsTypeByParentId(@RequestParam("parentId")Integer parentId){
        try {
            ResponseData data = ResponseData.ok();
            List<Map> list = goodsTypeService.loadGoodsTypeByParentId(parentId,AppUtil.getUser().getDept().getCompanyCode());
            data.putDataValue("list",list);
            return data;
        } catch (QinFeiException e) {
            throw new QinFeiException(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉,加载产品分类数据出错啦,请联系技术人员");
        }
    }

    @RequestMapping("/checkName")
    @ResponseBody
    public ResponseData checkName(@RequestParam("id")Integer id,@RequestParam("name")String name){
        try {
            ResponseData data = ResponseData.ok();
            List<GoodsType> list = goodsTypeService.getGoodsTypeByCondition(id,name,AppUtil.getUser().getCompanyCode());
            data.putDataValue("list",list);
            return data;
        } catch (QinFeiException e) {
            throw new QinFeiException(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"已存在相同产品名称");
        }
    }

    @RequestMapping("/checkGoodsTypeData")
    @ResponseBody
    public ResponseData checkGoodsTypeData(@RequestParam("parentId")Integer parentId){
        try {
            ResponseData data = ResponseData.ok();
            List<GoodsType> list = goodsTypeService.checkGoodsTypeData(parentId);
            data.putDataValue("list",list);
            return data;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 库存最大值预警
     * @param map
     * @return
     */
    @RequestMapping("/getStockMaxWarnData")
    @ResponseBody
    public PageInfo<Map> getStockMaxWarnData(@RequestParam Map map){
        try {
            if(hasInventoryAuthority()){
                return goodsTypeService.getStockMaxWarnData(map);
            }else {
                return null;
            }
        } catch (QinFeiException e) {
            throw new QinFeiException(e.getCode(),e.getMessage());
        }catch (Exception e){
            throw new QinFeiException(1002,"抱歉，库存最大值预警出错啦，请联系技术人员");
        }
    }

    /**
     * 库存最小值预警
     * @param map
     * @return
     */
    @RequestMapping("/getStockMinWarnData")
    @ResponseBody
    public PageInfo<Map> getStockMinWarnData(@RequestParam Map map){
        try {
            if(hasInventoryAuthority()){
                return goodsTypeService.getStockMinWarnData(map);
            }else {
                return null;
            }
        } catch (QinFeiException e) {
            throw new QinFeiException(e.getCode(),e.getMessage());
        }catch (Exception e){
            throw new QinFeiException(1002,"抱歉，库存最小值预警出错啦，请联系技术人员");
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
