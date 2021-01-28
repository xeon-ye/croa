package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media1.MediaForm1;
import com.qinfei.qferp.entity.media1.MediaTerm1;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaTerm1Mapper
 * @Description 媒体条件表
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:15
 * @Version 1.0
 */
public interface MediaTerm1Mapper extends BaseMapper<MediaTerm1, Integer> {

    int updateById(MediaTerm1 mediaTerm1);

    int deleteBatch(@Param("ids") List<Integer> ids);

    List<Map> listByMediaPlateId(Map map);

    List<MediaTerm1> listMediaTermByPlateId(@Param("mediaPlateId") Integer mediaPlateId);

    int getMediaTermCount(MediaTerm1 mediaTerm1);

}
