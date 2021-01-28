package com.qinfei.qferp.mapper.fee;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: 66
 * @date: 2020/9/14 17:08
 */
public interface UserAccountMapper {

    @Insert({
            "INSERT INTO fee_user_account (`receiving_bank`, `receiving_account`, `create_user_id`, `create_time` ) ",
            "VALUES ",
            "(#{receivingBank}, #{receivingAccount}, #{createUserId},now() )",
    })
    int insert(@Param("receivingBank") String receivingBank, @Param("receivingAccount") String receivingAccount, @Param("createUserId") Integer createUserId);

    @Select("SELECT count(1) FROM fee_user_account WHERE create_user_id = #{userId} and receiving_account = #{receivingAccount} ")
    int findCountByReceivingAccountAndUserId(@Param("userId") Integer userId, @Param("receivingAccount") String receivingAccount);

    @Select("SELECT receiving_bank as receivingBank,receiving_account as receivingAccount FROM fee_user_account WHERE create_user_id = #{value}")
    List<JSONObject> findListByUserId(Integer userId);
}
