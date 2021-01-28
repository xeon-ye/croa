package com.qinfei.qferp.service.workbench;

import com.qinfei.qferp.entity.workbench.Items;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface IItemsService {
    PageInfo<Map> list(Map item, Pageable pageable);
    //添加一个事项
    boolean addItems(Items item);
    //返回ID
    int addItemsReturnId(Items item);
    //将事项设为已办
    boolean finishItems(Items items);
    //根据待办id查询指定的待办状态
    int queryItemStateById(Integer itemId);

    @Transactional
    int batchFinishItems(String ids);

    void updateItemsTransactionAddress(List<Items> list);
}
