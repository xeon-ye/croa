package com.qinfei.qferp.service.biz;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface IArticleImportForEasyExcelService {
    @Transactional
    String batchOrderForEasyExcel(MultipartFile file);
}
