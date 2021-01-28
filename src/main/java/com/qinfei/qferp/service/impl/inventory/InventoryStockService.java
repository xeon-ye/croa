package com.qinfei.qferp.service.impl.inventory;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.inventory.*;
import com.qinfei.qferp.entity.inventoryStock.*;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.inventory.*;
import com.qinfei.qferp.service.impl.fee.FeeCodeUtil;
import com.qinfei.qferp.service.inventory.IInventoryStockService;
import com.qinfei.qferp.service.inventory.IPurchaseService;
import com.qinfei.qferp.service.inventory.IReceiveDetailsService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sun.jna.platform.win32.OaIdl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/***
 * @CalssName
 * @Description 进销存--库存
 * @Autor dsg
 * @Date 2020/3/16
 *
 */
@Service
@Slf4j
public class InventoryStockService implements IInventoryStockService {
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private IPurchaseService purchaseService;
    @Autowired
    private ReceiveApplyMapper applyMapper;
    @Autowired
    private IReceiveDetailsService detailsService;
    @Autowired
    private Config config;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private GoodsTypeMapper goodsTypeMapper;
    @Autowired
    private IUserService userService;

    //物品库存标题
    List<String> inventoryTitle = Arrays.asList("*产品分类*","*产品名称*","*数量*","创建时间","创建人","*仓库*");
    static List<String> notices= new ArrayList<>();
    static {
        notices.add("表格的第一行、第一列留空请勿删除；");
        notices.add("带星号标注的列必须有内容；");
        notices.add("数量：根据数量生成库存");
        notices.add("创建时间格式:YYYY-MM-DD；");
    }

    @Override
    public Integer getPageCount(Map map) {
        map.put("companyCode", AppUtil.getUser().getCompanyCode());
        return inventoryMapper.getPageCount(map);
    }

    @Override
    public PageInfo<Map> listPg(Map<String,Object> map, Pageable pageable){
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        map.put("companyCode", AppUtil.getUser().getCompanyCode());
        List<Map> list= inventoryMapper.listPg(map);
        return new PageInfo(list);
    }

    @Override
    public Integer getOutStockPageCount(Map map) {
        map.put("companyCode", AppUtil.getUser().getCompanyCode());
        return inventoryMapper.getOutStockPageCount(map);
    }

    @Override
    public PageInfo<Map> getOutStockListPg(Map map,Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        map.put("companyCode", AppUtil.getUser().getCompanyCode());
        List<Map> list = inventoryMapper.getOutStockListPg(map);
        return new PageInfo<>(list);
    }


