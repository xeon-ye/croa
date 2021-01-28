package com.qinfei.qferp.service.impl.fee;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.sys.AutoNumber;
import com.qinfei.qferp.mapper.sys.AutoNumberMapper;
import com.qinfei.qferp.utils.CodeUtil;

public class FeeCodeUtil {

    public static synchronized Integer getCode(String feeCode){
        AutoNumberMapper autoNumberMapper = SpringUtils.getBean(AutoNumberMapper.class) ;
        Integer year = CodeUtil.getYear() ;
        Integer month = CodeUtil.getMonth() ;
        Integer max = autoNumberMapper.getMaxCode(feeCode, year, month) ;
        //插入autoNumber表
        AutoNumber number = new AutoNumber() ;
        number.setCode(feeCode);
        number.setYear(year);
        number.setMonth(month);
        number.setValue(max);
        autoNumberMapper.insert(number) ;
        return max ;
    }
}
