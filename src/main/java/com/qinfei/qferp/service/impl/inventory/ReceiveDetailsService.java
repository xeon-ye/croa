package com.qinfei.qferp.service.impl.inventory;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.inventory.ReceiveDetails;
import com.qinfei.qferp.mapper.inventory.ReceiveDetailsMapper;
import com.qinfei.qferp.service.inventory.IReceiveDetailsService;
import com.qinfei.qferp.utils.AppUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 物品领用接口实现类
 * @author tsf
 */
@Service
public class ReceiveDetailsService implements IReceiveDetailsService {
    @Autowired
    private ReceiveDetailsMapper detailsMapper;

    @Override
    public List<ReceiveDetails> getReceiveDetailById(Integer id,String companyCode) {
        return detailsMapper.getReceiveDetailById(id);
    }

    @Override
    public List<ReceiveDetails> getStockDetailByWareId(Map map) {
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        return detailsMapper.getStockDetailByWareId(map);
    }

    @Override
    public ReceiveDetails getById(Integer id) {
        return detailsMapper.getById(id);
    }

    @Override
    public void saveReceiveDetailsBatch(List<ReceiveDetails> details) {
        try {
            if(CollectionUtils.isNotEmpty(details)){
                detailsMapper.saveReceiveDetailsBatch(details);
            }else{
                throw new QinFeiException(1002, "物品领用明细不能为空");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，批量新增物品领用明细出错啦");
        }
    }

    @Override
    public void editReceiveDetailsByParam(Map map) {
        map.put("updateTime",new Date());
        map.put("updateUserId", AppUtil.getUser().getId());
        detailsMapper.editReceiveDetailsByParam(map);
    }

    @Override
    public void delReceiveDetails(Integer id) {
       detailsMapper.delReceiveDetails(id);
    }

    @Override
    public void deleteReceiveDetails(Integer id) {
        detailsMapper.deleteReceiveDetails(id);
    }
}
