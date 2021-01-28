package com.qinfei.qferp.mapper.workbench;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.workbench.MessageRead;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface MessageReadMapper extends BaseMapper<MessageRead,Integer>{
    @Insert("<script>"+
            "insert into t_index_message_read(user_id,message_id,read_time) values" +
            "<foreach collection='messageReadList' item='item' index='index' separator=','>" +
            "(#{item.userId},#{item.messageId},#{item.readTime})"+
            "</foreach>"+
            "</script>")
    void updateMessage(@Param("messageReadList") List<MessageRead> messageReadList);

    @Select("<script>"+"select id from t_index_message where id not in (select message_id from t_index_message_read where user_id =#{userId}) and id in " +
            "<foreach collection='ids' item='item' index='index' separator=',' open='(' close=')' >" +
            "#{item}"+
            "</foreach>"+"</script>")
    List<Integer> findMessageId(Map<String,Object> map);
}
