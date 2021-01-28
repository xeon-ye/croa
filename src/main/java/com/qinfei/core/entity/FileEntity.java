package com.qinfei.core.entity;

import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.fee.Outgo;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: 66
 * @Date: 2020/11/14 15:21
 * @Description: 文件实体
 */
@Data
public class FileEntity {
    // 文件名
    private String affixName;
    // 文件链接
    private String affixLink;

}
