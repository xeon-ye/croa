package com.qinfei.qferp.mapper.administrative;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.administrative.UserBusinessConclusion;
import com.qinfei.qferp.entity.administrative.UserBusinessPlan;
import com.qinfei.qferp.entity.sys.User;
import org.apache.ibatis.annotations.Insert;

import java.util.List;
import java.util.Map;

public interface UserBusinessPlanMapper  extends BaseMapper<UserBusinessPlan, Integer> {




    int insertBusiness(UserBusinessPlan userBusinessPlan);

    UserBusinessPlan getById(Integer id);

    UserBusinessConclusion getConclusion(Integer id);

    Integer getConclusion1(Integer id);

    Integer getReimbursement(Integer id);

    int updateBusiness(UserBusinessPlan userBusinessPlan);

    int deleteBussiness(Integer id);


    Integer selectTask(Integer id);

    int addConclusion(Map map);

    List<String> selectUserDirector(Map map);

    String zj(Map map);

    int deleteConclusion(Integer id);

    int deleteCon(Integer adminId);

    String selectConclusion(Integer id);

    Integer selectItem(Integer id);

    UserBusinessConclusion selectFile(Integer id);

    User getUser(Integer userId);

    int insertItemId(Map map);

    int insertConclusionItemId(Map map);

    int updateState(Integer id);

    Object selectState(String  taskId);











}
