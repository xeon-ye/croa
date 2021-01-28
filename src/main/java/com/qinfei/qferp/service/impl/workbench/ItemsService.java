package com.qinfei.qferp.service.impl.workbench;

import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.workbench.ItemsMapper;
import com.qinfei.qferp.service.workbench.IItemsService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemsService implements IItemsService {
    @Autowired
    ItemsMapper itemsMapper;

    /**
     * 查询事项数据
     * @param item
     * @param pageable
     * @return
     */
    public PageInfo<Map> list(Map item, Pageable pageable){
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        //接收部门为用户所在部门
        item.put("userDept", AppUtil.getUser().getDeptId());
        item.put("acceptWork", AppUtil.getUser().getId());
        List<Map> items = itemsMapper.listPg(item);
        return new PageInfo<Map>(items);
    }

    /**
     * 添加一个系统事项,返回成功还是失败
     * @param item
     * @return
     */
    public boolean addItems(Items item){
        int row = addItemsReturnId(item);
        return row > 0;
    }

    /**
     * 添加一个事项，返回ID值
     * @param item
     * @return
     */
    public int addItemsReturnId(Items item){
        item.setCreateTime(new Date());
        //待办
        item.setTransactionState(Const.ITEM_W);
        //系统流程产生的事项
        item.setItemType(Const.ITEM_TYPE_SYS);
        //紧急程度为普通
        item.setUrgencyLevel(Const.ITEM_J3);
        //事项发起人
        if(item.getInitiatorWorker() == null || item.getInitiatorWorker() == 0) {
            item.setInitiatorWorker(AppUtil.getUser().getId());
        }
        return itemsMapper.insert(item);
    }

    /**
     * 将事项设置为已办
     * @param items
     * @return
     */
    @Override
    @Transactional
    public boolean finishItems(Items items) {
        items.setTransactionState(Const.ITEM_Y);
        //事项完成时间
        items.setFinishTime(new Date());
        //事项完成人
        items.setFinishWorker(AppUtil.getUser().getId());
        itemsMapper.finishItems(items);
        return true;
    }

    @Override
    @Transactional
    public int batchFinishItems(String ids) {
        Map map = new HashMap() ;
        map.put("userId",AppUtil.getUser().getId()) ;
        map.put("transactionState",Const.ITEM_Y);
        map.put("ids",ids) ;
        return itemsMapper.batchFinishItems(map) ;
    }

    @Override
    public int queryItemStateById(Integer itemId) {
        return itemsMapper.queryItemStateById(itemId);
    }

    @Override
    public void updateItemsTransactionAddress(List<Items> list) {
        itemsMapper.updateItemsTransactionAddress(list);
        return;
    }


}
