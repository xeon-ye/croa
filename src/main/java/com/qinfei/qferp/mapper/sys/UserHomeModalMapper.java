package com.qinfei.qferp.mapper.sys;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;


/**
 * Created by yanhonghao on 2019/10/8 14:41.
 */
public interface UserHomeModalMapper {

    @Insert({"<script>",
            " insert into sys_user_home_modal (user_id,home_modal) values (#{userId},#{homeModal})"
            , "</script>"})
    void save(@Param("userId") Integer userId, @Param("homeModal") String homeModal);

    @Update({"<script>", "update sys_user_home_modal set home_modal = #{homeModal} where user_id = #{userId}",
            "</script>"})
    void updateByUserId(@Param("userId") Integer userId, @Param("homeModal") String homeModal);

    @Select("select user_id as userId, home_modal as homeModal from sys_user_home_modal where user_id = #{v} limit 1")
    Map<String, String> findHomeModalByUserId(Integer userId);
}
