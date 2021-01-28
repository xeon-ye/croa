package com.qinfei.core.mapper;

import com.qinfei.core.entity.Log;
import com.qinfei.core.utils.ProviderUtil;
import org.apache.ibatis.annotations.*;

import java.util.List;


public interface LogMapper extends BaseMapper<Log, Integer> {

    @InsertProvider(type = ProviderUtil.class, method = "insert")
    @Options(useGeneratedKeys = true)
    int save(Log log);

    @SelectProvider(type = ProviderUtil.class, method = "all")
    @Results({
            @Result(property = "user", column = "user_id",
                    one = @One(select = "com.qinfei.qferp.mapper.sys.UserMapper.getById"))
    })
    List<Log> logs();

    @Results({
            @Result(property = "user", column = "user_id",
                    one = @One(select = "com.qinfei.qferp.mapper.sys.UserMapper.getById"))
    })
    Log get(Integer id);


    int delete(Integer id);


    @SelectProvider(type = ProviderUtil.class, method = "listByOrder")
    @Results({
            @Result(property = "user", column = "user_id",
                    one = @One(select = "com.qinfei.qferp.mapper.sys.UserMapper.getById"))
    })
    List<Log> search(@Param("t") Log log,@Param("orders") String... order);
}
