package com.qinfei.qferp.mapper.announcementinform;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.announcementinform.MediaPass;
import com.qinfei.qferp.entity.sys.Dept;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

public interface MediaPassMapper extends BaseMapper<MediaPass, Integer> {

    /**
     * 通知公告查询
     *
     * @param map
     * @return
     */
    @Select({"<script>",
            " SELECT t.* FROM t_announcement_inform t " +
                    "  left join t_announcement_resource_type tart " +
                    "  on t.resource_type = tart.resource_type " +
                    "  left join t_announcement_transaction_type tatt " +
                    "  on t.transaction_type = tatt.transaction_type " +
                    "  left join sys_dept s " +
                    "  on t.operation_dept_id = s.id " +
                    "  where " +
                    " case when t.create_id=#{userId} then 1=1 else t.state=4 end  " +
                    " and t.title is not null and t.title != ''" +
                    "<when test='deptId != null and deptId !=\"\"'>",
            " and (t.id in ( select id from t_operation_dapt where operation_dept_id = #{deptId} and state > -9 )or t.publish_dept_id = #{deptId}) ",
            "</when>",
            " <when test='transactionTypeQC!=null and transactionTypeQC!=\"\"'>",
            " AND t.transaction_type like '%${transactionTypeQC}%'",
            " </when>",
            " <when test='labelQC!=null and labelQC!=\"\"'>",
            " AND t.label like '%${labelQC}%'",
            " </when>",
            " <when test='resourceTypeQC!=null and resourceTypeQC!=\"\"'>",
            " AND t.resource_type like '%${resourceTypeQC}%'",
            " </when>",
            " <when test='releaseStartTimeQC!=null and releaseStartTimeQC!=\"\"'>",
            " AND t.release_time &gt;= #{releaseStartTimeQC}",
            " </when>",
            " <when test='releaseEndTimeQC!=null and releaseEndTimeQC!=\"\"'>",
            " AND t.release_time &lt;= #{releaseEndTimeQC}",
            " </when>",
            " <when test='resourcesStateQC!=null and resourcesStateQC!=\"\"'>",
            " AND t.state = #{resourcesStateQC}",
            " </when>",
            " <when test='publishDeptNameQC!=null and publishDeptNameQC!=\"\"'>",
            " AND t.publish_dept_name like '%${publishDeptNameQC}%'",
            " </when>",
            " <when test='releaseUserQC!=null and releaseUser!=\"\"'>",
            " AND t.release_user like '%${releaseUserQC}%'",
            " </when>",
            "order by t.id desc",
            "</script>"})
    List<MediaPass> selectByMap(Map map);

    @Select("select Max(value) from auto_number where code=#{code} and year=#{year} and month=#{month} ")
    Integer getMaxNo(@Param("code") String code, @Param("year") Integer year, @Param("month") Integer month);

    @Select("select * from t_announcement_inform where id = #{id}")
    MediaPass getById(Integer id);

    @Select("SELECT DISTINCT c.* FROM t_operation_dapt b,sys_dept c WHERE c.id = b.operation_dept_id " +
            "AND b.state >-1 AND c.state >- 2 AND b.id =#{id}")
    List<Dept> queryDeptByAccountId(Integer id);

    @Insert(" <script>delete from t_operation_dapt where id=#{id} " +
            " and operation_dept_id in " +
            " <foreach item=\"item\" index=\"index\" collection=\"list\"\n" +
            "   open=\"(\" separator=\",\" close=\")\">\n" +
            "   #{item.id}\n" +
            " </foreach>" +
            " </script>")
    void delDeptAccountDept(Map map);

    @Insert({"<script>",
            " insert into t_operation_dapt (" +
                    "operation_dept_id," +
                    "id," +
                    "user_id," +
                    "item_id" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.operationDeptId}," +
                    "#{item.id}," +
                    "#{item.userId}," +
                    "#{item.itemId})" +
                    "</foreach>",
            "</script>"})
    void insertoperationDept(List<Map> list);

    @Update(
            "<script>update t_operation_dapt set state=-9 where "+
                    " operation_dept_id in " +
                    " <foreach item=\"id\" index=\"index\" collection=\"list\"\n" +
                    "   open=\"(\" separator=\",\" close=\")\">\n" +
                    "   #{id}\n" +
                    " </foreach> </script>" )
    void editoperationDept (List<Integer> list);

    @Update("update t_announcement_inform set state=1 where release_time_end < now() and state <>1")
    void announcementTime();


    @Select({"<script>",
            " SELECT t.* FROM t_announcement_inform t " +
                    "  left join t_announcement_resource_type tart " +
                    "  on t.resource_type = tart.resource_type " +
                    "  left join t_announcement_transaction_type tatt " +
                    "  on t.transaction_type = tatt.transaction_type " +
                    "  left join sys_dept s " +
                    "  on t.operation_dept_id = s.id " +
                    "  where 1=1 " +
            " and t.title is not null and t.title != ''" +
            " <when test='transactionTypeQC!=null and transactionTypeQC!=\"\"'>",
            " AND t.transaction_type like '%${transactionTypeQC}%'",
            " </when>",
            " <when test='labelQC!=null and labelQC!=\"\"'>",
            " AND t.label like '%${labelQC}%'",
            " </when>",
            " <when test='resourceTypeQC!=null and resourceTypeQC!=\"\"'>",
            " AND t.resource_type like '%${resourceTypeQC}%'",
            " </when>",
            " <when test='releaseStartTimeQC!=null and releaseStartTimeQC!=\"\"'>",
            " AND t.release_time &gt;= #{releaseStartTimeQC}",
            " </when>",
            " <when test='releaseEndTimeQC!=null and releaseEndTimeQC!=\"\"'>",
            " AND t.release_time &lt;= #{releaseEndTimeQC}",
            " </when>",
            " <when test='resourcesStateQC!=null and resourcesStateQC!=\"\"'>",
            " AND t.state = #{resourcesStateQC}",
            " </when>",
            " <when test='publishDeptNameQC!=null and publishDeptNameQC!=\"\"'>",
            " AND t.publish_dept_name like '%${publishDeptNameQC}%'",
            " </when>",
            " <when test='releaseUserQC!=null and releaseUser!=\"\"'>",
            " AND t.release_user like '%${releaseUserQC}%'",
            " </when>",
            "AND t.company_code=#{companyCode}",
            "order by t.id desc",
            "</script>"})
    List<MediaPass> selectAll(Map map);



    @Select("select item_id from t_operation_dapt where id = #{Id} and  user_id=#{userId} and operation_dept_id=#{operationDeptId}")
    Integer[] finItem(Map map);

    @Select("select tod.item_id from t_operation_dapt tod LEFT JOIN t_index_items tii on tod.item_id= tii.id  WHERE tod.id=#{id}")
    List<Integer> itemId(Integer id);

    @Update("update t_index_items set transaction_state=2 where id= #{itemId}")
    void updateItemState(Map map);

}