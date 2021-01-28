package com.qinfei.qferp.service.impl.inventory;


import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.inventory.ReceiveApply;
import com.qinfei.qferp.entity.inventory.ReceiveDetails;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.inventory.GoodsMapper;
import com.qinfei.qferp.mapper.inventory.ReceiveApplyMapper;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.fee.FeeCodeUtil;
import com.qinfei.qferp.service.inventory.IApplyService;
import com.qinfei.qferp.service.inventory.IReceiveDetailsService;
import com.qinfei.qferp.service.workbench.IItemsService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.flowable.spring.boot.app.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * 物品领用接口实现类
 * @author tsf
 */
@Service
public class ApplyService implements IApplyService {
    @Autowired
    private ReceiveApplyMapper applyMapper;
    @Autowired
    private IItemsService itemsService;
    @Autowired
    private IReceiveDetailsService detailsService;
    @Autowired
    private IProcessService processService;
    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public ReceiveApply getById(Integer id) {
        User user = AppUtil.getUser();
        String companyCode = user.getDept().getCompanyCode();
        List<ReceiveDetails> details = detailsService.getReceiveDetailById(id,companyCode);
        ReceiveApply apply = applyMapper.getById(id);
        if(CollectionUtils.isNotEmpty(details)){
            apply.setDetails(details);
        }
        return apply;
    }

    @Override
    public ReceiveApply getByWareIdAndApplyId(Integer id){
        User user = AppUtil.getUser();
        Map map=new HashMap();
        ReceiveApply apply = applyMapper.getById(id);
        map.put("id",id);
        map.put("wareId",apply.getWareId());
        List<ReceiveDetails> details = detailsService.getStockDetailByWareId(map);
        if(CollectionUtils.isNotEmpty(details)){
            apply.setDetails(details);
        }
        return apply;
    }

