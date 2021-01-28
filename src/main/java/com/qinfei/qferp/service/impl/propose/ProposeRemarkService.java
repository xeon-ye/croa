package com.qinfei.qferp.service.impl.propose;
import com.qinfei.qferp.entity.propose.ProposeRemark;
import com.qinfei.qferp.mapper.propose.ProposeRemarkMapper;
import com.qinfei.qferp.service.propose.IProposeRemarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


/**
 * 建议意见接口实现类
 * @author tsf
 */
@Service
public class ProposeRemarkService implements IProposeRemarkService {

    @Autowired
    ProposeRemarkMapper proposeRemarkMapper;

    @Override
    public void saveProposeRemark(ProposeRemark proposeRemark) {
        proposeRemarkMapper.saveProposeRemark(proposeRemark);
    }

    @Override
    public List<ProposeRemark> queryProposeRemark(Integer id,String sort) {
        return proposeRemarkMapper.queryProposeRemark(id,sort);
    }
}
