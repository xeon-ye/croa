package com.qinfei.qferp.service.accountsMess;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.accountsMess.AccountsMess;
import com.github.pagehelper.PageInfo;

import org.springframework.data.domain.Pageable;
import java.util.Map;

public interface IAccountsMessService {
    Map<String,Object> accountsMessList(Map<String,Object> map);

    Map<String,Object> dockingListTable(Map<String,Object> map);

    ResponseData saveMessArticle(Map<String,Object> map);

    Map<String,Object> accountMessListTable(Map<String,Object> map);

    ResponseData selectMessDetails(Integer id);

    void updateMess(AccountsMess accountsMess);

    ResponseData addMessList(AccountsMess accountsMess);

    ResponseData deletMess(Integer id);

    ResponseData selectMessId(Integer id);



}
