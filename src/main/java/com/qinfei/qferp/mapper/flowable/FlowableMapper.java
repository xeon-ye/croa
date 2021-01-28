package com.qinfei.qferp.mapper.flowable;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.sys.District;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * Created by yanhonghao on 2019/6/21 10:38.
 */
public interface FlowableMapper extends BaseMapper<District, Integer> {
    @Delete("delete from act_hi_taskinst where PROC_INST_ID_=#{id} and TASK_DEF_KEY_ = #{key}")
    void deleteByProcInstIdAndTaskDefKey(@Param("id") String procInstId, @Param("key") String taskDefKey);

    @Select("select TASK_DEF_KEY_ from act_ru_task where ID_ = #{v}")
    String findFlowKeyByTaskId(String taskId);

    @Select("select TASK_DEF_KEY_ from act_hi_taskinst where EXECUTION_ID_ = #{id} and TASK_DEF_KEY_ != #{key} order by START_TIME_ desc limit 1")
    String findPreviousKey(@Param("id") String executorId, @Param("key") String defKey);

    @Select("select ID_ as id,ACT_ID_ as type, ACT_NAME_ as name from act_hi_actinst where PROC_INST_ID_ = #{id} order by START_TIME_ desc, END_TIME_ desc")
    List<Map<String, String>> listByProcInstId(String procInstId);

    @Select("select TASK_DEF_KEY_ as processKey,NAME_ as processName,OWNER_ as owner, ASSIGNEE_ as ownerId from act_hi_taskinst where PROC_INST_ID_ = #{v} and REV_ = 3 order by END_TIME_ desc")
    List<Map<String, String>> listTaskByProcInstId(String procInstId);

    @Delete("<script>" +
            "delete from act_hi_actinst where ID_ in " +
            "   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">" +
            "       #{item}" +
            "   </foreach>" +
            "</script>")
    void deleteActByIds(List<String> ids);

    @Select("select PROC_INST_ID_ from ACT_RU_TASK where ID_ = #{v}")
    String findProcInstIdByTaskId(String taskId);

    @Select("select ASSIGNEE_ from ACT_RU_TASK where EXECUTION_ID_ = #{v}")
    String findAssigneeByExecutorId(String executorId);

    @Select("select PROC_INST_ID_ as processInstId, PROC_DEF_ID_ as processDefId from act_hi_taskinst where ID_ = #{v}")
    Map<String, String> findProcInstIdByHisTaskId(String taskId);

    @Select("select ID_ as id, PROC_DEF_ID_ as procDefId, PROC_INST_ID_ as procInstId, EXECUTION_ID_ as executionId, ACT_ID_ as taskDefKey, ACT_NAME_ as taskName, ACT_TYPE_ as type from act_hi_actinst where PROC_INST_ID_ = #{id} order by START_TIME_ ASC, END_TIME_ ASC, ID_ ASC")
    List<Map<String, String>> listActByProcInstId(String procInstId);

    @Select("SELECT ahv.NAME_ as name, ahv.LONG_ as value FROM act_hi_varinst ahv WHERE ahv.PROC_INST_ID_ = #{procInstId} AND ahv.VAR_TYPE_ = 'boolean' AND ahv.NAME_ LIKE 'gateCheck%'")
    List<Map<String, Object>> listGatewayValList(@Param("procInstId") String procInstId);
    //获取审核的节点，审核人
    @Select("SELECT a.NAME_ name,a.OWNER_ owner from act_ru_task a LEFT JOIN act_hi_taskinst b on a.ID_=b.ID_ where a.PROC_INST_ID_=#{taskId}")
    Map findTaskNameAndPoint(String taskId);

    List<Map<String, Object>> listHistoryTask(@Param("param") List<Map<String, Object>> param);

    List<Map<String, String>> listHasAuditTaskByProcInstId(@Param("procInstId") String procInstId);


}
