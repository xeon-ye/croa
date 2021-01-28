package com.qinfei.qferp.mapper.propose;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.propose.ProposeRemark;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProposeRemarkMapper extends BaseMapper<ProposeRemark,Integer> {
    void saveProposeRemark(ProposeRemark proposeRemark);

    List<ProposeRemark> queryProposeRemark(@Param("id") Integer id, @Param("sort") String sort);
}