    @Override
    public Integer getApplyCount(Map map) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            map.put("userId",user.getId());
            map.put("companyCode",user.getCompanyCode());
            return applyMapper.getApplyCount(map);
        }catch (QinFeiException e){
            throw e;
        } catch (Exception e) {
            throw new QinFeiException(1002,"抱歉，获取物品领用分页数量出错啦，请联系技术人员");
        }
    }

    @Override
    public PageInfo<Map> listPg(Map map, Pageable pageable) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            map.put("userId",user.getId());
            map.put("companyCode",user.getCompanyCode());
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            List<Map> list = applyMapper.listPg(map);
            PageInfo<Map> pageInfo = new PageInfo<>(list);
            return pageInfo;
        }catch (QinFeiException e){
            throw e;
        } catch (Exception e) {
            throw new QinFeiException(1002,"抱歉，获取物品领用分页信息出错啦，请联系技术人员");
        }
    }

    @Override
    public Integer getUserApplyCount(Map map) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            map.put("userId",user.getId());
            return goodsMapper.getUserApplyCount(map);
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，获取物品使用分页数量出错啦，请联系技术人员");
        }
    }

    @Override
    public PageInfo<Map> getUserApplyData(Map map, Pageable pageable) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            map.put("userId",user.getId());
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            List<Map> list = goodsMapper.getUserApplyData(map);
            PageInfo<Map> pageInfo = new PageInfo<>(list);
            return pageInfo;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，获取物品使用分页数据出错啦，请联系技术人员");
        }
    }

    @Override
    public String getApplyCode() {
        return IConst.APPLY_CODE+ DateUtils.format(new Date(),"yyyyMMdd")+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.APPLY_CODE),5);
    }

    @Override
    public ReceiveApply saveApply(ReceiveApply apply, List<Integer> type, List<Integer> goodsId, List<String> unit, List<Integer> amount,List<Double> price,List<Double> totalMoney, List<Integer> handleId, List<Date> returnDate) {
        try {
            //step1:处理主表信息
            User user = AppUtil.getUser();
            apply.setCompanyCode(user.getDept().getCompanyCode());
            apply.setId(null);
            apply.setCreateTime(new Date());
            applyMapper.saveApply(apply);
            if(apply.getState()>0){
                //step2:发起流程
                processService.addApplyProcess(apply,3);
            }
            //step3:进行物品领用明细数据的录入
            addApplyDetailsBatch(apply,type,goodsId,unit,amount,price,totalMoney,handleId,returnDate);
            return apply;
        }catch (QinFeiException e){
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，新增物品领用信息出错啦，请联系技术人员");
        }
    }

    @Override
    public ReceiveApply editApply(ReceiveApply apply, List<Integer> type, List<Integer> goodsId, List<String> unit, List<Integer> amount,List<Double> price,List<Double> totalMoney, List<Integer> handleId, List<Date> returnDate) {
        try {
            User user = AppUtil.getUser();
            apply.setCompanyCode(user.getDept().getCompanyCode());
            apply.setUpdateTime(new Date());
            apply.setUpdateUserId(user.getId());
            applyMapper.editApply(apply);
            //处理待办
            finishItem(apply);
            if(apply.getState()>0){
                //发起流程
                processService.addApplyProcess(apply,3);
            }
            //step2:处理物品领用明细表
            detailsService.deleteReceiveDetails(apply.getId());
            addApplyDetailsBatch(apply,type,goodsId,unit,amount,price,totalMoney,handleId,returnDate);
            return apply;
        }catch (QinFeiException e){
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，编辑物品领用信息出错啦，请联系技术人员");
        }
    }

    /**
     * 物品领用流程更新状态
     * @param apply
     * @return
     */
    @Override
    public void processApply(ReceiveApply apply) {
        applyMapper.editApply(apply);
    }

    @Override
    public PageInfo<Map> orderList(Map map) {
        map.put("companyCode", AppUtil.getUser().getCompanyCode());
        List<Map> list = applyMapper.orderList(map);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Map> orderList2(Map map) {
        map.put("companyCode", AppUtil.getUser().getCompanyCode());
        List<Map> list = applyMapper.orderList2(map);
        return new PageInfo<>(list);
    }

    @Override
    public void delApply(Integer id) {
        try {
            ReceiveApply entity = applyMapper.getById(id);
            if(ObjectUtils.isEmpty(entity)){
                throw new QinFeiException(1002,"此物品领用信息已被删除，请刷新一下页面");
            }
            detailsService.delReceiveDetails(id);
            applyMapper.delApply(id);
            finishItem(entity);
        }catch (QinFeiException e){
            throw e;
        } catch (Exception e) {
            throw new QinFeiException(1002,"抱歉，删除物品领用信息出错啦，请联系技术人员");
        }
    }

    //批量添加领用明细
    private void addApplyDetailsBatch(ReceiveApply apply, List<Integer> type, List<Integer> goodsId, List<String> unit, List<Integer> amount,List<Double> price,List<Double> totalMoney, List<Integer> handleId, List<Date> returnDate){
        User user = AppUtil.getUser();
        List<ReceiveDetails> list = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(type)){
            for (int i=0;i<type.size();i++){
                ReceiveDetails details = new ReceiveDetails();
                details.setApplyId(apply.getId());
                details.setType(type.get(i));
                details.setGoodsId(goodsId.get(i));
                details.setState(0);
                details.setUnit(unit.get(i));
                details.setAmount(amount.get(i));
                details.setPrice(price.get(i));
                details.setTotalMoney(totalMoney.get(i));
                details.setUserId(handleId.get(i));
                details.setUpdateUserId(user.getId());
                details.setCreateTime(apply.getCreateTime());
                details.setUpdateTime(new Date());
                if(CollectionUtils.isNotEmpty(returnDate)){
                    details.setReturnTime(returnDate.get(i));
                }else{
                    details.setReturnTime(null);
                }
                details.setCompanyCode(user.getDept().getCompanyCode());
                list.add(details);
            }
            try {
                detailsService.saveReceiveDetailsBatch(list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

     //处理待办
    private void finishItem(ReceiveApply entity){
        if(entity.getItemId()!=null){
            Items items = new Items();
            items.setId(entity.getItemId());
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }
    }
}
