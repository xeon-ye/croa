package com.qinfei.qferp.mapper.media;

import com.qinfei.qferp.entity.media.FileEntitys;
import org.apache.ibatis.annotations.Param;

public interface FileEntityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FileEntitys record);

    int insertSelective(FileEntitys record);

    FileEntitys selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FileEntitys record);

    int updateByPrimaryKey(FileEntitys record);

    FileEntitys selectByArtId(@Param("artId") Integer artId, @Param("typeId") Integer typeId);

    int updateByrelevanceIdSelective(FileEntitys record);

}