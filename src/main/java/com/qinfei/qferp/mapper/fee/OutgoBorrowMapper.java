package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.fee.OutgoBorrow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OutgoBorrowMapper extends BaseMapper<OutgoBorrow,Integer> {
    @Select("select * from fee_outgo_borrow where  id = #{id}")
    OutgoBorrow getById(Integer id);

    @Select("select b.* from fee_outgo_borrow b where  b.outgo_id = #{outgoId} and b.borrow_id = #{borrowId}")
    OutgoBorrow getByOutgoIdAndBorrowId(@Param("outgoId") Integer outgoId, @Param("borrowId") Integer borrowId);

    @Select("select b.* from fee_outgo_borrow b where  b.outgo_id = #{outgoId}")
    List<OutgoBorrow> queryBorrowById(@Param("outgoId") Integer outgoId);

    @Select("select b.* from fee_outgo_borrow b where  b.borrow_id = #{borrowId}")
    List<OutgoBorrow> queryBorrowByBorrowId(@Param("borrowId") Integer borrowId);

    @Delete("delete from fee_outgo_borrow where outgo_id = #{outgoId}")
    void deleteByOutgoId(@Param("outgoId") Integer outgoId) ;

    @Delete("delete from fee_outgo_borrow where borrow_id = #{borrowId}")
    void deleteByBorrowId(@Param("borrowId") Integer borrowId) ;

}
