package com.qinfei.qferp.service.outapi;

import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.crm.CrmSearchCache;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

/**
 * @CalssName: ICompanyService
 * @Description: 企查查接口服务
 * @Author: Xuxiong
 * @Date: 2020/6/5 0005 14:53
 * @Version: 1.0
 */
public interface ICompanyService {
    //分页查询公司（第一页查询缓存表、第二页开始调用企查查接口）
    PageInfo<CrmSearchCache> companySearch(String keyword, Pageable pageable);

    //校验公司，不返回信息（优先查询缓存表，没查到调用企查查接口）
    ResponseData checkCompany(String keyword);

    //校验公司，返回公司信息（直接查询企查查接口，该该方法用于标准公司改名时，需要对比公司曾用名，曾用名包含老名字时即可替换）
    CrmSearchCache checkCompanyByApi(String keyword);
}
