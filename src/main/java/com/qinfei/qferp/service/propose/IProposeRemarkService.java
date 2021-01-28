package com.qinfei.qferp.service.propose;

import com.qinfei.qferp.entity.propose.ProposeRemark;

import java.util.List;

/**
 * 建议意见接口
 * @Author ：tsf;
 */
public interface IProposeRemarkService {
    void saveProposeRemark(ProposeRemark proposeRemark);

    List<ProposeRemark> queryProposeRemark(Integer id,String sort);
}
