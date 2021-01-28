package com.qinfei.qferp.mapper.propose;
import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.propose.Propose;
import org.apache.ibatis.annotations.*;
import java.util.Date;
import java.util.Map;
import java.util.List;

public interface ProposeMapper extends BaseMapper<Propose,Integer> {
    @Select("select propose.*,dict.name adviceType from t_propose propose " +
                "left join sys_dict dict on propose.propose_type=dict.id " +
                "where dict.type_code ='PROPOSE_TYPE' and propose.id = #{id} and dict.state>-9 and dict.state>-9")
    Propose getById(int id);

    /**
     * 建议管理
     * @param map
     * @return
     */
    @Select({"<script>" +
            "select propose.*,dict.name adviceType,user.name userName from t_propose propose " +
            "left join sys_dict dict on propose.propose_type=dict.id " +
            "left join sys_user user on propose.appoint_person=user.id " +
            "where dict.type_code ='PROPOSE_TYPE' and propose.company_code=#{companyCode} and propose.state>-9 and dict.state>-9 " +
            "<when test='proposeState!=null and proposeState!=\"\"'>",
            "and propose.state =#{proposeState}",
            "</when>",
            "<when test='userName!=null and userName!=\"\"'>",
            "and propose.name like '%${userName}%'",
            "</when>",
            "<when test='handlePersonQc!=null and handlePersonQc!=\"\"'>",
            "and propose.handle_person like '%${handlePersonQc}%'",
            "</when>",
            "<when test='problem!=null and problem!=\"\"'>",
            "and propose.problem_description like '%${problem}%'",
            "</when>",
            "<when test='proposeTypeQc!=null and proposeTypeQc!=\"\"'>",
            "and propose.propose_type =#{proposeTypeQc}",
            "</when>",
            "<when test='startTime!=null and startTime!=\"\"'>",
            "and propose.handle_time &gt;=#{startTime}",
            "</when>",
            "<when test='endTime!=null and endTime!=\"\"'>",
            "and propose.handle_time &lt;= STR_TO_DATE(concat(#{endTime},' 23:59:59'),'%Y/%m/%d %T')",
            "</when>",
            "<when test='startTimeQc!=null and startTimeQc!=\"\"'>",
            "and propose.entry_time &gt;=#{startTimeQc}",
            "</when>",
            "<when test='endTimeQc!=null and endTimeQc!=\"\"'>",
            "and propose.entry_time &lt;= STR_TO_DATE(concat(#{endTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
            "</when>",
            "<when test='yearQc!=null'>",
            " and propose.year =#{yearQc}",
            "</when>",
            "<when test='monthQc!=null'>",
            " and propose.month =#{monthQc}",
            "</when>",
            "<when test='handleResultQc!=null and handleResultQc!=\"\"'>",
            " and propose.handle_result =#{handleResultQc}",
            "</when>",
            " order by propose.entry_time desc",
            "</script>"})
    List<Map> queryPropose(Map map);