    /**
     * 新增物品入库（新增了库存不能修改）
     * 1、添加出入库记录，2、修改物品采购明细3、修改物品采购订单状态
     * @param outbound
     * @param purchaseDetails
     */
    @Transactional
    @Override
    public Outbound addOutBound(Outbound outbound, List<String> purchaseDetails) {
        try {
            //入库暂存功能
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            outbound.setCompanyCode(user.getCompanyCode());
            outbound.setCreateTime(new Date());
            //step1、添加出入库记录
            inventoryMapper.saveOutbound(outbound);
            //step2、修改物品采购明细信息
            if (CollectionUtils.isNotEmpty(purchaseDetails)) {
                for (int i = 0; i < purchaseDetails.size(); i++) {
                    String[] data = purchaseDetails.get(i).split(":");
                    Integer detailsId = 0;
                    Integer wareId = 0;
                    if (!StringUtils.isEmpty(data[0])) {
                        //物品采购明细id
                        detailsId = Integer.valueOf(data[0]);
                    }
                    if (!StringUtils.isEmpty(data[1])) {
                        //物品采购明细关联仓库id
                        wareId = Integer.valueOf(data[1]);
                    }
                    if (detailsId != null && wareId != null) {
                        Map map = new HashMap();
                        map.put("id", detailsId);
                        map.put("wareId", wareId);
                        purchaseService.editPurchaseDetailsByParam(map);
                    }
                }
            }
            //step3修改物品采购订单状态
            purchaseService.editPurchaseState(6, null,outbound.getForeignId());
            if(outbound.getState()==1){//入库提交功能
                purchaseService.editPurchaseState(7, null,outbound.getForeignId());
                List<PurchaseDetails>  list = purchaseService.getPurchaseDetailsById(outbound.getForeignId());
                List<Goods> goodsList = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(list)){
                    for(PurchaseDetails details :list){
                        Integer amount = details.getAmount();
                        for(int i = 0;i<amount;i++){
                            Goods goods = new Goods();
                            goods.setState(0);
                            goods.setTypeId(details.getType());
                            goods.setGoodsId(details.getGoodsId());
                            goods.setNumber(1);
                            goods.setPurchaseId(outbound.getForeignId());
                            goods.setCreateId(user.getId());
                            goods.setCreateName(user.getName());
                            goods.setCreateTime(new Date());
                            goods.setUpdateUserId(user.getId());
                            goods.setUpdateTime(new Date());
                            goods.setCompanyCode(user.getCompanyCode());
                            goods.setWarehouseId(details.getWarehouseId());
                            String inventoryCode = IConst.INVENTORY_CODE+ DateUtils.format(new Date(),"yyyyMMdd")+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.INVENTORY_CODE),5);
                            goods.setCode(inventoryCode);
                            goodsList.add(goods);
                        }
                    }
                    goodsMapper.addGoodsBatch(goodsList);
                }
            }
            return outbound;
        } catch (QinFeiException e) {
            throw e;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，新增物品入库数字转换出错啦，请联系技术人员");
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，新增物品入库出错啦，请联系技术人员");
        }
    }

    /**
     * 编辑物品入库
     * 不切换订单（1修改出入库记录表，2修改物品明细绑定仓库，3修改订单状态为2待入库）
     * 切换订单（1修改出入库记录表，2修改物品明细绑定仓库，3修改订单状态；还原原订单状态为1审批完成）
     * @param outbound
     * @param purchaseDetails
     * @return
     */
    @Transactional
    @Override
    public Outbound editOutBound(Outbound outbound, List<String> purchaseDetails) {
        try {
            //入库暂存功能
            Integer oldPurchaseId = outbound.getPurchaseId();
            Integer newPurchaseId = outbound.getForeignId();
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            outbound.setUpdateUserId(user.getId());
            outbound.setUpdateTime(new Date());
            outbound.setCompanyCode(user.getCompanyCode());
            //step1、修改出入库记录
            inventoryMapper.editOutbound(outbound);
            //step2、修改物品采购明细信息
            if (CollectionUtils.isNotEmpty(purchaseDetails)) {
                for (int i = 0; i < purchaseDetails.size(); i++) {
                    String[] data = purchaseDetails.get(i).split(":");
                    Integer detailsId = 0;
                    Integer wareId = 0;
                    if (!StringUtils.isEmpty(data[0])) {
                        //物品采购明细id
                        detailsId = Integer.valueOf(data[0]);
                    }
                    if (!StringUtils.isEmpty(data[1])) {
                        //物品采购明细关联仓库id
                        wareId = Integer.valueOf(data[1]);
                    }
                    if (detailsId != null && wareId != null) {
                        Map map = new HashMap();
                        map.put("id", detailsId);
                        map.put("wareId", wareId);
                        purchaseService.editPurchaseDetailsByParam(map);
                    }
                }
            }
            //step3修改物品采购订单状态
            purchaseService.editPurchaseState(6,null, outbound.getForeignId());
            if(!oldPurchaseId.equals(newPurchaseId)){
                //还原原订单状态为1审批完成
                purchaseService.editPurchaseState(1, null,oldPurchaseId);
                //根据采购id置空采购明细的仓库id
                purchaseService.editWarehouseByPurchaseId(oldPurchaseId);
            }
            if(outbound.getState()==1){//入库提交功能
                purchaseService.editPurchaseState(7, null,outbound.getForeignId());
                List<PurchaseDetails>  list = purchaseService.getPurchaseDetailsById(outbound.getForeignId());
                List<Goods> goodsList = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(list)){
                    for(PurchaseDetails details :list){
                        Integer amount = details.getAmount();
                        for(int i = 0;i<amount;i++){
                            Goods goods = new Goods();
                            //入库状态
                            goods.setState(0);
                            goods.setTypeId(details.getType());
                            goods.setGoodsId(details.getGoodsId());
                            goods.setNumber(1);
                            goods.setPurchaseId(newPurchaseId);
                            goods.setCreateId(user.getId());
                            goods.setCreateName(user.getName());
                            goods.setCreateTime(new Date());
                            goods.setUpdateUserId(user.getId());
                            goods.setUpdateTime(new Date());
                            goods.setCompanyCode(user.getCompanyCode());
                            if(details.getWarehouseId()==null){
                                goods.setWarehouseId(outbound.getWareId());
                            }else {
                                goods.setWarehouseId(details.getWarehouseId());
                            }
                            String inventoryCode = IConst.INVENTORY_CODE+ DateUtils.format(new Date(),"yyyyMMdd")+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.INVENTORY_CODE),5);
                            goods.setCode(inventoryCode);
                            goodsList.add(goods);
                        }
                    }
                    goodsMapper.addGoodsBatch(goodsList);
                }
            }
            return outbound;
        } catch (QinFeiException e) {
            throw e;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉编辑物品入库数字转换出错啦，请联系技术人员");
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉编辑物品入库出错啦，请联系技术人员");
        }
    }

    /**
     * 删除出入库记录（还原订单状态）
     * @param id
     */
    @Transactional
    @Override
    public void delOutboundById(Integer id,Integer purchaseId) {
        try {
            if(id==null || purchaseId==null){
                throw new QinFeiException(1002,"抱歉，找不到该记录，删除不了");
            }
            inventoryMapper.delOutboundById(id);
            //还原原订单状态为1审批完成
            purchaseService.editPurchaseState(1, null,purchaseId);
            //根据采购id置空采购明细的仓库id
            purchaseService.editWarehouseByPurchaseId(purchaseId);
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，删除出入库记录出错啦");
        }
    }

    /**
     * 新增出库
     * 添加出入库记录
     * 修改物品领用明细信息
     * @param outbound
     * @param applyDetails
     */
    @Transactional
    @Override
    public Outbound addOutStock(Outbound outbound, List<String> applyDetails) {
        //出库功能
        User user = AppUtil.getUser();
        outbound.setUpdateUserId(user.getId());
        outbound.setUpdateTime(new Date());
        outbound.setCompanyCode(user.getCompanyCode());
        outbound.setCreateTime(new Date());
        //step1、添加出入库记录
        inventoryMapper.saveOutbound(outbound);
        //step2修改物品领用订单状态 （in关键字）
        Integer applyState=null;
        if(outbound.getState()==0){
           applyState=6;//部分出库
        }else{
           applyState=7;//全部出库
        }
        applyMapper.editApplyState(applyState, outbound.getForeignId());
        //step3、修改物品领用明细信息
        if (CollectionUtils.isNotEmpty(applyDetails)) {
            Integer detailId = 0;
            Integer outAmount = 0;
            for (int i = 0; i < applyDetails.size(); i++) {
                String[] data = applyDetails.get(i).split(":");
                if (!StringUtils.isEmpty(data[0])) {
                    //物品领用明细id
                    detailId = Integer.valueOf(data[0]);
                }
                if (!StringUtils.isEmpty(data[1])) {
                    //物品领用明细出库数量
                    outAmount = Integer.valueOf(data[1]);
                }
                if (detailId != null && outAmount != null) {
                    Map map = new HashMap();
                    map.put("id", detailId);
                    map.put("outAmount", outAmount);
                    ReceiveDetails obj = detailsService.getById(detailId);
                    if(outAmount.equals(obj.getAmount())){
                        //1全部出库,2部分出库
                        map.put("state",1);
                    }else{
                        map.put("state",2);
                    }
                    detailsService.editReceiveDetailsByParam(map);
                }
            }
        }
        List<ReceiveDetails> list = detailsService.getReceiveDetailById(outbound.getForeignId(),user.getCompanyCode());
        ReceiveApply entity = applyMapper.getById(outbound.getForeignId());
        if (CollectionUtils.isNotEmpty(list)) {
            for (ReceiveDetails details : list) {
                //产品id
                Integer goodsId = details.getGoodsId();
                Map condition=new HashMap();
                condition.put("goodsId",goodsId);
                condition.put("wareId",entity.getWareId());
                //某产品所有的库存id
                List<Integer> ids = goodsMapper.getGoodsIdByProductId(condition);
                List<Integer> tempList = new ArrayList<>();
                //申请数量
                Integer amount = details.getAmount();
                //物品领用明细id
                Integer applyDetailsId = details.getId();
                //前台传来的物品明细id
                Integer detailId = 0;
                //前台传来的物品出库数量
                Integer outAmount = 0;
                if (CollectionUtils.isNotEmpty(applyDetails)) {
                    for (int i = 0; i < applyDetails.size(); i++) {
                        String [] data = applyDetails.get(i).split(":");
                        if (!StringUtils.isEmpty(data[1])) {
                            //物品领用明细出库数量
                            outAmount = Integer.valueOf(data[1]);
                        }
                        if (!StringUtils.isEmpty(data[0])) {
                            //物品领用明细id
                            detailId = Integer.valueOf(data[0]);
                            //物品明细id相同,缓存明细id及出库数量
                            if(applyDetailsId.equals(detailId)){
                                break;
                            }
                        }
                    }
                }
                //页面输入数量必须小于申请数量
                if(outAmount<=amount){
                    //待出库数量
                    if(outAmount<=ids.size()){
                        if(outAmount>0){
                            if(CollectionUtils.isNotEmpty(ids)){
                                for (int i = 0; i < outAmount; i++) {
                                    tempList.add(ids.get(i));
                                }
                            }
                            Map map = new HashMap();
                            //出库状态
                            map.put("state", -1);
                            map.put("applyId", outbound.getForeignId());
                            //使用人id
                            map.put("userId", details.getUserId());
                            //归还日期
                            map.put("returnTime", details.getReturnTime());
                            map.put("updateUserId", user.getId());
                            map.put("updateTime", new Date());
                            map.put("list", tempList);
                            //step4、修改库存产品信息
                            goodsMapper.updateGoodsBatch(map);
                        }
                    }else{
                        throw new QinFeiException(1002,"物品出库数量必须大于库存数量，请核实库存");
                    }
                }else{
                    throw new QinFeiException(1002,"物品出库数量必须大于申请数量");
                }
            }
        }
        return outbound;
    }

    /**
     * 编辑出库
     * 修改物品领用明细信息
     * @param outbound
     * @param applyDetails
     */
    @Override
    public Outbound editOutStock(Outbound outbound, List<String> applyDetails) {
        User user = AppUtil.getUser();
        outbound.setUpdateUserId(user.getId());
        outbound.setUpdateTime(new Date());
        outbound.setCompanyCode(user.getCompanyCode());
        //step1、修改出入库记录
        inventoryMapper.editOutbound(outbound);
        //step2、修改物品领用订单状态 （in关键字）
        Integer applyState=null;
        if(outbound.getState()==0){
            applyState=6;//部分出库
        }else{
            applyState=7;//全部出库
        }
        applyMapper.editApplyState(applyState, outbound.getForeignId());
        List<ReceiveDetails> list = detailsService.getReceiveDetailById(outbound.getForeignId(),user.getCompanyCode());
        ReceiveApply entity = applyMapper.getById(outbound.getForeignId());
        if (CollectionUtils.isNotEmpty(list)) {
            for (ReceiveDetails details : list) {
                //产品id
                Integer goodsId = details.getGoodsId();
                Map condition=new HashMap();
                condition.put("goodsId",goodsId);
                condition.put("wareId",entity.getWareId());
                //某产品所有的库存id
                List<Integer> ids = goodsMapper.getGoodsIdByProductId(condition);
                List<Integer> tempList = new ArrayList<>();
                //申请数量
                Integer amount = details.getAmount();
                //已出库数量
                Integer outStockAmount = StringUtils.isEmpty(details.getOutAmount())?0:details.getOutAmount();
                //物品领用明细id
                Integer applyDetailsId = details.getId();
                //待出库数量
                Integer needOutStockAmount=0;
                //前台传来的物品明细id
                Integer detailId = 0;
                //前台传来的物品出库数量
                Integer outAmount = 0;
                if (CollectionUtils.isNotEmpty(applyDetails)) {
                    for (int i = 0; i < applyDetails.size(); i++) {
                        String [] data = applyDetails.get(i).split(":");
                        if (!StringUtils.isEmpty(data[1])) {
                            //物品领用明细出库数量
                            outAmount = Integer.valueOf(data[1]);
                        }
                        if (!StringUtils.isEmpty(data[0])) {
                            //物品领用明细id
                            detailId = Integer.valueOf(data[0]);
                            //物品明细id相同,缓存明细id及出库数量
                            if(applyDetailsId.equals(detailId)){
                                break;
                            }
                        }
                    }
                }
                //页面输入数量必须小于申请数量
                if(outAmount<=amount){
                    //待出库数量
                    needOutStockAmount=outAmount-outStockAmount;
                    if(needOutStockAmount<=ids.size()){
                        if(needOutStockAmount>0){
                            if(CollectionUtils.isNotEmpty(ids)){
                                for (int i = 0; i < needOutStockAmount; i++) {
                                    tempList.add(ids.get(i));
                                }
                            }
                            Map map = new HashMap();
                            //出库状态
                            map.put("state", -1);
                            map.put("applyId", outbound.getForeignId());
                            //使用人id
                            map.put("userId", details.getUserId());
                            //归还日期
                            map.put("returnTime", details.getReturnTime());
                            map.put("updateUserId", user.getId());
                            map.put("updateTime", new Date());
                            map.put("list", tempList);
                            //step3、修改库存产品信息
                            goodsMapper.updateGoodsBatch(map);
                            //step4、修改物品领用明细信息
                            if (detailId != null && outAmount != null) {
                                Map map2 = new HashMap();
                                map2.put("id", detailId);
                                map2.put("outAmount", outAmount);
                                if(outAmount.equals(amount)){
                                    //1全部出库,2部分出库
                                    map2.put("state",1);
                                }else{
                                    map2.put("state",2);
                                }
                                //step4、修改物品领用明细信息
                                detailsService.editReceiveDetailsByParam(map2);
                            }
                        }
                    }else{
                        throw new QinFeiException(1002,"物品待出库数量必须大于库存数量，请核实库存");
                    }
                }else{
                    throw new QinFeiException(1002,"物品出库数量必须大于申请数量");
                }
            }
        }
        return outbound;
    }

    @Override
    public String getOutboundCode() {
        return IConst.INVENTORY_STOCK + DateUtils.format(new Date(),"yyyyMMdd")+CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.INVENTORY_STOCK), 5);
    }

    @Override
    public String getOutStockCode() {
        return IConst.OUTSTOCK_CODE + DateUtils.format(new Date(),"yyyyMMdd")+CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.OUTSTOCK_CODE), 5);
    }

    @Override
    public String getInventoryCode(){
        return IConst.INVENTORY_CODE+ DateUtils.format(new Date(),"yyyyMMdd")+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.INVENTORY_CODE),5);
    }

    @Override
    public ResponseData viewOutbound(Integer id){
        try{
            ResponseData data = ResponseData.ok();
            if (id==null){
                throw new QinFeiException(1002,"没有获取到入库id");
            }
            //根据出入库id查询出入库信息
            Outbound outbound = inventoryMapper.getById(id);
            //获取物品采购订单信息
            Purchase purchase = purchaseService.getById2(outbound.getForeignId());
            data.putDataValue("outbound",outbound);
            data.putDataValue("purchase",purchase);
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, e.getMessage());
        }
    }

    @Override
    public ResponseData editAjax(Integer id) {
        try{
            ResponseData data = ResponseData.ok();
            if (id==null){
                throw new QinFeiException(1002,"没有获取到出库id");
            }
            //根据出入库id查询出入库信息
            Outbound outbound = inventoryMapper.getById(id);
            //获取物品领用订单信息
            ReceiveApply apply = applyMapper.getById(outbound.getForeignId());
            data.putDataValue("outbound",outbound);
            data.putDataValue("apply",apply);
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, e.getMessage());
        }
    }

    @Override
    public void exportTemplate(OutputStream outputStream){
        List<Map<String,Object>> sheetInfo =new ArrayList<>();
        Map<String,Object> purchaseSheet = new HashMap<>();
        List<String> purchaseRowTitles = new ArrayList<>();
        purchaseRowTitles.addAll(inventoryTitle);
        purchaseSheet.put("templateName","产品库存导入");
        purchaseSheet.put("rowTitles",purchaseRowTitles);
        purchaseSheet.put("notices",notices);
        sheetInfo.add(purchaseSheet);
        DataImportUtil.createMoreSheetFile(sheetInfo,outputStream);
    }

    @Override
    @Transactional
    public String importInventoryData(String fileName){
        File file = new File(fileName);
        String result = null;
        if(!file.exists()){
            file.mkdirs();
        }
        if(file.exists()){
            Map<String,Object> purchaseResult = handlePurchaseData(file,0);
            List<Map<String, Object>> sheetInfo = new ArrayList<>();
            //打印出错的信息，如果产品库存无误正常导入
            if (purchaseResult != null && purchaseResult.size() > 0) {
                sheetInfo.add(purchaseResult);
            }
            if (sheetInfo.size() > 0) {
                result = DataImportUtil.createMoreSheetFile("产品库存导入失败内容", sheetInfo, config.getUploadDir(), config.getWebDir());
            }
            if(result!=null){
                throw new QinFeiException(1003,result);//如果有内容错误，则抛出异常，事务失效，防止一次导入多个sheet表时，前面sheet数据正常而导致数据入库
            }
            return result;
        }else {
            throw new QinFeiException(1002,"导入文件不存在");
        }
    }

    private Map<String,Object> handlePurchaseData(File file,Integer type){
        List<Object[]> execlContent = null;
        Map<String,Object> handResult = null;
        //产品库存
        execlContent = EasyExcelUtil.getExcelContent(file,type+1,3,2);
        if(CollectionUtils.isNotEmpty(execlContent)){
            handResult = dealInventoryData(AppUtil.getUser(),execlContent);
        }
        return handResult;
    }

    private Map<String,Object> dealInventoryData(User user,List<Object[]> excelContent){
        List<String> rowTitles = new ArrayList<>();//导入模板总列数
        rowTitles.addAll(inventoryTitle);
        int totalColumnNum = rowTitles.size();//总列数
        int rowNum = excelContent.size();//导入物品库存行数
        List<Object[]> validErrorData = new ArrayList<>(); //用于保存校验未通过的数据；
        Map<String,Object> errorMap = new HashMap<>();
        List<Goods> list = new ArrayList<>();
        Object [] row = null;
        Boolean flag = true;
        for (int i =0; i<rowNum;i++){
            row = excelContent.get(i);
            if (row.length<=1){
                continue;
            }
            if (row.length!=totalColumnNum){
                row = Arrays.copyOf(row,row.length+1);
                row[row.length-1]="模板格式不对应,请核实模板是否一致";
                validErrorData.add(row);
                flag= false;
                break;
            }
            boolean isValidSuccess= true;
            Goods goods= new Goods();
            String companyCode = user.getDept().getCompanyCode();
            for (int j = 0;j<row.length ;j++){
                String columValue = String.valueOf(row[j]);
                boolean requiredFlag = validField(rowTitles,j);
                if (columValue == null || "".equals(columValue) && !requiredFlag){
                    columValue="";
                }
                String errorInfo = null;
                if (!StringUtils.isEmpty(columValue)){
                    if (j==0){
                        //产品分类
                        Map map = new HashMap();
                        map.put("typeFlag", 1);
                        map.put("name", columValue);
                        map.put("companyCode", companyCode);
                        List<GoodsType> typeList = goodsTypeMapper.getSameNameList(map);
                        if (CollectionUtils.isNotEmpty(typeList)) {
                            goods.setTypeId(typeList.get(0).getId());
                        } else {
                            errorInfo="未找到";
                        }
                    }else if (j==1){
                        //产品名称
                        Map map = new HashMap();
                        map.put("name", columValue);
                        map.put("companyCode", companyCode);
                        List<GoodsType> goodsList = goodsTypeMapper.getSameNameList(map);
                        if (CollectionUtils.isNotEmpty(goodsList)) {
                            goods.setGoodsId(goodsList.get(0).getId());
                        } else {
                            errorInfo="未找到";
                        }
                    }else if(j==2){
                        //数量
                        try {
                            Integer amount=Integer.valueOf(columValue);
                            if(amount<=0){
                                errorInfo="必须大于0";
                            }
                            goods.setNumber(amount);
                        } catch (NumberFormatException e) {
                            errorInfo="格式不对";
                        }
                    }else if(j==3){
                        //创建时间
                        try {
                            Date createTime;
                            if (columValue.indexOf("/") > 0) {
                                createTime = DateUtils.parse(columValue, "yyyy/MM/dd");
                            } else if (columValue.indexOf("-") > 0) {
                                createTime = DateUtils.parse(columValue, "yyyy-MM-dd");
                            } else {
//                            我们期望这个日期解析出来是：2019/10/30,而结果却是43768.什么原因呢？这个数字是什么呢？是以1900年为原点，到2019年10月30日，之间经过的天数。
//                            知道这个后，就很好处理了，我们拿到1900年的日期，在这个日期上加上43768天即可
                                Calendar calendar = new GregorianCalendar(1900, 0, -1);
                                Date d = calendar.getTime();
////                            然后，利用DateUtils的方法，加上天数（这个天数被转为了字符串，值为43768）
                                createTime = DateUtils.getAfterDay(d, Integer.valueOf(columValue));
                            }
                            goods.setCreateTime(createTime);
                        } catch (Exception e) {
                            errorInfo="格式不对";
                        }
                    }else if(j==4){
                        //创建人
                        Map map = new HashMap();
                        map.put("name", columValue);
                        List<User> userList = userService.listByParams(map);
                        if (CollectionUtils.isNotEmpty(userList)) {
                            Integer userId = userList.get(0).getId();
                            goods.setUserId(userId);
                        } else {
                            errorInfo = "不存在";
                        }
                    }else if (j==5){
                        //仓库
                        Map map = new HashMap();
                        map.put("warehouseName",columValue);
                        map.put("companyCode",user.getCompanyCode());
                        List<Warehouse> warehouseList = warehouseMapper.warehouseList(map);
                        if (CollectionUtils.isNotEmpty(warehouseList)){
                            goods.setWarehouseId(warehouseList.get(0).getId());
                        }else {
                            errorInfo="不存在！";
                        }
                    }
                }else {
                    if (requiredFlag) {
                        errorInfo="不能为空";
                    }
                }
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(errorInfo)){
                    row = Arrays.copyOf(row, row.length + 1);
                    if (rowTitles.get(j).matches("^\\*.+\\*$")) { //如果是必输字段，则去除开头和结尾的*
                        row[row.length - 1] = rowTitles.get(j).substring(1, rowTitles.get(j).length() - 1) + errorInfo;
                    } else {
                        row[row.length - 1] = rowTitles.get(j) + errorInfo;
                    }
                    validErrorData.add(row); //缓存校验未通过数据
                    isValidSuccess = false;
                    break; //直接进入下一个采购数据判断
                }

            }
            if (!isValidSuccess) {//数据校验不成功继续下一个产品库存
                continue;
            }
            list.add(goods);
        }
        //判断是否存在有效数据
        //如果有错误，则数据不入库，提示错误信息
        int errorSize = validErrorData.size();
        if (errorSize >0){
            //导入失败原因
            rowTitles.add("失败原因");
            errorMap.put("templateName","产品库存导入失败内容");
            errorMap.put("rowTitles",rowTitles);
            errorMap.put("exportData",validErrorData);
        }
        int successSize = list.size();
        if (successSize >0){
            //添加产品库存
            List<Goods> objList = new ArrayList<>();
            for(Goods goods:list){
                Integer amount=goods.getNumber();
                if(amount>0){
                    for(int m=0;m<amount;m++){
                        Goods currentGoods=new Goods();
                        String inventoryCode = IConst.INVENTORY_CODE+ DateUtils.format(new Date(),"yyyyMMdd")+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.INVENTORY_CODE),5);
                        currentGoods.setCode(inventoryCode);
                        currentGoods.setState(0);
                        currentGoods.setTypeId(goods.getTypeId());
                        currentGoods.setGoodsId(goods.getGoodsId());
                        currentGoods.setNumber(1);
                        currentGoods.setCreateId(user.getId());
                        currentGoods.setCreateName(user.getName());
                        currentGoods.setUpdateUserId(user.getId());
                        currentGoods.setUpdateTime(new Date());
                        currentGoods.setCreateTime(new Date());
                        currentGoods.setCompanyCode(user.getDept().getCompanyCode());
                        currentGoods.setWarehouseId(goods.getWarehouseId());
                        objList.add(currentGoods);
                    }
                }
            }
            goodsMapper.addGoodsBatch(objList);
        }
        if(flag==false){
            rowTitles.add("失败原因");
            errorMap.put("templateName","产品库存导入失败内容");
            errorMap.put("rowTitles",rowTitles);
            errorMap.put("exportData",validErrorData);
        }
        return errorMap;
    }

    /**
     * 判断指定列值是否必输（根据**判断）
     * @param rowTitles 所有列名称
     * @param columnIndex 列索引
     */
    private boolean validField(List<String> rowTitles, int columnIndex){
        if(rowTitles.get(columnIndex).matches("^\\*.+\\*$")){
            return true;
        }
        return false;
    }

    //产品库存导出
    @Override
    public List<Map> exportInventoryDetail(Map map , OutputStream outputStream){
            try {
                User user = AppUtil.getUser();
                if(user==null){
                    throw new QinFeiException(1002,"请先登录");
                }
                String companyCode = user.getCompanyCode();
                map.put("companyCode",companyCode);
                List<Map> list = goodsMapper.listPg(map);
                List<Map> typeList = goodsTypeMapper.loadGoodsTypeInfo(companyCode);
                List<Map> wareList = warehouseMapper.getWarehouseList(companyCode);
                String[] heads = {"库存编号","产品分类","产品名称","单位","数量","创建时间","仓库","金额","库存状态"};
                String[] fields= {"code","typeId","goodsName","unit","number","createTime","warehouseId","price","state"};
                ExcelUtil.exportExcel("产品库存信息", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                     if(value!=null){
                         if("typeId".equals(field)){
                             Integer typeId = Integer.valueOf(value.toString());
                             for(int i=0;i<typeList.size();i++){
                                 Map obj =  typeList.get(i);
                                 Integer id = Integer.valueOf(obj.get("id").toString());
                                 String name = obj.get("name").toString();
                                 if(typeId.equals(id)){
                                     cell.setCellValue(name);
                                     break;
                                 }
                             }
                         }else if("warehouseId".equals(field)) {
                             Integer warehouseId = Integer.valueOf(value.toString());
                             for(int i=0;i<wareList.size();i++){
                                 Map obj2 =  wareList.get(i);
                                 Integer id2 = Integer.valueOf(obj2.get("id").toString());
                                 String name2 = obj2.get("name").toString();
                                 if(warehouseId.equals(id2)){
                                     cell.setCellValue(name2);
                                     break;
                                 }
                             }
                         }else if("state".equals(field)){
                              Integer state = Integer.valueOf(value.toString());
                              String stateTips = "";
                              if(state==0){
                                  stateTips="库存";
                              }else if(state==1){
                                  stateTips="报修";
                              }else if(state==2){
                                  stateTips="报废";
                              }else if(state==3){
                                  stateTips="归还";
                              }else if(state==-1){
                                  stateTips="使用中";
                              }
                             cell.setCellValue(stateTips);
                         }else{
                             cell.setCellValue(value.toString());
                         }
                     }
                });
                return list;
            } catch (Exception e) {
                e.printStackTrace();
                throw new QinFeiException(1002,"很抱歉，物品产品库存导出出错啦，请联系技术人员");
            }finally {
                try {
                    if(outputStream!=null){
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    @Override
    public ResponseData warehousingDetail(Integer id){
        try {
            ResponseData data = ResponseData.ok();
            List<Map<String,Object>> list = inventoryMapper.warehousingDetail(id);
            data.putDataValue("list",list);
            return  data;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，物品入库显示物品采购明细信息出错啦，请联系技术人员");
        }
    }

    @Override
    public List<Map<String, Object>> applyDetailsData(Integer id) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            ReceiveApply apply=applyMapper.getById(id);
            if(ObjectUtils.isEmpty(apply)){
                throw new QinFeiException(1002,"找不到领用记录");
            }
            Integer wareId=apply.getWareId();
            Map map=new HashMap();
            map.put("id",id);
            map.put("companyCode",AppUtil.getUser().getCompanyCode());
            map.put("wareId",wareId);
            return inventoryMapper.applyDetailsData(map);
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e){
            throw new QinFeiException(1002,"抱歉,物品领用显示明细信息报错啦，请联系技术人员");
        }
    }

    @Transactional
    @Override
    public void lossEffect(Outbound outbound) {
        try {
            Integer purchaseId=0;
            if(outbound.getId()==null){
                //新增
                purchaseId=outbound.getForeignId();
            }else {
                //编辑
                purchaseId=outbound.getPurchaseId();
                //使出入库记录失效
                inventoryMapper.editOutboundState(-5,outbound.getId());
            }
            Purchase purchase = purchaseService.getById2(purchaseId);
            if(!ObjectUtils.isEmpty(purchase)){
                purchaseService.editPurchaseState(-5,outbound.getRejectReason(),purchaseId);
            }else{
                throw new QinFeiException(1002,"找不到该采购单");
            }
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，采购单失效出错了，请联系技术人员");
        }
    }
}
