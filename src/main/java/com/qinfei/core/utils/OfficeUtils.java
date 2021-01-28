package com.qinfei.core.utils;

import com.qinfei.core.config.Config;
import lombok.AllArgsConstructor;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.OfficeException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@AllArgsConstructor
public class OfficeUtils {
    private DocumentConverter documentConverter;
    private Config config;

    static final List<String> exts = Arrays.asList(".doc", ".docs", ".xls", ".xlsx", ".ppt", ".pptx");

    /**
     * 处理附件
     *
     * @param picNames
     * @param picPaths
     * @param multipartFile
     * @throws IOException
     */
    public void disposeAffix(StringBuilder picNames, StringBuilder picPaths, MultipartFile multipartFile) throws IOException, OfficeException {
        if (multipartFile.getSize() > 0) {//表示上传了新附件
            String temp = multipartFile.getOriginalFilename();
            String ext = null;
            if (temp != null && temp.contains(".")) {
                ext = temp.substring(temp.lastIndexOf("."));
            }
            String id = UUIDUtil.get32UUID();
            String fileName = id + ext;
            String childPath = "/fee/reimbursement/";
            File parent = new File(config.getUploadDir(), childPath);
            if (!parent.exists()) {
                boolean b = parent.mkdirs();
            }
            File destFile = new File(parent, fileName);
            multipartFile.transferTo(destFile);
            if (exts.contains(ext)) {
                documentConverter.convert(destFile).to(new File(parent, id + ".html")).as(DefaultDocumentFormatRegistry.HTML).execute();
                destFile.delete();
            }
            picNames.append(multipartFile.getOriginalFilename()).append(",");
            picPaths.append(config.getWebDir()).append(childPath).append(fileName).append(",");
        }
    }
    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM/");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }

    public String toHtml(String fileName, String uploadDir, String wordPath, String webDir) {
        String dir =getStringData()+ "htmls/";
        String ext = ".html";
        try {
            File parent = new File(uploadDir, dir);
            if (!parent.exists()) {
                boolean b = parent.mkdirs();
            }
            File file = new File(parent, fileName + ext);
            if (!file.exists())
                documentConverter.convert(new File(wordPath)).to(file).as(DefaultDocumentFormatRegistry.HTML).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return webDir + dir + fileName + ext;
    }

    public String toPDF(String fileName, String uploadDir, String wordPath, String webDir) {
        String dir = "pdfs/";
        String ext = ".pdf";
        try {
            File parent = new File(uploadDir, dir);
            if (!parent.exists()) {
                boolean b = parent.mkdirs();
            }
            File file = new File(parent, fileName + ext);
            if (!file.exists())
                documentConverter.convert(new File(wordPath)).to(file).as(DefaultDocumentFormatRegistry.PDF).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return webDir + dir + fileName + ext;
    }

    public String toJPEG(String fileName, String uploadDir, String wordPath, String webDir) {
        String dir = "jpgs/";
        String ext = ".jpg";
        try {
            File parent = new File(uploadDir, dir);
            File file = new File(parent, fileName + ext);
            if (!file.exists())
                documentConverter.convert(new File(wordPath)).to(file).as(DefaultDocumentFormatRegistry.JPEG).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return webDir + dir + fileName + ext;
    }
}