    /**
     * 建议查询
     * @param map
     * @return
     */
    @Select({"<script>" +
            "select * from "+
            "(select propose.*,dict.name adviceType from t_propose propose "+
            "left join sys_dict dict on propose.propose_type=dict.id "+
            "where dict.type_code ='PROPOSE_TYPE' and propose.company_code=#{companyCode} and propose.state>-2 and dict.state>-2 "+
            "and propose.user_Id=#{userId} "+
            "UNION "+
            "select propose.*,dict.name adviceType from t_propose propose "+
            "left join sys_dict dict on propose.propose_type=dict.id "+
            "where dict.type_code ='PROPOSE_TYPE' and propose.company_code=#{companyCode} and propose.state>-2 and dict.state>-2 "+
            "and propose.appoint_person=#{userId} "+
            "UNION "+
            "select propose.*,dict.name adviceType from t_propose propose "+
            "left join sys_dict dict on propose.propose_type=dict.id "+
            "where dict.type_code ='PROPOSE_TYPE' and propose.company_code=#{companyCode} and propose.state>-2 and dict.state>-2 "+
            "and propose.id in (SELECT DISTINCT(advice_id) from t_propose_remark where create_id = #{userId}) "+
            "UNION "+
            "select propose.*,dict.name adviceType from t_propose propose "+
            "INNER JOIN sys_dict dict on propose.propose_type=dict.id "+
            "INNER JOIN t_propose_relation a on a.id = dict.id "+
            "where dict.type_code ='PROPOSE_TYPE' and propose.company_code=#{companyCode} and propose.state>-2 and dict.state>-2 "+
            "and a.user_id = #{userId}) as t where t.state>-2 "+
            "<when test='userName!=null and userName!=\"\"'>" ,
                 " and t.name like '%${userName}%'",
            "</when>",
            "<when test='handlePersonQc!=null and handlePersonQc!=\"\"'>" ,
                 " and t.handle_person like '%${handlePersonQc}%'",
            "</when>",
            "<when test='problem!=null and problem!=\"\"'>" ,
                  " and t.problem_description like '%${problem}%'",
            "</when>",
            "<when test='proposeState!=null and proposeState!=\"\"'>" ,
                  " and t.state =#{proposeState}",
            "</when>",
            "<when test='proposeTypeQc!=null and proposeTypeQc!=\"\"'>" ,
                  " and t.propose_type =#{proposeTypeQc}",
            "</when>",
            "<when test='startTime!=null and startTime!=\"\"'>" ,
                  " and t.handle_time &gt;=#{startTime}",
            "</when>",
            "<when test='endTime!=null and endTime!=\"\"'>" ,
                  " and t.handle_time &lt;=STR_TO_DATE(concat(#{endTime},' 23:59:59'),'%Y/%m/%d %T')",
            "</when>",
            "<when test='startTimeQc!=null and startTimeQc!=\"\"'>" ,
                 " and t.entry_time &gt;=#{startTimeQc}",
            "</when>",
            "<when test='endTimeQc!=null and endTimeQc!=\"\"'>" ,
                  " and t.entry_time &lt;=STR_TO_DATE(concat(#{endTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
            "</when>",
            "<when test='yearQc!=null'>" ,
                 " and t.year =#{yearQc}",
            "</when>",
            "<when test='monthQc!=null'>" ,
                " and t.month =#{monthQc}",
            "</when>",
            "<when test='handleResultQc!=null and handleResultQc!=\"\"'>",
            " and t.handle_result =#{handleResultQc}",
            "</when>",
            " order by t.entry_time desc",
            "</script>"})
    List<Propose> queryProposeByself(Map map);

