package com.qinfei.qferp.service.mobile;

import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * @CalssName: IProcessMobileService
 * @Description: 移动端流程接口
 * @Author: Xuxiong
 * @Date: 2020/6/8 0008 16:42
 * @Version: 1.0
 */
public interface IProcessMobileService {
    //分页查询未审核的录用信息
    PageInfo<Map<String, Object>> listHireNotAudit(Map<String, Object> param, Pageable pageable);

    //查询已审核的录用流程查询
    PageInfo<Map<String, Object>> listHireHasAudit(Map<String, Object> param, Pageable pageable);

    //查询转正、离职、交接、调岗未审核流程
    PageInfo<Map<String, Object>> listEmployeeNotAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询转正、离职、交接、调岗已审核流程
    PageInfo<Map<String, Object>> listEmployeeHasAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询未审核的请款流程
    PageInfo<Map<String, Object>> listOutgoNotAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询已审核的请款流程
    PageInfo<Map<String, Object>> listOutgoHasAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询未审核的请款唤醒流程
    PageInfo<Map<String, Object>> listOutgoWorkUpNotAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询已审核的请款唤醒流程
    PageInfo<Map<String, Object>> listOutgoWorkUpHasAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询未审核的删稿流程
    PageInfo<Map<String, Object>> listDropNotAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询未审核的删稿流程
    PageInfo<Map<String, Object>> listDropHasAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询未审核的借款流程
    PageInfo<Map<String, Object>> listBorrowNotAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询已审核的借款流程
    PageInfo<Map<String, Object>> listBorrowHasAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询未审核的报销流程
    PageInfo<Map<String, Object>> listReimursementNotAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询已审核的报销流程
    PageInfo<Map<String, Object>> listReimursementHasAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询未审核的开票流程
    PageInfo<Map<String, Object>> listInvoiceNotAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询已审核的开票流程
    PageInfo<Map<String, Object>> listInvoiceHasAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询未审核的退款流程
    PageInfo<Map<String, Object>> listRefundNotAuditByParam(Map<String, Object> param, Pageable pageable);

    //查询已审核的退款流程
    PageInfo<Map<String, Object>> listRefundHasAuditByParam(Map<String, Object> param, Pageable pageable);

    PageInfo<Map<String,Object>> administrativeList(Map<String,Object> map,Pageable pageable);

    PageInfo<Map<String,Object>> administrativeProcessAlready(Map<String,Object> map,Pageable pageable);

    PageInfo<Map<String,Object>> administrativeMeeting(Map<String,Object> map,Pageable pageable);

    PageInfo<Map<String,Object>> administrativeMeetingAlready(Map<String,Object>map,Pageable pageable);

    PageInfo<Map<String,Object>> administrativeSaveBuy(Map<String,Object> map,Pageable pageable);

    PageInfo<Map<String,Object>> administrativeSaveBuyAlready(Map<String,Object> map,Pageable pageable);

    PageInfo<Map<String,Object>> administrativeApply(Map<String,Object> map,Pageable pageable);

    PageInfo<Map<String,Object>> administrativeApplyAlready(Map<String,Object> map, Pageable pageable);

    PageInfo<Map<String,Object>> businessRefundList(Map<String,Object> map, Pageable pageable);

    PageInfo<Map<String,Object>> businessRefundListAlready(Map<String,Object> map, Pageable pageable);

    PageInfo<Map<String,Object>> businessCommissionList(Map<String,Object> map, Pageable pageable);

    PageInfo<Map<String,Object>> businessCommissionListAlready(Map<String,Object> map, Pageable pageable);

    PageInfo<Map<String,Object>> administrativePerformance(Map<String,Object>map, Pageable pageable);

    PageInfo<Map<String,Object>> administrativePerformanceAlready(Map<String,Object>map, Pageable pageable);

}
