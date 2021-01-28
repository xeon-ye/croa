package com.qinfei.qferp.service.impl.fee;

import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.fee.Borrow;
import com.qinfei.qferp.entity.fee.BorrowRepay;
import com.qinfei.qferp.entity.fee.Drop;
import com.qinfei.qferp.entity.fee.Outgo;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.fee.BorrowMapper;
import com.qinfei.qferp.mapper.fee.BorrowRepayMapper;
import com.qinfei.qferp.mapper.fee.DropMapper;
import com.qinfei.qferp.service.fee.IDropService;
import com.qinfei.qferp.service.impl.flow.ProcessService;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.service.impl.workbench.ItemsService;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.*;

@Service
public class DropService implements IDropService {
    @Autowired
    private DropMapper dropMapper ;
    @Autowired
    private ItemsService itemsService ;
    @Autowired
    private ProcessService processService ;
    @Autowired
    private UserService userService ;
    @Autowired
    private BorrowService borrowService ;
    @Autowired
    private BorrowMapper borrowMapper ;
    @Autowired
    private BorrowRepayMapper borrowRepayMapper ;

    @Override
    public PageInfo<Map> listPg(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        if(map.get("deptId") != null){//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        List<Map> list = dropMapper.listPg(map);
        for (Map data : list) {
            if(data.get("phone") != null && !org.springframework.util.StringUtils.isEmpty(String.valueOf(data.get("phone")))){
                String phone = EncryptUtils.decrypt(String.valueOf(data.get("phone")));
                data.put("phone", phone);
            }
        }
//        Integer userId = AppUtil.getUser().getId();
//        for (Map tmp : list) {
//            String taskId = String.valueOf(tmp.get("taskId"));
//            FindFinanceTaskOwnerCommand findTaskOwner = new FindFinanceTaskOwnerCommand(taskId);
//            managementService.executeCommand(findTa·skOwner);
//            if (StringUtils.equalsIgnoreCase(findTaskOwner.getUserId(), String.valueOf(userId)))
//                tmp.put("isOwner", true);
//            else
//                tmp.put("isOwner", false);
//        }
        return new PageInfo<>(list);
    }

    @Override
    public Drop getById(Integer id) {
        return dropMapper.getById(id);
    }

    @Override
    public Drop add(Drop entity) {
        dropMapper.insert(entity);
        return entity;
    }
    @Override
    @Transactional
    public Drop edit(Drop entity) {
        User user = AppUtil.getUser();
        entity.setUpdateUserId(user.getId());
        if(entity.getPayAmount()>0 && entity.getType() == IConst.DROP_FUND_FLAG_FALSE){
            entity.setType(IConst.DROP_FUND_FLAG_TRUE);
            List<Borrow> borrowList = borrowMapper.queryByOutgoId(entity.getAccountId()) ;
            //payAmount退还的备用金，
            if(borrowList!=null && borrowList.size()>0){
                Borrow borrow = borrowList.get(0) ;
                borrowService.backBorrowInfo(borrow,entity.getPayAmount());
                insertBorrowRepay(entity.getId(),borrow.getId(),entity.getPayAmount());
            }
        }
        //请款暂存
        if(entity.getState()==0){
            dropMapper.update(entity);
         //请款提交审核
        }else{
            dropMapper.update(entity);
            //待办变已办
            finishItem(entity) ;
            // 紧急程度字段暂不启用
            // taskId为空：首次提交审核；不为空：驳回后提交审核
            processService.addManuscriptProcess(entity, 3);
        }
        finishItem(entity);
        return entity;
    }
    private void insertBorrowRepay(Integer dropId,Integer borrowId,Double fundAmount){
        User user = AppUtil.getUser() ;
        Drop entity = dropMapper.getById(dropId) ;
        BorrowRepay repay = new BorrowRepay() ;
        repay.setBorrowId(borrowId);
        repay.setRepayId(entity.getId());
        repay.setRepayCode(entity.getCode());
        repay.setType(IConst.REPAY_TYPE_DROP);
        repay.setAmount(0-fundAmount);//退稿还备用金，值是负的
        repay.setState(IConst.REPAY_STATE_FINISH);
        repay.setCreateTime(new Date());
        repay.setCreateUserId(user.getId());
        repay.setCreateName(user.getName());
        borrowRepayMapper.insert(repay) ;
    }
    @Override
    public Drop update(Drop entity) {
        dropMapper.update(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delById(Drop entity) {
        if(entity.getType() == IConst.DROP_FUND_FLAG_TRUE){
            List<Borrow> borrowList = borrowMapper.queryByOutgoId(entity.getAccountId()) ;
            if(borrowList!=null && borrowList.size()>0){
                Borrow borrow = borrowList.get(0) ;
                borrowService.dealBorrowInfo(borrow,entity.getPayAmount());
                //删除旧的还款关系表
                borrowRepayMapper.deleteByRepayIdAndTypeAndState(entity.getId(),IConst.REPAY_TYPE_DROP,IConst.REPAY_STATE_FINISH);
            }
        }
        User user = AppUtil.getUser() ;
        List<Integer> list = dropMapper.queryArticleIdsByDropId(entity.getId()) ;
        Map<String,Object> map = new HashMap<>() ;
        map.put("list",list) ;
        map.put("state",1) ;
        map.put("userId",user.getId()) ;
        //还原稿件请款字段
        dropMapper.changeDropArticleBatch(map);
        //更新利润
//        articleMapperXML.updateProfitInfo(map);
        //删除关系表
        dropMapper.delDropArticle(entity.getId());
        //删除请款
        entity.setState(IConst.STATE_DELETE);
        entity.setUpdateUserId(user.getId());
        dropMapper.update(entity);
        finishItem(entity) ;
    }

    private void finishItem(Drop entity){
        if(entity.getItemId()!=null){
            Items items = new Items();
            items.setId(entity.getItemId());
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }
    }
    /**
     * 请款选定供应商和稿件后先存数据
     * @param map
     * @return
     */
    @Override
    @Transactional
    public Drop saveStepOne(Map map,User user,Outgo outgo) {
        Integer supplierId = Integer.parseInt((String)map.get("supplierIdSec")) ;
        String supplierName = (String)map.get("supplierNameSec");
        String supplierContactor = (String)map.get("supplierContactorSec");
        String articleIds = (String)map.get("articleIdsSec");
        String companyCode = (String)map.get("companyCode");
        String mediaTypeId= (String)map.get("mediaTypeId");

        Drop entity = new Drop() ;
        entity.setSupplierId(supplierId);
        entity.setSupplierName(supplierName);
        entity.setSupplierContactor(supplierContactor);
        entity.setApplyId(user.getId());
        entity.setApplyName(user.getName());
        entity.setApplyTime(new Date());
        entity.setDeptId(user.getDeptId());
        entity.setDeptName(user.getDeptName()) ;
        entity.setCreator(user.getId());
        entity.setCreateTime(new Date());

        List<String> plateCode= SysConfigUtils.getConfigValue("plateCode",List.class);
        if (CollectionUtils.isNotEmpty(plateCode) &&plateCode.contains(mediaTypeId)){
            entity.setCompanyCode(user.getCompanyCode());
        }else {
            entity.setCompanyCode(companyCode);
        }
        entity.setAccountId(outgo.getId());
        entity.setApplyAmount(outgo.getApplyAmount());
        entity.setFundAmount(outgo.getFundAmount());

        //生成KP2018110001的编号，前六位是年月，后四位累加
        entity.setCode(IConst.DROP_CODE+ CodeUtil.getMonthStr()+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.DROP_CODE),4)) ;
        dropMapper.insert(entity) ;
        //对稿件进行删除操作
        insertDropArticle(entity.getId(),articleIds) ;
        return entity ;
    }

    //维护关系
    private void insertDropArticle(Integer DropId, String articleIds) {
        if(articleIds.indexOf(",")>-1){
            String[] ids = articleIds.split(",") ;
            //放入set排重
            Set<Integer> set = new HashSet();
            for(int i=0;i<ids.length;i++){
                set.add(Integer.parseInt(ids[i]));
            }
            Iterator<Integer> iterator =set.iterator() ;
            //插入关系表数据
            while(iterator.hasNext()){
                Integer articleId =  iterator.next() ;
                dropMapper.insertDropArticle(DropId,articleId);
            }
            //稿件状态变成-9
            List<Integer> list = new ArrayList<>(set);
            Map<String,Object> map = new HashMap<>() ;
            map.put("list",list) ;
            map.put("state",IConst.STATE_DELETE) ;
            map.put("userId",AppUtil.getUser().getId()) ;
            dropMapper.changeDropArticleBatch(map);
        }else{
            Integer id = Integer.parseInt(articleIds) ;
            //插入关系表数据
            dropMapper.insertDropArticle(DropId,id);
            //稿件状态变成-9
            List<Integer> list = new ArrayList<>() ;
            list.add(id) ;
            Map<String,Object> map = new HashMap<>() ;
            map.put("list",list) ;
            map.put("state",IConst.STATE_DELETE) ;
            map.put("userId",AppUtil.getUser().getId()) ;
            dropMapper.changeDropArticleBatch(map);
        }
    }
    //选中的稿件
    @Override
    public PageInfo<Map> listPgForSelectedArticle(int pageNum, int pageSize, Integer id) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = dropMapper.listPgForSelectedArticle(id);
        return new PageInfo<>(list);
    }