    /**
     * 建议查询(需求变更，政委角色特殊处理)
     * @param map
     * bug政委只能看到指定部门下的建议，其他部门建议指定为他时查找不到该建议
     * 会有冲突
     * @return
     */
    @Select({"<script>" +
            "select t.*,dict.name adviceType from t_propose t left join sys_dict dict " +
            "on t.propose_type=dict.id where dict.type_code ='PROPOSE_TYPE'" +
            "and t.company_code=#{companyCode} and t.state>-9 and dict.state>-9 " +
            "<when test='deptIds!=null'>" ,
            " <foreach collection='deptIds.split(\",\")' item='str' open='and t.dept_id in (' close=')' separator=','>",
            " #{str}",
            " </foreach>",
            "</when>",
            "<when test='userName!=null and userName!=\"\"'>" ,
            " and t.name like '%${userName}%'",
            "</when>",
            "<when test='handlePersonQc!=null and handlePersonQc!=\"\"'>" ,
            " and t.handle_person like '%${handlePersonQc}%'",
            "</when>",
            "<when test='problem!=null and problem!=\"\"'>" ,
            " and t.problem_description like '%${problem}%'",
            "</when>",
            "<when test='proposeState!=null and proposeState!=\"\"'>" ,
            " and t.state =#{proposeState}",
            "</when>",
            "<when test='proposeTypeQc!=null and proposeTypeQc!=\"\"'>" ,
            " and t.propose_type =#{proposeTypeQc}",
            "</when>",
            "<when test='startTime!=null and startTime!=\"\"'>" ,
            " and t.handle_time &gt;=#{startTime}",
            "</when>",
            "<when test='endTime!=null and endTime!=\"\"'>" ,
            " and t.handle_time &lt;=STR_TO_DATE(concat(#{endTime},' 23:59:59'),'%Y/%m/%d %T')",
            "</when>",
            "<when test='startTimeQc!=null and startTimeQc!=\"\"'>" ,
            " and t.entry_time &gt;=#{startTimeQc}",
            "</when>",
            "<when test='endTimeQc!=null and endTimeQc!=\"\"'>" ,
            " and t.entry_time &lt;= STR_TO_DATE(concat(#{endTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
            "</when>",
            "<when test='yearQc!=null'>" ,
            " and t.year =#{yearQc}",
            "</when>",
            "<when test='monthQc!=null'>" ,
            " and t.month =#{monthQc}",
            "</when>",
            "<when test='handleResultQc!=null and handleResultQc!=\"\"'>",
            " and t.handle_result =#{handleResultQc}",
            "</when>",
            " order by t.entry_time desc",
            "</script>"})
    List<Propose> queryProposeByZW(Map map);

    @Update({"<script>",
             " update t_propose "+
             "<trim prefix=\"set\" suffixOverrides=\",\">" +
                 "<if test='proposeType!=null and proposeType!=\"\"'>",
                    "propose_type=#{proposeType},",
                 "</if>",
                "<if test='problemDescription!=\"\"'>",
                    "problem_description=#{problemDescription},",
                "</if>",
                "<if test='expectSolution!=\"\"'>",
                    "expect_solution=#{expectSolution},",
                "</if>",
                 "<if test='handlePerson!=null and handlePerson!=\"\"'>",
                    "handle_person=#{handlePerson},",
                 "</if>",
                 "<if test='handleTime!=null'>",
                     "handle_time=#{handleTime}," ,
                 "</if>" ,
                "<if test='updateTime!=null'>",
                    "update_time=#{updateTime}," ,
                "</if>" ,
                "<if test='handleResult!=null'>",
                     "handle_result=#{handleResult}," ,
                "</if>" ,
                "<if test='appointPerson!=null and appointPerson!=\"\"'>",
                     "appoint_person=#{appointPerson}," ,
                "</if>" ,
                "<if test='handleAdvice!=null and handleAdvice!=\"\"'>",
                    "handle_advice=#{handleAdvice}," ,
                "</if>" ,
                "<if test='itemId!=null and itemId!=\"\"'>",
                    "item_id=#{itemId}," ,
                "</if>" ,
                "<if test='state!=null'>",
                "state=#{state}," ,
                "</if>" ,
                "</trim>",
                "where id=#{id}" ,
            "</script>"})
    void updatePropose(Propose t);

    @Update("update t_propose set item_id=#{item} where id= #{id}")
    void updateProposeItems(@Param("item")String item,@Param("id")Integer id);

    @Update("update t_propose set state=-9 , update_time=#{updateTime} where id=#{id}")
    void deletePropose(@Param("id") int id, @Param("updateTime") Date updateTime);

    /**
     * 判断建议类别下是否有建议
     * @param id
     * @return
     */
    @Select("select t.* from t_propose t LEFT JOIN sys_dict dict on dict.id = t.propose_type where dict.type_code = 'PROPOSE_TYPE' and dict.state>-2 and t.state>-2 and dict.id=#{id}")
    List<Propose> queryAdviceById(@Param("id") Integer id);
}
