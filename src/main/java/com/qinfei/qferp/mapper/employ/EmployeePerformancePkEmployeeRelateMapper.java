package com.qinfei.qferp.mapper.employ;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.employ.EmployeePerformancePKEmployeeRelate;
import com.qinfei.qferp.entity.employ.EmployeePerformancePk;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Created by yanhonghao on 2019/4/23 17:30.
 */
public interface EmployeePerformancePkEmployeeRelateMapper extends
        BaseMapper<EmployeePerformancePKEmployeeRelate, Integer> {

    @Insert({"<script>",
            " insert into e_employee_performance_pk_employee_relate (" +
                    "e_employee_performance_pk_id," +
                    "left_employee_id," +
                    "left_employee_name," +
                    "right_employee_name," +
                    "right_employee_id" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.eEmployeePerformancePkId}," +
                    "#{item.leftEmployeeId}," +
                    "#{item.leftEmployeeName}," +
                    "#{item.rightEmployeeName}," +
                    "#{item.rightEmployeeId})" +
                    "</foreach>",
            "</script>"})
    void insertBatch(List<EmployeePerformancePKEmployeeRelate> list);

    @Select("select * from e_employee_performance_pk_employee_relate where e_employee_performance_pk_id = #{v}")
    List<EmployeePerformancePKEmployeeRelate> selectByPKId(int pkId);

    @Update({"<script>",
            "update e_employee_performance_pk_employee_relate set",
            "left_employee_id = case id ",
            "<foreach collection='list' item='item' separator=' '>",
            "when #{item.id} then #{item.leftEmployeeId}",
            " </foreach> END,",
            "right_employee_id = case id ",
            "<foreach collection='list' item='item' separator=' '>",
            "when #{item.id} then #{item.rightEmployeeId}",
            " </foreach> END,",
            "left_employee_name = case id ",
            "<foreach collection='list' item='item' separator=' '>",
            "when #{item.id} then #{item.leftEmployeeName}",
            " </foreach> END,",
            "right_employee_name = case id ",
            "<foreach collection='list' item='item' separator=' '>",
            "when #{item.id} then #{item.rightEmployeeName}",
            " </foreach> END",
            "where id IN",
            "<foreach collection='list' item='item' open='(' close=')' separator=','>",
            "#{item.id}",
            "</foreach>",
            "</script>"})
    void updateBatch(List<EmployeePerformancePKEmployeeRelate> list);

    @Delete("delete from e_employee_performance_pk_employee_relate where e_employee_performance_pk_id = #{v}")
    long deleteByPKId(int id);
}
