//package com.qinfei.qferp.flow.custom;
//
//import com.qinfei.qferp.flow.custom.entity.Flow;
//import org.apache.ibatis.annotations.Insert;
//
//public interface FlowMapper {
//
//    @Insert("insert into t_flow (id,name,code,task_id) values(#{id},#{name},#{code},#{task.id})")
//    int insert(Flow flow);
//}