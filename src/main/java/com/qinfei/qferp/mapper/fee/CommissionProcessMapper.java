package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.fee.CommissionProcess;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface CommissionProcessMapper extends BaseMapper<CommissionMapper,Integer> {
    @Select("select * from fee_commission_process where state>-2 and id = #{id}")
    CommissionMapper getById(Integer id);

    @Insert({"<script>",
            " insert into fee_commission_process (" +
                    "user_id," +
                    "article_id," +
                    "year," +
                    "month," +
                    "income," +
                    "outgo," +
                    "taxes," +
                    "refund," +
                    "other_pay," +
                    "profit," +
                    "percent," +
                    "comm," +
                    "create_user_id," +
                    "create_time" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.userId}," +
                    "#{item.articleId}," +
                    "#{item.year}," +
                    "#{item.month}," +
                    "#{item.income}," +
                    "#{item.outgo}," +
                    "#{item.taxes}," +
                    "#{item.refund}," +
                    "#{item.otherPay}," +
                    "#{item.profit}," +
                    "#{item.percent}," +
                    "#{item.comm}," +
                    "#{item.createUserId}," +
                    "now())" +
                    "</foreach>",
            "</script>"})
    void insertBatch(List<CommissionProcess> list);

    @Update("update fee_commission_process set state=-9 where article_id = #{artId}")
    void delByArticleId(Integer artId);

    @Insert({"<script>",
            " update fee_commission_process  set state = -9,update_user_id=#{userId}" +
                    " where state=0 and article_id in" +
                    "        <foreach item='item' index='index' collection='list'" +
                    "                 open='(' separator=',' close=')'>" +
                    "            #{item}" +
                    "        </foreach>",
            "</script>"})
    void delByArticleIdBatch(Map map);

}
