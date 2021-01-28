package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.fee.Borrow;
import com.qinfei.qferp.entity.fee.ReimbursementBorrow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ReimbursementBorrowMapper extends BaseMapper<ReimbursementBorrow,Integer> {
    @Select("select * from fee_reimbursement_borrow where  id = #{id}")
    ReimbursementBorrow getById(Integer id);

    @Select("select b.* from fee_reimbursement_borrow b where  b.rem_id = #{remId} and b.borrow_id = #{borrowId}")
    ReimbursementBorrow getByRemIdAndBorrowId(@Param("remId") Integer remId, @Param("borrowId") Integer borrowId);

    @Select("select a.id,a.code,a.title,a.type,a.apply_name applyName,a.dept_name deptName,a.apply_amount applyAmount," +
            " a.pay_amount payAmount,a.repay_amount repayAmount,remain_amount remainAmount,b.amount amount " +
            " from fee_reimbursement_borrow b left join fee_borrow a on a.id=b.borrow_id where  b.rem_id = #{remId}")
    List<Map> queryBorrowMapByRemId(@Param("remId") Integer remId);

    //查询从表
    @Select({"<script>",
            "select a.id,a.code,a.title,a.type,a.apply_name applyName,a.dept_name deptName,a.apply_amount applyAmount, a.pay_amount payAmount,a.repay_amount repayAmount,remain_amount remainAmount,b.rem_id ,b.amount amount from fee_reimbursement_borrow b left join fee_borrow a on a.id=b.borrow_id where  b.rem_id in " +
                    "<foreach collection=\"ids\" item=\"id\" open=\"(\" close=\")\" separator=\",\">\n" +
                    "    #{id}\n" +
                    "</foreach>",
            "</script>"})
    List<Map<String, Object>> listBorrowByIds(@Param("ids") List<Integer> ids);

    @Select("select a.* " +
            " from fee_reimbursement_borrow b left join fee_borrow a on a.id=b.borrow_id where  b.rem_id = #{remId}")
    List<Borrow> queryBorrowByRemId(@Param("remId") Integer remId);

    @Select("select a.* from fee_reimbursement_borrow b left join fee_borrow a on b.borrow_id=a.id where  b.borrow_id = #{borrowId}")
    List<Borrow> queryBorrowByBorrowId(@Param("borrowId") Integer borrowId);

    @Delete("delete from fee_reimbursement_borrow where rem_id = #{remId}")
    void deleteByRemId(@Param("remId") Integer remId) ;

    @Delete("delete from fee_reimbursement_borrow where borrow_id = #{borrowId}")
    void deleteByBorrowId(@Param("borrowId") Integer borrowId) ;

}
