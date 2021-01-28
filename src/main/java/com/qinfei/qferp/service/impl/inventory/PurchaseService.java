package com.qinfei.qferp.service.impl.inventory;

import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.inventory.*;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.inventory.GoodsMapper;
import com.qinfei.qferp.mapper.inventory.PurchaseMapper;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.fee.FeeCodeUtil;
import com.qinfei.qferp.service.inventory.IGoodsTypeService;
import com.qinfei.qferp.service.inventory.IPurchaseService;
import com.qinfei.qferp.service.inventory.IPurchaseSupplierService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.service.workbench.IItemsService;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import java.io.File;
import java.io.OutputStream;
import java.util.*;

/**
 * 物品采购
 * @author tsf
 */
@Service
public class PurchaseService implements IPurchaseService{
    @Autowired
    private PurchaseMapper purchaseMapper;
    @Autowired
    private IItemsService itemsService;
    @Autowired
    private IProcessService processService;
    @Autowired
    private IUserService userService;
    @Autowired
    private Config config;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private IGoodsTypeService goodsTypeService;
    @Autowired
    private IPurchaseSupplierService purchaseSupplierService;

    //物品采购标题
    List<String> purchaseTitle = Arrays.asList("*采购编号*","*采购标题*","*采购员*","*采购日期*","备注");
    List<String> purchaseField = Arrays.asList("purchaseCode","title","buyerId","purchaseTime","createId","createTime","description");
    //物品采购明细标题
    List<String> purchaseDetailTitle = Arrays.asList("*采购编号*","*产品分类*","*产品名称*","数量","*单价*","*供应商*","*支付方式*");
    List<String> purchaseDetailField = Arrays.asList("purchaseCode","parentId","goodsId","amount","price","supplierId","payMethod");
    static List<String> notices= new ArrayList<>();
    static List<String> notices2= new ArrayList<>();
    static{
        notices.add("表格的第一行、第一列留空请勿删除；");
        notices.add("表格数据第一列不能为空；");
        notices.add("带星号标注的列必须有内容；");
        notices.add("采购日期格式:YYYY-MM-DD；");

        notices2.add("表格的第一行、第一列留空请勿删除；");
        notices2.add("表格数据第一列不能为空；");
        notices2.add("带星号标注的列必须有内容；");
        notices2.add("采购编号是物品采购信息中的编号,物品编号存在时进行更新，不存在则提示物品编号不存在；");
        notices2.add("支付方式：微信，支付宝，银行卡；");
    }

    @Override
    public Purchase getById(Integer id) {
        Purchase purchase = null;
        try {
            //物品采购记录
            purchase = purchaseMapper.getById(id);
            //物品明细数据
            List<PurchaseDetails> purchaseDetails = purchaseMapper.getPurchaseDetailsById(id);
            //将物品采购记录完善
            if(!ObjectUtils.isEmpty(purchase)){
                purchase.setPurchaseDetails(purchaseDetails);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，物品采购根据采购编号查询出错啦，请联系技术人员");
        }
        return purchase;
    }

    @Override
    public Purchase getById2(Integer id) {
        return purchaseMapper.getById(id);
    }

    @Override
    public Integer getPageCount(Map map) {
        int result=0;
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            map.put("companyCode",user.getCompanyCode());
            result = purchaseMapper.getPageCount(map);
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，物品采购获取分页数量出错啦，请联系技术人员");
        }
        return result;
    }

