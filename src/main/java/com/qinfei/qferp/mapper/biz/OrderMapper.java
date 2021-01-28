package com.qinfei.qferp.mapper.biz;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.biz.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface OrderMapper extends BaseMapper<Order, Integer> {

    @Select("select * from t_biz_order where no=#{no}")
    Order getByNo(@Param("no") String no);

    @Select({"<script>", "SELECT * FROM (SELECT a.* ,b.`id` cid,b.`user_name` cusername,b.`name` cname,\n" +
            "  c.`id` uid,c.`user_name` uusername,c.`name` uname,d.id did,d.name deptname\n" +
            "FROM t_biz_order a LEFT JOIN sys_user b ON a.`user_id` = b.`id` \n" +
            "  LEFT JOIN sys_user c ON a.`update_user_id` = c.`id` \n" +
            "  LEFT JOIN sys_dept d ON a.`depat_id` = c.`id` ) a" +
            " <when test='state!=null '>",
            " AND a.state=#{state}", "</when>",
            " <when test='companyId!=null '>",
            " AND a.company_id =#{companyId}", " </when>",
            " order by id desc",
            "</script>"})
    @Results({@Result(column = "cid", property = "user.id"),
            @Result(column = "cusername", property = "user.userName"),
            @Result(column = "cname", property = "user.name"),
            @Result(column = "did", property = "dept.id"),
            @Result(column = "deptname", property = "dept.name"),
            @Result(column = "uid", property = "updateUser.id"),
            @Result(column = "uusername", property = "updateUser.userName"),
            @Result(column = "uname", property = "updateUser.name"),
    })
    List<Order> search(Order order);

    @Update("update t_biz_order set amount=#{amount} where id=#{id}")
    int updateAmount(@Param("amount") Double amount,@Param("id") Integer id);

    @Select("update t_biz_order set cust_name=#{custName} where cust_id=#{custId}")
    Order updateCustInfo(@Param("custId") Integer custId,@Param("custName") String custName);

    @Select("select * from t_biz_order where user_id=#{userId} and state>-9 and cust_id is null and depat_id = #{deptId}")
    List<Order> getOrderByUserId(@Param("userId") Integer userId,@Param("deptId") Integer deptId) ;

    //获取已经完善但没有提成的稿件
    @Select("select ord.* from t_biz_order ord LEFT JOIN t_biz_article art on ord.id = art.order_id where ord.state >-2 and art.state >-2 and cust_id = #{custId} and art.commission_states <> 1")
    List<Order> getOrderByCustId(@Param("custId") Integer custId);

//    @Select("SELECT COUNT(*) \n" +
//            "from t_biz_article tba \n" +
//            "INNER JOIN t_biz_order tbo on tba.order_id = tbo.id \n" +
//            "INNER JOIN t_crm_cust tcc on tbo.cust_id = tcc.id\n" +
//            "where tcc.id = #{custId} and tba.state > -2")
//    int dealCustnumber(@Param("custId") Integer custId);
//
//    @Select("UPDATE t_crm_cust set state=1 where id=#{id}")
//    int updateCustState(@Param("custId") Integer Id);

}