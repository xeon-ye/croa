package com.qinfei.qferp.mapper.workbench;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.workbench.Items;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface ItemsMapper extends BaseMapper<Items,Integer>{
    @Select({"<script>",
            "SELECT it.id id,it.item_name itemName,\n"+
            "it.item_type itemType,\n"+
            "it.work_type workType,\n"+
            "it.start_time startTime,\n"+
            "it.create_time createTime,\n"+
            "it.end_time endTime,\n"+
            "it.transaction_address transactionAddress,\n"+
            "it.finish_address finishAddress,\n"+
            "it.transaction_state transactionState,\n"+
            "iu.name initiatorWorkerName FROM t_index_items it\n"+
            "left join sys_user iu\n"+
            "on it.initiator_worker = iu.id\n"+
            "left join sys_dept fdept\n"+
            "on it.initiator_dept = fdept.id\n"+
            "where ( accept_worker = #{acceptWork}\n"+
            "or (accept_worker is null and accept_dept = #{userDept})\n"+
            "or (accept_worker is null and accept_dept is null) )\n"+
            "<when test='transactionState!=null and transactionState!=\"\"'>",
            "AND it.transaction_state = #{transactionState}",
            "</when>",
            "<when test='workType!=null and workType!=\"\"'>",
            "AND it.work_type like '%${workType}%'",
            "</when>",
            "<when test='urgencyLevel!=null and urgencyLevel!=\"\"'>",
            "AND it.urgency_level = #{urgencyLevel}",
            "</when>",
            "<when test='itemName!=null and itemName!=\"\"'>",
            "AND it.item_name like '%${itemName}%'",
            "</when>",
            "<when test='initiatorDeptName!=null and initiatorDeptName!=\"\"'>",
            "AND fdept.name like '%${initiatorDeptName}%'",
            "</when>",
            "<when test='initiatorWorker!=null and initiatorWorker!=\"\"'>",
            "AND iu.name like '%${initiatorWorker}%'",
            "</when>",
            "<when test='startTimeStart!=null and startTimeStart!=\"\"'>",
            "AND it.start_time &gt;= #{startTimeStart}",
            "</when>",
            "<when test='startTimeEnd!=null and startTimeEnd!=\"\"'>",
            "AND it.start_time &lt;= #{startTimeEnd}",
            "</when>",
            "<when test='updateStart!=null and updateStart!=\"\"'>",
            "AND it.end_time &gt;= #{updateStart}",
            "</when>",
            "<when test='updateEnd!=null and updateEnd!=\"\"'>",
            "AND it.end_time &lt;= #{updateEnd}",
            "</when>",
            "order by it.start_time desc",
            "</script>"
    })
    List<Map> listPg(Map params);

    @Update("UPDATE t_index_items SET transaction_state= #{transactionState,jdbcType=INTEGER} , finish_time= now() , finish_worker= #{finishWorker} WHERE id= #{id,jdbcType=INTEGER}")
    int finishItems(Items items);

    @Update("UPDATE t_index_items SET transaction_state= #{transactionState,jdbcType=INTEGER} , finish_time= now() , finish_worker= #{userId,jdbcType=INTEGER} WHERE id in (${ids})")
    int batchFinishItems(Map map);

    @Select("select transaction_state from t_index_items where id = #{itemId}")
    int queryItemStateById(Integer itemId);

    @Update({"<script>",
            " update t_index_items " +
                "<trim prefix='set' suffixOverrides=','>" +
                    "<trim prefix='transaction_address = case' suffix='end,'>" +
                        "<foreach collection='list' item='item' index='index' >" +
                            " when id=#{item.id} then #{item.transactionAddress} " +
                        "</foreach>" +
                    "</trim>" +
                "</trim>" +
                " where " +
                " <foreach collection='list' separator='or' item='i' index='index'>" +
                    " id=#{i.id}" +
                " </foreach>",
            "</script>"})
    void updateItemsTransactionAddress(List<Items> list);
}
