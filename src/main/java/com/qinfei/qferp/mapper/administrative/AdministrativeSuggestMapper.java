package com.qinfei.qferp.mapper.administrative;

import com.qinfei.qferp.entity.administrative.AdministrativeSuggest;

public interface AdministrativeSuggestMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AdministrativeSuggest record);

    int insertSelective(AdministrativeSuggest record);

    AdministrativeSuggest selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AdministrativeSuggest record);

    int updateByPrimaryKey(AdministrativeSuggest record);
}