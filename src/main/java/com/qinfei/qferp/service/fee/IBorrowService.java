package com.qinfei.qferp.service.fee;

import com.qinfei.qferp.entity.fee.Borrow;
import com.qinfei.qferp.entity.fee.BorrowRepay;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IBorrowService {


    PageInfo<Map> listPg(int pageNum, int pageSize, Map map);

    /**
     * 查询出已借款并且未还款的借款
     * 用于请款，报销时冲抵
     * @param map
     * @return
     */
    PageInfo<Map> listPgForOutgo(int pageNum, int pageSize, Map map);

    PageInfo<Map> listPgForReimbursement(int pageNum, int pageSize, Map map);

    Borrow getById(Integer id) ;

    Borrow changeAccount(Borrow entity,Map map);

    Borrow add(Borrow entity);

    Borrow edit(Borrow entity);

    Borrow update(Borrow entity);

    void delById(Borrow entity);

    Borrow confirm(Map map, Borrow entity);

    Borrow checkBtoB(Borrow entity,String desc);

    Borrow repay(Borrow entity);

    Borrow repayConfirm(Borrow entity);

    Borrow repayReject(Borrow entity);

    @Transactional
    void CWReject(Borrow entity);

    @Transactional
    Boolean CWReturn(Borrow entity);

    List<BorrowRepay> queryRepayByBorrowId(Integer borrowId);

    //借款表还款
    void dealBorrowInfo(Borrow borrow, Double amount);

    //借款表还款信息撤回
    void backBorrowInfo(Borrow borrow, Double amount);

    List<Map> exportBorrow(Map map, OutputStream outputStream);


    Map remburseSum(Map map);

    String downloadBorrowData(Map<String, Object> param);

    String batchDownloadData(Map<String, Object> param);

    List<Map> listBorrowData(Map<String, Object> param);
}
