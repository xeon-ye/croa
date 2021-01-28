package com.qinfei.qferp.service.impl.biz;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.metadata.Sheet;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.mapper.DictMapper;
import com.qinfei.qferp.mapper.biz.ArticleImportMapper;
import com.qinfei.qferp.mapper.biz.ArticleMapper;
import com.qinfei.qferp.mapper.biz.OrderMapper;
import com.qinfei.qferp.mapper.media.MediaInfoMapper;
import com.qinfei.qferp.mapper.media.SupplierMapper;
import com.qinfei.qferp.mapper.media1.Media1Mapper;
import com.qinfei.qferp.mapper.media1.MediaPlateMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.biz.IArticleImportForEasyExcelService;
import com.qinfei.qferp.service.crm.IStatisticsService;
import com.qinfei.qferp.service.impl.media.MediaTypeService;
import com.qinfei.qferp.service.media1.IMediaForm1Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ArticleImportForEasyExcelService implements IArticleImportForEasyExcelService {
    @Autowired
    ArticleImportMapper articleImportMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    MediaTypeService mediaTypeService;
    @Autowired
    SupplierMapper supplierMapper;
    @Autowired
    MediaInfoMapper mediaInfoMapper;
    @Autowired
    Media1Mapper media1Mapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    ArticleMapper articleMapper;
    @Autowired
    DictMapper dictMapper;
    @Autowired
    IStatisticsService statisticsService;
    @Autowired
    IMediaForm1Service mediaForm1Service;
    @Autowired
    private MediaPlateMapper mediaPlateMapper;
    @Value("${media.onlineTime}")
    private String onlineTime;

    @Override
    @Transactional
    public String batchOrderForEasyExcel(MultipartFile file) {
        ArticleImportExcelListener listener = new ArticleImportExcelListener(articleImportMapper, userMapper, mediaTypeService,
                supplierMapper, mediaInfoMapper, media1Mapper,orderMapper, articleMapper, dictMapper, statisticsService,
                mediaForm1Service,  mediaPlateMapper, onlineTime);
        try {
            EasyExcelFactory.readBySax(new BufferedInputStream(file.getInputStream()),
                    new Sheet(1, 2, ArticleImportExcelInfo.class), listener);
            List<String> list = listener.getResultMsg() ;
            if(CollectionUtils.isNotEmpty(list)){
                return StringUtils.join(list, ",");
            }else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "稿件批量导入异常，请联系技术人员！");
        }
    }
}
