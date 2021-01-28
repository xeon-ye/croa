package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.sys.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface SysUpdatePasswordMapper extends BaseMapper<SysUpdatePassword, Integer> {


      @Select("SELECT * FROM sys_update_pwd where creator=#{creator} ")
      List<SysUpdatePassword> querySysUpdatePasswordByCreator(@Param("creator") Integer creator);

}
