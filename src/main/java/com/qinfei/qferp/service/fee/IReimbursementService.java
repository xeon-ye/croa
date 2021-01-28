package com.qinfei.qferp.service.fee;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.fee.Reimbursement;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IReimbursementService {

    PageInfo<Map> listPg(int pageNum, int pageSize, Map map);

    //向主表插入数据
    Reimbursement add(Reimbursement entity,
                      List<String> costType,
                      List<String> purpose,
                      List<Double> money,
                      List<Integer> numberOfDocument,
                      List<Double> currentTotalPrice);

    Reimbursement getById(Integer id) ;

    Reimbursement changeAccount(Reimbursement entity,Map<Object,String> map);

    Reimbursement update(Reimbursement entity);

    @Transactional
    void CWReject(Reimbursement entity);

    void delById(Reimbursement entity);

    Reimbursement edit(Reimbursement entity,
                       List<String> costType,
                       List<String> purpose,
                       List<Double> money,
                       List<Integer> numberOfDocument,
                       List<Double> currentTotalPrice);

    @Transactional
    List<Map> saveBorrowInfo(Map map);

    List<Map> queryBorrowMapByRemId(Integer remId);

    @Transactional
    void cleanBorrowInfo(Integer id);

    void processReimbursement(String dataId, Integer state, Integer loginUserId, String taskId, Integer itemId, Integer acceptWorker);

    void checkBtoB(Reimbursement entity);

    /**
     * 出纳出款
     */
    void confirm(Reimbursement entity, Map map);

    /**
     * 报销导出
     */
    List<Map> export(Map map, OutputStream outputStream);

    ResponseData getByAdmId(Integer admId);

    Map reimburseSum(Map map);

    Boolean CWReturn(Reimbursement reimbursement);

    String downloadData(Map<String, Object> param);

    String batchDownloadData(Map<String, Object> param);

    List<Map> listReimburseData(Map<String, Object> param);
}
