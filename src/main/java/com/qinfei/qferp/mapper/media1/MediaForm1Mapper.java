package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media.MediaForm;
import com.qinfei.qferp.entity.media1.MediaForm1;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaForm1Mapper
 * @Description 媒体扩展表单
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:15
 * @Version 1.0
 */
public interface MediaForm1Mapper extends BaseMapper<MediaForm1, Integer> {

    int updateById(MediaForm1 mediaForm1);

    int deleteBatch(@Param("ids") List<Integer> ids);

    List<Map> listByMediaPlateId(Map map);

    List<MediaForm1> listMediaFormByPlateId(@Param("mediaPlateId")Integer mediaPlateId);

    List<MediaForm1> listAllMediaForm();

    List<MediaForm> listAllOldMediaForm();

    int getMediaFormCount(MediaForm1 mediaForm1);

    List<MediaForm1> listPriceTypeByPlateId(@Param("mediaPlateId") Integer mediaPlateId);

    List<MediaForm1> listAllPriceType();

}
