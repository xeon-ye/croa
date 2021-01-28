package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.sys.AutoNumber;
import com.qinfei.qferp.entity.sys.Role;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AutoNumberMapper extends BaseMapper<AutoNumber, Integer> {

    @Select("select IFNULL(Max(value)+1,1) from auto_number where code=#{code} and year=#{year} and month=#{month} ")
    Integer getMaxCode(@Param("code") String code,@Param("year") Integer year,@Param("month") Integer month) ;

}
