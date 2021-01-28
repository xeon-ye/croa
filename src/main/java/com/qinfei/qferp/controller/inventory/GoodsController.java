package com.qinfei.qferp.controller.inventory;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.inventory.Goods;
import com.qinfei.qferp.entity.inventory.GoodsRecord;
import com.qinfei.qferp.service.inventory.IGoodsRecordService;
import com.qinfei.qferp.service.inventory.IGoodsService;
import com.qinfei.qferp.utils.DataImportUtil;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 产品控制器
 * @author tsf
 */
@Slf4j
@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private IGoodsService goodsService;

    //获取产品库存分页数量
    @RequestMapping("/getPageCount")
    @ResponseBody
    public ResponseData getPageCount(@RequestParam Map map, Pageable pageable){
        try {
            ResponseData data = ResponseData.ok();
            Integer total = goodsService.getPageCount(map);
            data.putDataValue("total",total);
            return data;
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，获取产品库存分页数量出错啦，请联系技术人员");
        }
    }

    //获取产品库存分页信息
    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<Map> listPg(@RequestParam Map map, Pageable pageable){
        try {
            return goodsService.listPg(map,pageable);
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，获取产品库存分页数据出错啦，请联系技术人员");
        }
    }

    //库存盘点选择产品查询库存产品的数量
    @RequestMapping("/getTotalAmount")
    @ResponseBody
    public ResponseData getTotalAmount(@RequestParam Map map, Pageable pageable){
        try {
            ResponseData data = ResponseData.ok();
            Integer total = goodsService.getTotalAmount(map);
            data.putDataValue("total",total);
            return data;
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，获取选择产品分页数量出错啦，请联系技术人员");
        }
    }

    //库存盘点选择产品查询库存产品数据
    @RequestMapping("/queryGoodsData")
    @ResponseBody
    public PageInfo<Map> queryGoodsData(@RequestParam Map map, Pageable pageable){
        try {
            return goodsService.queryGoodsData(map,pageable);
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，获取选择产品分页数据出错啦，请联系技术人员");
        }
    }

    //根据产品id产品库存数量
    @RequestMapping("/getStockAmountById")
    @ResponseBody
    public ResponseData getStockAmountById(@RequestParam Map map){
        try {
            ResponseData data = ResponseData.ok();
            data.putDataValue("amount",goodsService.getStockAmountById(map));
            return data;
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，切换仓库重新计算库存出错啦，请联系技术人员");
        }
    }

    /**
     * 选择产品(库存盘点)显示在盘点明细的数据
     * @param wareId
     * @param ids
     * @param pageable
     * @return
     */
    @RequestMapping("/getGoodsList")
    @ResponseBody
    public PageInfo<Map> getGoodsList(@RequestParam("wareId")Integer wareId,@RequestParam(value = "ids[]",required = false) List<Integer> ids,Pageable pageable){
        try {
            return goodsService.getGoodsList(wareId,ids,pageable);
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，获取选择产品分页数据出错啦，请联系技术人员");
        }
    }

    //根据id查询产品信息
    @RequestMapping("/editAjax")
    @ResponseBody
    public ResponseData editAjax(@RequestParam("id")Integer id){
        try {
            ResponseData data = ResponseData.ok();
            Map map = goodsService.getById(id);
            data.putDataValue("entity",map);
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002,"抱歉,根据产品id查询库存信息出错啦,请联系技术人员");
        }
    }

    //根据id查询产品信息
    @RequestMapping("/getGoodsById")
    @ResponseBody
    public ResponseData getGoodsById(@RequestParam("id")Integer id){
        try {
            ResponseData data = ResponseData.ok();
            Goods goods = goodsService.getGoodsById(id);
            data.putDataValue("entity",goods);
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002,"抱歉,根据产品编号查询产品信息出错啦,请联系技术人员");
        }
    }

    @RequestMapping("/save")
    @ResponseBody
    public ResponseData save(Goods goods){
        try {
            ResponseData data = ResponseData.ok();
            goodsService.save(goods);
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002,"抱歉,新增产品信息出错啦,请联系技术人员");
        }
    }

    @RequestMapping("/edit")
    @ResponseBody
    public ResponseData edit(Goods goods){
        try {
            ResponseData data = ResponseData.ok();
            goodsService.update(goods);
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002,"抱歉,修改产品信息出错啦,请联系技术人员");
        }
    }

    @RequestMapping("/addGoodsBatch")
    @ResponseBody
    public ResponseData addGoodsBatch(Goods goods){
        try {
            ResponseData data = ResponseData.ok();
            goodsService.addGoodsBatch(goods);
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002,"抱歉,添加库存信息出错啦,请联系技术人员");
        }
    }

    @RequestMapping("/del")
    @ResponseBody
    public ResponseData del(@RequestParam("id")Integer id){
        try {
            ResponseData data = ResponseData.ok();
            goodsService.del(id);
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002,"抱歉,删除产品信息出错啦,请联系技术人员");
        }
    }

    //根据产品分类id查询产品信息
    @RequestMapping("/getGoodsByParentId")
    @ResponseBody
    public ResponseData getGoodsByParentId(@RequestParam("parentId") Integer parentId){
        try {
            ResponseData data = ResponseData.ok();
            List<Goods> list = goodsService.getGoodsByParentId(parentId);
            data.putDataValue("list",list);
            return data;
        }catch (QinFeiException e){
           return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉,根据产品分类编号查询产品信息出错啦,请联系技术人员");
        }
    }

    //判断产品名称是否重复
    @RequestMapping("/checkSameName")
    @ResponseBody
    public ResponseData checkSameName(@RequestParam("id") Integer id,@RequestParam("name")String name){
        try {
            ResponseData data = ResponseData.ok();
            List<Goods> list = goodsService.checkSameName(id,name);
            data.putDataValue("list",list);
            return data;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，检查产品名称重复出错啦，请联系技术人员");
        }
    }

    /**
     * 根据库存Id查询库存操作记录
     * @param id
     * @return
     */
    @RequestMapping("/queryByInventoryId")
    @ResponseBody
    public ResponseData queryByInventoryId(@RequestParam("id") Integer id){
        try {
            ResponseData data = ResponseData.ok();
            List<Map> list = goodsService.getInventoryById(id);
            data.putDataValue("list",list);
            return data;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，根据库存id查询库存操作记录出错啦！");
        }
    }

    /**
     * 获取导入模板
     * @param response
     */
    @RequestMapping("/exportTemplate")
    @ResponseBody
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response){
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = "产品导入模板";
            String index = UUIDUtil.get8UUID();
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + index + ".xls");
            OutputStream outputStream = response.getOutputStream();
            goodsService.exportTemplate(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量导入产品
     * @param multipartFile
     */
    @RequestMapping("/batchGoodsForEasyExcel")
    @ResponseBody
    public ResponseData batchGoodsForEasyExcel(@RequestParam(value = "file") MultipartFile multipartFile){
        try{
            String fileName = multipartFile.getOriginalFilename();
            if(fileName.indexOf(".xls")>-1){
                String errMessage = goodsService.importGoodsData(multipartFile);
                if(StringUtils.isEmpty(errMessage)){
                    return ResponseData.ok().putDataValue("message","产品导入成功");
                }else{
                    return ResponseData.customerError(1002,errMessage);
                }
            }else{
                return ResponseData.customerError(1001,"上传的文件类型不正确！") ;
            }
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002,"产品导入失败");
        }
    }
}
