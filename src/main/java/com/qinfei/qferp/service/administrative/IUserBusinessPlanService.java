package com.qinfei.qferp.service.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.administrative.UserBusinessConclusion;
import com.qinfei.qferp.entity.administrative.UserBusinessPlan;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IUserBusinessPlanService {
    String CACHE_KEY = "userBusinessPlan";

    void saveBusiness(UserBusinessPlan userBusinessPlan);

    ResponseData onBusiness(UserBusinessPlan userBusinessPlan, MultipartFile[] multipartFiles, MultipartFile[] pics);

    ResponseData addBusiness(UserBusinessPlan userBusinessPlan, MultipartFile[] multipartFiles,MultipartFile[] pics);

    int getConclusion(Integer admId);

    int getReimbursement(Integer admId);

    ResponseData getBussiness(Integer id);

    ResponseData updateBusiness(UserBusinessPlan userBusinessPlan, MultipartFile[] files, MultipartFile[] pics);

    ResponseData editUpdateOnBusiness( UserBusinessPlan userBusinessPlan,  MultipartFile[] files, MultipartFile[] pics);

    ResponseData deleteBussiness(Integer id);

    ResponseData viewBusiness(Integer id);

    ResponseData addConclusion(Map map,MultipartFile[] pics, MultipartFile[] attach);

    ResponseData  selectUser();

    int deleteConclusion(Integer id);

    int deleteCon(Integer adminId);

    UserBusinessConclusion selectFile(Integer id);

    Items addItem(Integer userId,Integer administrativeId );

    Items addConlusionItem(Integer userId,Integer administrativeId );

    void confirm(Integer itemId);

    void confirm1(Integer itemConclusionId);

    long selectState(String taskId);
}