    @Override
    public PageInfo<Map> getPurchasePage(Map map, Pageable pageable) {
        PageInfo<Map> pageInfo = null;
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            map.put("companyCode",user.getCompanyCode());
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            List<Map> list = purchaseMapper.getPurchasePage(map);
            pageInfo = new PageInfo<>(list);
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，获取物品采购分页列表出错啦，请联系技术人员");
        }
        return pageInfo;
    }

    @Override
    @Transactional
    public Purchase savePurchase(Purchase purchase, List<Integer> type, List<Integer> goodsId, List<String> specs, List<String> unit,
                             List<Integer> amount, List<Double> price, List<Double> totalMoney,List<Integer> supplierId,List<Integer> payMethod) {
        try {
            //step1:处理物品采购信息
            User user = AppUtil.getUser();
            purchase.setId(null);
            purchase.setDeptId(user.getDeptId());
            purchase.setCompanyCode(AppUtil.getUser().getCompanyCode());
            purchase.setCreateTime(new Date());
            purchaseMapper.insert(purchase);
            if(purchase.getState()>0){
                //step2:发起流程
                processService.addPurchaseProcess(purchase,3);
            }
            //step3:进行物品明细数据的录入
            insertPurchaseDetailsData(purchase,type,goodsId,specs,unit,amount,price,totalMoney,supplierId,payMethod);
            return purchase;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，新增物品采购信息出错啦，请联系技术人员");
        }
    }

    @Override
    @Transactional
    public Purchase editPurchase(Purchase purchase, List<Integer> type, List<Integer> goodsId, List<String> specs, List<String> unit,
                             List<Integer> amount, List<Double> price, List<Double> totalMoney,List<Integer> supplierId,List<Integer> payMethod) {
        try {
            //step1:处理主表信息
            User user = AppUtil.getUser();
            purchase.setUpdateUserId(user.getId());
            purchase.setUpdateTime(new Date());
            purchaseMapper.editPurchase(purchase);
            //处理代办
            finishItem(purchase);
            //物品采购暂存
            if(purchase.getState()>0){
                //发起流程（提交审核）
                processService.addPurchaseProcess(purchase,3);
            }
            //step2:处理物品采购子表
            purchaseMapper.delPurchaseDetailsBatch(purchase.getId());
            insertPurchaseDetailsData(purchase,type,goodsId,specs,unit,amount,price,totalMoney,supplierId,payMethod);
            return purchase;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，编辑物品采购信息出错啦，请联系技术人员");
        }
    }

    @Override
    public Integer getPurchaseDetailsByGoodsId(Integer id) {
        return purchaseMapper.getPurchaseDetailsByGoodsId(id,AppUtil.getUser().getDept().getCompanyCode());
    }

    @Override
    public List<PurchaseDetails> getPurchaseDetailsById(Integer id) {
        return purchaseMapper.getPurchaseDetailsById(id);
    }

    @Override
    public void editPurchaseDetailsByParam(Map map) {
        //id,warehouseId
        map.put("updateTime",new Date());
        map.put("updateUserId",AppUtil.getUser().getId());
        purchaseMapper.editPurchaseDetailsByParam(map);
    }

    @Override
    public void editWarehouseByPurchaseId(Integer id) {
        purchaseMapper.editWarehouseByPurchaseId(id);
    }

    @Override
    public void delPurchase(Integer id) {
        try {
            Purchase entity = purchaseMapper.getById(id);
            if(ObjectUtils.isEmpty(entity)){
                throw new QinFeiException(1002,"此产品信息已被删除，请刷新一下页面");
            }
            //编辑物品明细数据
            purchaseMapper.editPurchaseDetailsState(id);
            //删除采购记录
            purchaseMapper.delPurchase(id);
            //处理待办
            finishItem(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，删除物品采购信息出错啦，请联系技术人员");
        }
    }

    @Override
    public void editPurchaseState(Integer state,String rejectReason, Integer id) {
        purchaseMapper.editPurchaseState(state,rejectReason,id);
    }

    //进行物品明细数据的录入
    private void insertPurchaseDetailsData(Purchase purchase,List<Integer> type, List<Integer> goodsId, List<String> specs,List<String> unit,
                                           List<Integer> amount, List<Double> price, List<Double> totalMoney,List<Integer> supplierId,List<Integer> payMethod){
        try {
            if(type!=null && type.size()>0){
                List<PurchaseDetails> details = new ArrayList<>();
                for(int i =0 ;i<type.size();i++){
                    PurchaseDetails entity = new PurchaseDetails();
                    entity.setPurchaseId(purchase.getId());
                    entity.setCreateId(purchase.getUserId());
                    entity.setCreateTime(purchase.getCreateTime());
                    entity.setType(type.get(i));
                    entity.setGoodsId(goodsId.get(i));
                    entity.setState(0);
                    entity.setAmount(amount.get(i));
                    entity.setPrice(price.get(i));
                    entity.setTotalMoney(totalMoney.get(i));
                    entity.setSupplierId(supplierId.get(i));
                    entity.setPayMethod(payMethod.get(i));
                    details.add(entity);
                }
                purchaseMapper.addPurchaseDetailsBatch(details);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，新增物品采购明细出错啦，请联系技术人员");
        }
    }

    @Override
    public Integer getPurchaseBySupplierId(Integer supplierId) {
        return purchaseMapper.getCountBySupplierId(supplierId);
    }

    @Override
    public List<Map> exportPurchaseDetail(Map map, OutputStream outputStream) {
        List<Map> list=null;
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
//            map.put("userId",user.getId());
            map.put("companyCode",user.getCompanyCode());
            list = purchaseMapper.getPurchasePage(map);
            String[] heads = {"采购编号", "标题", "采购员", "采购日期", "采购金额", "创建人", "创建时间","采购状态"};
            String[] fields = {"purchaseCode", "title", "buyerName", "buyTime", "money", "userName", "createTime","state"};
            ExcelUtil.exportExcel("物品采购信息", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (value != null) {
                    if("state".equals(field)){
                        String stateTips = "";
                        Integer state=Integer.valueOf(value.toString());
                        switch (state) {
                            case -5 :
                                stateTips = "已失效";
                                break;
                            case -1 :
                                stateTips = "审核驳回";
                                break;
                            case 0 :
                                stateTips = "已保存";
                                break;
                            case 1 :
                                stateTips = "已完成";
                                break;
                            case 2 :
                                stateTips = "审核通过";
                                break;
                            case 4 :
                                stateTips = "部长审核";
                                break;
                            case 6 :
                                stateTips = "入库暂存";
                                break;
                            case 7 :
                                stateTips = "已入库";
                                break;
                            case 14 :
                                stateTips = "行政部长审核";
                                break;
                            default :
                                break;
                        }
                        cell.setCellValue(stateTips);
                    }else{
                        cell.setCellValue(value.toString());
                    }
                }
            });
        }catch (QinFeiException e){
           throw e;
        }catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，物品采购导出出错啦，请联系技术人员");
        }
        return list;
    }

    @Override
    public void processPurchase(Purchase purchase) {
        try {
            purchaseMapper.update(purchase);
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，物品采购的流程更新状态出错啦，请联系技术人员");
        }
    }

    /**
     * 处理待办
     * @param entity
     */
    private void finishItem(Purchase entity){
        if(entity.getItemId()!=null){
            Items items = new Items();
            items.setId(entity.getItemId());
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }
    }

    public String getPurchaseCode(){
        return IConst.PURCHASE_CODE+ DateUtils.format(new Date(),"yyyyMMdd")+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.PURCHASE_CODE),5);
    }

    @Override
    public PageInfo<Map> orderList(Map<String,Object>map,Pageable pageable){
        map.put("companyCode",AppUtil.getUser().getDept().getCompanyCode());
        List<Map> orderList= purchaseMapper.orderList(map);
        return  new PageInfo(orderList);
    }

    @Override
    public PageInfo<Map> orderList2(Map<String, Object> map, Pageable pageable) {
        map.put("companyCode",AppUtil.getUser().getDept().getCompanyCode());
        List<Map> orderList= purchaseMapper.orderList2(map);
        return  new PageInfo(orderList);
    }

    /**
     * 导入模板
     * @param outputStream
     */
    @Override
    public void getDataImportTemplate(OutputStream outputStream) {
        List<Map<String,Object>> sheetInfo = new ArrayList<>();
        Map<String,Object> purchaseSheet = new HashMap<>();
        List<String> purchaseRowTitles = new ArrayList<>();
        purchaseRowTitles.addAll(purchaseTitle);
        purchaseSheet.put("templateName","物品采购信息导入");
        purchaseSheet.put("rowTitles",purchaseRowTitles);
        purchaseSheet.put("notices",notices);
        Map<String,Object> purchaseDetailSheet = new HashMap<>();
        List<String> purchaseDetailRowTitles = new ArrayList<>();
        purchaseDetailRowTitles.addAll(purchaseDetailTitle);
        purchaseDetailSheet.put("templateName","物品采购明细信息导入");
        purchaseDetailSheet.put("rowTitles",purchaseDetailRowTitles);
        purchaseDetailSheet.put("notices",notices2);
        sheetInfo.add(purchaseSheet);
        sheetInfo.add(purchaseDetailSheet);
        DataImportUtil.createMoreSheetFile(sheetInfo,outputStream);
    }

    /**
     * 处理采购的数据
     * @param fileName
     * @return
     */
    @Override
    public String importPurchaseData(String fileName) {
        File file = new File(fileName);
        String result = null;
        if(!file.exists()){
           file.mkdirs();
        }
        if(file.exists()){
            Map<String,Object> purchaseResult = handlePurchaseData(file,0);
            Map<String,Object> purchaseDetailResult = handlePurchaseData(file,1);
            List<Map<String, Object>> sheetInfo = new ArrayList<>();
            //打印出错的信息，如果采购信息无误正常导入
            if (purchaseResult != null && purchaseResult.size() > 0) {
                sheetInfo.add(purchaseResult);
            }
            if (purchaseDetailResult != null && purchaseDetailResult.size() > 0) {
                sheetInfo.add(purchaseDetailResult);
            }
            if (sheetInfo.size() > 0) {
                result = DataImportUtil.createMoreSheetFile("物品采购导入失败内容", sheetInfo, config.getUploadDir(), config.getWebDir());
            }
            if(result!=null){
                throw new QinFeiException(1003,result);//如果有内容错误，则抛出异常，事务失效，防止一次导入多个sheet表时，前面sheet数据正常而导致数据入库
            }
            return result;
        }else {
            throw new QinFeiException(1002,"导入文件不存在");
        }
    }

    /**
     * 处理导入数据
     * @param file
     * @return
     */
    private Map<String,Object> handlePurchaseData(File file,Integer type){
        List<Object[]> execlContent = null;
        Map<String,Object> handResult = null;
        if(type==0){
            //物品采购信息
            execlContent = EasyExcelUtil.getExcelContent(file,type+1,3,2);
            if(CollectionUtils.isNotEmpty(execlContent)){
                handResult = dealPurchaseData(AppUtil.getUser(),execlContent);
            }
        }else{
            //物品明细信息
            execlContent = EasyExcelUtil.getExcelContent(file,type+1,3,2);
            if(CollectionUtils.isNotEmpty(execlContent)){
                handResult = dealPurchaseDetailData(AppUtil.getUser(),execlContent);
            }
        }
        return handResult;
    }

    /**
     * 处理物品采购数据
     * @param user
     * @param excelContent
     * @return
     */
    private Map<String,Object> dealPurchaseData(User user,List<Object[]> excelContent){
        List<String> rowTitles = new ArrayList<>();//导入模板总列数
        rowTitles.addAll(purchaseTitle);
        int totalColumNum = rowTitles.size();//总列数
        int rowNum = excelContent.size();//导入物品采购行数
        List<Object[]> validErrorData = new ArrayList<>(); //用于保存校验未通过的数据；
        //定义集合保存数据
        List<Purchase> purchases = new ArrayList<>();
        //去除重复的采购记录
        Set<Purchase> purchaseSet = new HashSet<>();
        Map<String,Object> errorMap = new HashMap<>();
        Object [] row = null;
        Boolean flag = true;
        for (int j = 0;j<rowNum;j++){//遍历行数据
            row = excelContent.get(j);
            if(row.length<=1){
                continue;//如果行数据仅有一列，则为Excel文件中说明信息，直接处理下一行数据
            }
            if(row.length!=totalColumNum){//如果行数据与采购模板列个数不一致，直接判断下一个
                row = Arrays.copyOf(row,row.length+1);
                row[row.length-1] = "模板格式不对应,请核实模板是否一致";
               validErrorData.add(row);
               flag = false;
               break;
            }
            boolean isValidSuccess = true; // 校验成功标志，默认校验成功
            Purchase currentPurchase = new Purchase();//保存单签的采购信息
            currentPurchase.setCompanyCode(user.getDept().getCompanyCode());
            currentPurchase.setUserId(user.getId());
            currentPurchase.setUserName(user.getName());
            currentPurchase.setState(0);
            currentPurchase.setDeptId(user.getDeptId());
            currentPurchase.setCreateTime(new Date());
            for (int i = 0;i<row.length;i++) { //Excel列数据循环
                String columValue = String.valueOf(row[i]);
                //获取列值
                boolean requiredFlag = validField(rowTitles, i);//校验字段是否必输
                if (columValue == null || "".equals(columValue) && !requiredFlag) {
                    columValue = "";
                }
                String errorInfo = null;
                if (StringUtils.isNotEmpty(columValue)) {
                    if (i == 0) { //采购编号
                        List<Purchase> list = purchaseMapper.getPurchaseByCode(user.getDept().getCompanyCode(), String.valueOf(columValue));
                        if (CollectionUtils.isNotEmpty(list)) {
                            errorInfo = "已存在";
                        } else {
                            currentPurchase.setPurchaseCode(columValue);
                        }
                    } else if (i == 1) { //采购标题
                        currentPurchase.setTitle(columValue);
                    } else if (i == 2) { //采购员
                        Map map = new HashMap();
                        map.put("name", columValue);
                        List<User> userList = userService.listByParams(map);
                        if (CollectionUtils.isNotEmpty(userList)) {
                            Integer userId = userList.get(0).getId();
                            currentPurchase.setBuyerId(userId);
                        } else {
                            errorInfo = "不存在";
                        }
                    } else if (i == 3) { //采购日期
                        try {
                            Date buyingTime;
                            if (columValue.indexOf("/") > 0) {
                                buyingTime = DateUtils.parse(columValue, "yyyy/MM/dd");
                            } else if (columValue.indexOf("-") > 0) {
                                buyingTime = DateUtils.parse(columValue, "yyyy-MM-dd");
                            } else {
//                            我们期望这个日期解析出来是：2019/10/30,而结果却是43768.什么原因呢？这个数字是什么呢？是以1900年为原点，到2019年10月30日，之间经过的天数。
//                            知道这个后，就很好处理了，我们拿到1900年的日期，在这个日期上加上43768天即可
                                Calendar calendar = new GregorianCalendar(1900, 0, -1);
                                Date d = calendar.getTime();
////                            然后，利用DateUtils的方法，加上天数（这个天数被转为了字符串，值为43768）
                                buyingTime = DateUtils.getAfterDay(d, Integer.valueOf(columValue));
                            }
                            currentPurchase.setBuyTime(buyingTime);
                        } catch (Exception e) {
                            errorInfo="格式不对";
                            e.printStackTrace();
                        }
                    } else { //备注
                        currentPurchase.setDesc(columValue);
                    }
                } else {
                    if (requiredFlag) {
                        errorInfo = "不能为空";
                    }
                }
                if (StringUtils.isNotEmpty(errorInfo)) {
                    row = Arrays.copyOf(row, row.length + 1);
                    if (rowTitles.get(i).matches("^\\*.+\\*$")) { //如果是必输字段，则去除开头和结尾的*
                        row[row.length - 1] = rowTitles.get(i).substring(1, rowTitles.get(i).length() - 1) + errorInfo;
                    } else {
                        row[row.length - 1] = rowTitles.get(i) + errorInfo;
                    }
                    validErrorData.add(row); //缓存校验未通过数据
                    isValidSuccess = false;
                    break; //直接进入下一个采购数据判断
                }
            }
            if(!isValidSuccess){//数据校验不成功继续下一个采购
                continue;
            }
            //添加采购
            purchaseSet.add(currentPurchase);
        }
        //判断是否存在有效数据
        //如果有错误，则数据不入库，提示错误信息
        int errorSize = validErrorData.size();
        if(errorSize>0){
            //导入失败原因
            rowTitles.add("失败原因");
            errorMap.put("templateName","物品采购导入失败内容");
            errorMap.put("rowTitles",rowTitles);
            errorMap.put("exportData",validErrorData);
        }
        int successSize = purchaseSet.size();
        if(successSize>0){
           purchases.addAll(purchaseSet);
           if(CollectionUtils.isNotEmpty(purchases)){
               purchaseMapper.addPurchaseBatch(purchases);
           }
        }
        if(flag==false){
            rowTitles.add("失败原因");
            errorMap.put("templateName","物品采购导入失败内容");
            errorMap.put("rowTitles",rowTitles);
            errorMap.put("exportData",validErrorData);
        }
        return errorMap;
    }

    /**
     * 处理物品明细数据
     * @param user
     * @param excelContent
     * @return
     */
    private Map<String,Object> dealPurchaseDetailData(User user,List<Object[]> excelContent){
        List<String> rowTitles = new ArrayList<>();//导入模板总列数
        rowTitles.addAll(purchaseDetailTitle);
        int totalColumNum = rowTitles.size();//总列数
        int rowNum = excelContent.size();//导入物品采购行数
        List<Object[]> validErrorData = new ArrayList<>(); // 用于保存校验未通过的数据；
        List<PurchaseDetails> purchaseDetails = new ArrayList<>();
        Map<String,Object> errorMap = new HashMap<>();
        //采购编号集合
        List<Integer> idList = new ArrayList<>();
        //去重采购编号集合
        Set<Integer> idSet = new HashSet<>();
        Object [] row = null;
        Boolean flag = true;
        int typeId=0;//产品分类id
        String typeName="";//产品分类名称
        Integer number=0;//数量
        Integer purchaseId = 0;//采购id
        for (int j = 0;j<rowNum;j++){//遍历行数据
            row = excelContent.get(j);
            if(row.length<=1){
                continue;//如果行数据仅有一列，则为Excel文件中说明信息，直接处理下一行数据
            }
            if(row.length!=totalColumNum){//如果行数据与采购模板列个数不一致，直接判断下一个
                row = Arrays.copyOf(row,row.length+1);
                row[row.length-1] = "模板格式不对应,请核实模板是否一致";
                validErrorData.add(row);
                flag = false;
                break;
            }
            boolean isValidSuccess = true; // 校验成功标志，默认校验成功
            PurchaseDetails currentPurchaseDetails = new PurchaseDetails();
            String companyCode = user.getDept().getCompanyCode();
            currentPurchaseDetails.setState(0);
            currentPurchaseDetails.setCreateId(user.getId());
            currentPurchaseDetails.setCreateTime(new Date());
            for (int i = 0;i<row.length;i++) {
                String columValue = String.valueOf(row[i]);
                //获取列值
                boolean requiredFlag = validField(rowTitles, i);//校验字段是否必输
                if (columValue == null || "".equals(columValue) && !requiredFlag) {//如果是价格字段，并且是非必输的字段，则不填默认为0
                    columValue = "";
                }
                String errorInfo = null;
                if (!Objects.isNull(columValue) && !"".equals(columValue)) {
                    if (i == 0) { //采购编号
                        List<Purchase> list = purchaseMapper.getPurchaseByCode(companyCode, columValue);
                        if (CollectionUtils.isNotEmpty(list)) {
                            //多条记录不考虑
                            purchaseId = list.get(0).getId();
                            Integer state = list.get(0).getState();
                            //state==1审核已完成
                            if(state==0){
                                currentPurchaseDetails.setPurchaseId(purchaseId);
                                idSet.add(purchaseId);
                            }else {
                                errorInfo = "该采购已在审核中，不能修改";
                            }
                        } else {
                            errorInfo = "不存在";
                        }
                    } else if (i == 1) { //产品分类
                        typeName = columValue;
                        List<GoodsType> goodsTypeList = goodsTypeService.getGoodsTypeByCondition(null, columValue, companyCode);
                        if (CollectionUtils.isNotEmpty(goodsTypeList)) {
                            typeId = goodsTypeList.get(0).getId();
                            currentPurchaseDetails.setType(typeId);
                        } else {
                            errorInfo = "不存在，请检查是否输入正确";
                        }
                    } else if (i == 2) { //产品名称
                        List<GoodsType> goodsList = goodsTypeService.getGoodsTypeByCondition(typeId,columValue,companyCode);
                        if (CollectionUtils.isNotEmpty(goodsList)) {
                            currentPurchaseDetails.setGoodsId(goodsList.get(0).getId());
                        } else {
                            errorInfo = "未找到该产品";
                        }
                    } else if (i == 3) { //数量
                        try {
                            number = Integer.valueOf(columValue);
                            currentPurchaseDetails.setAmount(number);
                        } catch (NumberFormatException e) {
                            errorInfo = "格式不对";
                            e.printStackTrace();
                        }
                    } else if (i == 4) { //单价
                        try {
                            Double price = Double.parseDouble(columValue);
                            currentPurchaseDetails.setPrice(price);
                            currentPurchaseDetails.setTotalMoney(number * price);
                        } catch (NumberFormatException e) {
                            errorInfo = "格式不对";
                            e.printStackTrace();
                        }
                    } else if (i == 5) { //供应商
                        Map obj = new HashMap();
                        obj.put("name", columValue);
                        obj.put("companyCode", companyCode);
                        List<PurchaseSupplier> purchaseSupplierList = purchaseSupplierService.getPurchaseSupplierByName(obj);
                        if (CollectionUtils.isNotEmpty(purchaseSupplierList)) {
                            Integer supplierId = purchaseSupplierList.get(0).getId();
                            currentPurchaseDetails.setSupplierId(supplierId);
                        } else {
                            errorInfo = "不存在";
                        }
                    } else if (i == 6) { //支付方式
                        if(StringUtils.isEmpty(columValue)){
                            errorInfo = "不为空";
                        }else{
                            if("微信".equals(columValue)){
                                currentPurchaseDetails.setPayMethod(0);
                            }else if("支付宝".equals(columValue)){
                                currentPurchaseDetails.setPayMethod(1);
                            }else if("银行卡".equals(columValue)){
                                currentPurchaseDetails.setPayMethod(2);
                            }else{
                                currentPurchaseDetails.setPayMethod(null);
                            }
                        }
                    }
                } else {
                    if (requiredFlag) {
                        errorInfo = "不能为空";
                    }
                }
                if (StringUtils.isNotEmpty(errorInfo)) {
                    row = Arrays.copyOf(row, row.length + 1);
                    if (rowTitles.get(i).matches("^\\*.+\\*$")) { //如果是必输字段，则去除开头和结尾的*
                        row[row.length - 1] = rowTitles.get(i).substring(1, rowTitles.get(i).length() - 1) + errorInfo;
                    } else {
                        row[row.length - 1] = rowTitles.get(i) + errorInfo;
                    }
                    validErrorData.add(row); //缓存校验未通过数据
                    isValidSuccess = false;
                    break; //直接进入下一个采购数据判断
                }
            }
            if (!isValidSuccess) {//数据校验不成功继续下一个采购
                continue;
            }
            purchaseDetails.add(currentPurchaseDetails);
        }
        //判断是否存在有效数据
        //如果有错误，则数据不入库，提示错误信息
        int errorSize = validErrorData.size();
        if(errorSize>0){
            //导入失败原因
            rowTitles.add("失败原因");
            errorMap.put("templateName","物品采购明细导入失败内容");
            errorMap.put("rowTitles",rowTitles);
            errorMap.put("exportData",validErrorData);
        }
        int successSize = purchaseDetails.size();
        if(successSize>0){
            idList.addAll(idSet);
            Map map = new HashMap();
            if(CollectionUtils.isNotEmpty(idList)){
                map.put("list",idList);
                map.put("userId",user.getId());
            }
            if(map!=null){
                purchaseMapper.delPurchaseDetailsByIds(map);
            }
            //添加明细记录
            purchaseMapper.addPurchaseDetailsBatch(purchaseDetails);
        }
        if(flag==false){
            rowTitles.add("失败原因");
            errorMap.put("templateName","物品采购明细导入失败内容");
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

    /**
     * 添加待办
     * @param userId
     * @param purchaseId
     * @return
     */
    public Items addItem(Integer userId,Integer purchaseId,String title){
        Items items = new Items();
        items.setItemName(title+"-物品采购确认抄送");
        items.setItemContent("物品采购");
        items.setWorkType("物品采购确认");
        //获取接收抄送人的信息
        User user = userService.getById2(userId);
        //发起者
        User initiator= AppUtil.getUser();
        items.setInitiatorWorker(initiator.getId());
        //发起者部门
        items.setInitiatorDept(initiator.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/inventory/purchase_list?flag=2&id=" + purchaseId);
        items.setFinishAddress("/inventory/purchase_list?flag=1&id=" + purchaseId);
        items.setAcceptWorker(user.getId());
        items.setAcceptDept(user.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        Purchase purchase = new Purchase();
        purchase.setId(purchaseId);
        purchase.setItemId(items.getId());
        purchase.setUpdateUserId(userId);
        if(purchaseId!=null){
            purchaseMapper.update(purchase);
        }
        return items;
    }

    /**
     * 财务确认抄送
     * @param itemId
     * @return
     */
    @Override
    public void confirm(Integer itemId) {
        Items items = new Items();
        items.setId(itemId);
        items.setTransactionState(Const.ITEM_Y);
        itemsService.finishItems(items);
    }

    @Override
    public List<Map> relatedReimbursement(List<String> list) {
        return purchaseMapper.relatedReimbursement(list);
    }

    @Override
    public List<Map> checkRelatedReimbursement(List<String> list) {
        return purchaseMapper.checkRelatedReimbursement(list);
    }
}
