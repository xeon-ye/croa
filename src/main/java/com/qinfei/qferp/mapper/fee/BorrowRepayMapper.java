package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.fee.BorrowRepay;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BorrowRepayMapper extends BaseMapper<BorrowRepay,Integer> {

    @Select("select * from fee_borrow_repay where  id = #{id}")
    BorrowRepay getById(Integer id);

    @Select("select * from fee_borrow_repay where  borrow_id = #{borrowId} order by id desc")
    List<BorrowRepay> queryByBorrowId(Integer borrowId);

    @Select("select b.* from fee_borrow_repay b where  b.repay_id = #{repayId} and b.borrow_id = #{borrowId} and b.type=#{type} and state = #{state}")
    BorrowRepay getByRepayIdAndBorrowIdAndTypeAndState(@Param("repayId") Integer repayId, @Param("borrowId") Integer borrowId, @Param("type") Integer type, @Param("state") Integer state);

    @Select("select b.* from fee_borrow_repay b where  b.repay_id = #{repayId} and b.type=#{type}")
    List<BorrowRepay> queryByRepayIdAndType(@Param("repayId") Integer outgoId, @Param("type") Integer type);

    @Select("select b.* from fee_borrow_repay b where  b.borrow_id = #{borrowId} and b.type=#{type}")
    List<BorrowRepay> queryByBorrowIdAndType(@Param("borrowId") Integer borrowId, @Param("type") Integer type);

    @Delete("delete from fee_borrow_repay where repay_id = #{repayId} and borrow_id = #{borrowId} and type=#{type}")
    void deleteByRepayIdAndBorrowIdAndType(@Param("repayId") Integer repayId, @Param("borrowId") Integer borrowId, @Param("type") Integer type) ;

    @Delete("delete from fee_borrow_repay where repay_id = #{repayId}  and type=#{type}")
    void deleteByRepayIdAndType(@Param("repayId") Integer repayId, @Param("type") Integer type) ;

    @Delete("delete from fee_borrow_repay where borrow_id = #{borrowId} and type=#{type}")
    void deleteByBorrowIdAndType(@Param("borrowId") Integer borrowId, @Param("type") Integer type) ;

    @Delete("delete from fee_borrow_repay where repay_id = #{repayId}  and type=#{type} and state=#{state}")
    void deleteByRepayIdAndTypeAndState(@Param("repayId") Integer repayId, @Param("type") Integer type, @Param("state") Integer state) ;

}
