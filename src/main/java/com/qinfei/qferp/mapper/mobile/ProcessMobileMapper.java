package com.qinfei.qferp.mapper.mobile;

import com.qinfei.qferp.entity.biz.ProjectNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ProcessMobileMapper {
    //获取当前用户历史审核节点，返回记录ID
    List<Map<String, Object>> listHisTaskDataIdByUserId(Map<String, Object> param);

    //查询员工信息
    List<Map<String, Object>> listEmpByEmpIds(@Param("empIds") List<Integer> empIds);

    //查询未审核的录用流程查询
    List<Map<String, Object>> listHireNotAudit(Map<String, Object> param);
    List<Map<String,Object>> administrativeList(Map<String,Object>map);

    List<Map<String,Object>>administrativeProcessAlready(Map<String,Object> map);

    List<Map<String,Object>>administrativeMeeting(Map<String,Object> map);

    List<Map<String,Object>>administrativeMeetingAlready(Map<String,Object> map);

    List<Map<String,Object>> administrativeSaveBuy(Map<String,Object> map);

    List<Map<String,Object>> administrativeSaveBuyAlready(Map<String,Object> map);

    List<Map<String,Object>> administrativeApply(Map<String,Object> map);

    List<Map<String,Object>> administrativeApplyAlready(Map<String,Object> map);

    List<Map<String,Object>> businessRefundList(Map<String,Object> map);

    List<Map<String,Object>> businessRefundListAlready(Map<String,Object> map);

    List<Map<String,Object>> businessCommissionList(Map<String,Object> map);

    List<ProjectNode> selectProJect(Integer id);

    List<Map<String,Object>> businessCommissionListAlready(Map<String,Object> map);

    List<Map<String,Object>> administrativePerformance(Map<String,Object> map);

    List<Map<String,Object>> administrativePerformanceAlready(Map<String,Object> map);
    //查询已审核的录用流程查询
    List<Map<String, Object>> listHireHasAudit(Map<String, Object> param);

    //查询转正、离职、交接、调岗审核流程
    List<Map<String, Object>> listEmployeeNotAuditByParam(Map<String, Object> param);

    //查询未审核的请款流程
    List<Map<String, Object>> listOutgoNotAuditByParam(Map<String, Object> param);

    //查询已审核的请款流程
    List<Map<String, Object>> listOutgoHasAuditByParam(Map<String, Object> param);

    //查询未审核的请款唤醒流程
    List<Map<String, Object>> listOutgoWorkUpNotAuditByParam(Map<String, Object> param);

    //查询已审核的请款唤醒流程
    List<Map<String, Object>> listOutgoWorkUpHasAuditByParam(Map<String, Object> param);

    //查询未审核的删稿流程
    List<Map<String, Object>> listDropNotAuditByParam(Map<String, Object> param);

    //查询未审核的删稿流程
    List<Map<String, Object>> listDropHasAuditByParam(Map<String, Object> param);

    //查询未审核的借款流程
    List<Map<String, Object>> listBorrowNotAuditByParam(Map<String, Object> param);

    //查询已审核的借款流程
    List<Map<String, Object>> listBorrowHasAuditByParam(Map<String, Object> param);

    //查询未审核的报销流程
    List<Map<String, Object>> listReimursementNotAuditByParam(Map<String, Object> param);

    //查询已审核的报销流程
    List<Map<String, Object>> listReimursementHasAuditByParam(Map<String, Object> param);

    //查询未审核的开票流程
    List<Map<String, Object>> listInvoiceNotAuditByParam(Map<String, Object> param);

    //查询已审核的开票流程
    List<Map<String, Object>> listInvoiceHasAuditByParam(Map<String, Object> param);

    //查询未审核的退款流程
    List<Map<String, Object>> listRefundNotAuditByParam(Map<String, Object> param);

    //查询已审核的退款流程
    List<Map<String, Object>> listRefundHasAuditByParam(Map<String, Object> param);
}


