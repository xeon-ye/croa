package com.qinfei.qferp.service.fee;

import com.qinfei.qferp.entity.fee.Account;
import com.qinfei.qferp.entity.sys.Dept;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface IAccountService {

    PageInfo<Map> listPg(int pageNum, int pageSize, Map map);

    Account getById(Integer id) ;

    Account add(Account entity);

    Account outgoAccountAdd(Account entity);

    Integer supplierType(Integer supplierId);

    void delById(Account entity);

    Account edit(Account entity);

    void addPersonalAccount(Account entity);

    PageInfo<Map> listPgForSelectAccount(int pageNum, int pageSize, Integer companyId, Integer type, Map map);

    PageInfo<Map> listPgForSelectAccountNotCompanyCode(int pageNum, int pageSize, Integer companyId, Integer type, Map map);

    List<Dept> insertAccountDept(Integer accountId, Integer deptId);

    @Transactional
    List<Dept> deleteAccountDept(Integer accountId, Integer deptId);

    List<Dept> queryDeptByAccountId(Integer id);

    List<Account> queryCompanyAccountList(String companyCode);
}
