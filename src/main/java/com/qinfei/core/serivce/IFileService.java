package com.qinfei.core.serivce;

import com.qinfei.core.entity.FileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author: 66
 * @Date: 2020/11/14 15:25
 * @Description: 文件业务层
 */
public interface IFileService<T extends FileEntity> {
    void handleFile(T t, MultipartFile[] multipartFiles, T oldT,String path) throws IOException;
}
