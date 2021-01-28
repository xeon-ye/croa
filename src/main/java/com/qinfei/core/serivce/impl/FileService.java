package com.qinfei.core.serivce.impl;

import com.qinfei.core.config.Config;
import com.qinfei.core.entity.FileEntity;
import com.qinfei.core.serivce.IFileService;
import com.qinfei.core.utils.UUIDUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: 66
 * @Date: 2020/11/14 15:25
 * @Description: 文件业务层
 */
@Service
public class FileService<T extends FileEntity> implements IFileService<T> {

    @Resource
    private Config config;

    /**
     * 文件赋值
     *
     * @param entity         前段传递实体
     * @param multipartFiles 文件
     * @param old            数据库
     */
    @Override
    public void handleFile(T entity, MultipartFile[] multipartFiles, T old, String path) throws IOException {
        List<String> picNames = new ArrayList<>();
        List<String> picPaths = new ArrayList<>();
        //附件处理逻辑：1、如果取得的multipartFiles.length>1,那么一定是上传了多个新附件，直接使用二进制存储
        //2、如果multipartFiles.length=1，那么可能没有上传附件，也可能上传了一个附件
        //3、如果上传了一个附件，multipartFile.getSize()=1,二进制存储
        //4、如果没有上传新附件，直接把old中的附件拿过来
        if(multipartFiles != null ){
            if (multipartFiles.length > 1) {//表示上传了新附件
                for (MultipartFile multipartFile : multipartFiles) {
                    if (multipartFile.getSize() > 0) {
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath = getStringData() + path;
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        multipartFile.transferTo(destFile);
                        picNames.add(multipartFile.getOriginalFilename());
                        picPaths.add(config.getWebDir() + childPath + fileName);
                    }
                }
                entity.setAffixName(picNames.toString().replaceAll("\\[|\\]", ""));
                entity.setAffixLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            } else {
                MultipartFile multipartFile = multipartFiles[0];
                if (multipartFile.getSize() > 0) {//表示上传了新附件
                    String temp = multipartFile.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath = getStringData() + path;
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    picNames.add(multipartFile.getOriginalFilename());
                    picPaths.add(config.getWebDir() + childPath + fileName);
                    entity.setAffixName(picNames.toString().replaceAll("\\[|\\]", ""));
                    entity.setAffixLink(picPaths.toString().replaceAll("\\[|\\]", ""));
                } else {//表示附件没有变化
                    entity.setAffixName(old.getAffixName());
                    entity.setAffixLink(old.getAffixLink());
                }
            }
        }
    }

    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }
}