    //待请款稿件
    @Override
    public PageInfo<Map> listPgForSelectArticle(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = dropMapper.listPgForSelectArticle(map);
        return new PageInfo<>(list);
    }

    /**
     * 算出当前借款订单的申请金额
     * @param id
     * @return
     */
    @Override
    public Map querySumAmount(Integer id ){
        return dropMapper.querySumAmount(id);
    }
    /**
     * 根据稿件编号查询请款id
     * @param articleId
     * @return
     */
    @Override
    public Integer queryDropId(Integer articleId){
        Integer outId = dropMapper.queryDropId(articleId);
        return outId!=null?outId:-2;
    }

    //增加待办
    public Items addItem(Drop entity,User user){
        Items items = new Items();
        items.setItemName(entity.getTitle()+"-请款出账确认等待处理");
        items.setItemContent("您有新的请款出账确认需要处理");
        items.setWorkType("出账确认申请");
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/fee/queryDrop?flag=2&id="+entity.getId());
        items.setFinishAddress("/fee/queryDrop?flag=2&id="+entity.getId());
        User cw = userService.getCWBZInfo(user.getCompanyCode()) ;
        items.setAcceptWorker(cw.getId()) ;
        items.setAcceptDept(cw.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items ;
    }

    //增加待办
    public Items addApplyItem(Drop entity,User user){
        Items items = new Items();
        items.setItemName(entity.getTitle()+"-请款撤回等待处理");
        items.setItemContent("您有新的请款撤回需要处理");
        items.setWorkType("请款撤回");
        User mediaUser = userService.getById(entity.getApplyId()) ;
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/fee/queryDrop?flag=-1&id="+entity.getId());
        items.setFinishAddress("/fee/queryDrop?flag=-1&id="+entity.getId());
        items.setAcceptWorker(mediaUser.getId()) ;
        items.setAcceptDept(mediaUser.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items ;
    }


    @Override
    public List<Map> exportDrop(Map map, OutputStream outputStream) {
        if(map.get("deptId") != null){//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        List<Map> list = dropMapper.listPg(map);
        String[] heads = {"请款编号","请款人","所在部门","请款日期","请款标题","供应商名称","支付户主",
                "支付账户", "支付开户行","请款金额","期望付款日期","出款账户","出款金额","出款日期"};
        String[] fields = {"code","applyName","deptName","applyTime", "title","supplierName",
                "accountName","accountBankNo", "accountBankName","applyAmount","expertPayTime",
                "outAccountName","payAmount","payTime"};
        ExcelUtil.exportExcel("进账列表", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if ("applyAmount".equals(field)||"payAmount".equals(field)) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                }else if ("applyTime".equals(field)||"expertPayTime".equals(field)||"payTime".equals(field)) {
                    cell.setCellValue(value.toString());
//                    cell.setCellValue(DateUtils.format((Date) value, DateUtils.DEFAULT));
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        });
        return list;
    }

    @Override
    public List<Outgo> queryOutgoByArticleIds(List articleIds){
        return dropMapper.queryOutgoByArticleIds(articleIds) ;
    }
}
