package com.qinfei.qferp.utils;

import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 文件管理工具；
 */
@Slf4j
public class FileUtil {

    /**
     * 多文件文件上传；
     *
     * @param fileNames   ：上传的文件名；
     * @param uploadFiles ：上传的文件；
     * @param path        ：保存文件的路径；
     * @param object      ：类对象；
     * @param regex       ：文件类型校验正则；
     * @return 保存的文件名称；
     */
    public static String uploadFile(String[] fileNames, MultipartFile[] uploadFiles, String path, Class<?> object, String... regex) {
        List<String> fullPaths = new ArrayList<String>();
        int length = fileNames.length;
        if (length > 0) {
            String fileName;
            for (int i = 0; i < length; i++) {
                fileName = fileNames[i];
                // 校验上传的文件类型；
                if (StringUtils.isEmpty(regex) || checkFileType(fileName, regex)) {
                    fullPaths.add(uploadFile(fileName, uploadFiles[i], path, object));
                }
            }
        }
        return fullPaths.toString();
    }


    /**
     * 文件上传
     *
     * @param uploadFiles
     * @param path
     * @return
     */
    public static String uploadFile(MultipartFile[] uploadFiles, String path) {
        List<String> fullPaths = new ArrayList<String>();
        String fileName;
        if (uploadFiles != null && uploadFiles.length > 0) {
            for (MultipartFile file : uploadFiles) {
                fullPaths.add(saveFile(file, path));
            }
        }
        return fullPaths.toString();
    }


    /**
     * 文件保存
     *
     * @param file
     * @param destination 文件描述
     * @throws IllegalStateException
     * @throws IOException
     */
    private static String saveFile(MultipartFile file, String destination) {
        // 获取上传的文件名称，并结合存放路径，构建新的文件名称
        String filename = file.getOriginalFilename();
        String dateDir = getCurrentDateDir();
        File filepath = new File(destination + dateDir, filename);

        // 判断路径是否存在，不存在则新创建一个
        if (!filepath.getParentFile().exists()) {
            filepath.getParentFile().mkdirs();
        }
        // 将上传文件保存到目标文件目录
        try {
            file.transferTo(new File(destination + dateDir + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filepath.toString();
    }

    /**
     * 文件保存
     *
     * @param file
     * @param destination 文件描述
     * @throws IllegalStateException
     * @throws IOException
     */
    public static String saveFileWithUUID(MultipartFile file, String destination) {
        // 获取上传的文件名称，并结合存放路径，构建新的文件名称
        String filename = UUIDUtil.get32UUID();
        File filepath = new File(destination, filename);
        // 判断路径是否存在，不存在则新创建一个
        if (!filepath.getParentFile().exists()) {
            filepath.getParentFile().mkdirs();
        }
        // 将上传文件保存到目标文件目录
        try {
            file.transferTo(new File(destination + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
    }
    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM/");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }

    /**
     * 文件上传；
     *
     * @param fileName   ：上传的文件名；
     * @param uploadFile ：上传的文件；
     * @param path       ：保存文件的路径；
     * @param object     ：类对象；
     * @return 保存的文件名称；
     */
    public static String uploadFile(String fileName, MultipartFile uploadFile, String path, Class<?> object) {
        File file;
        String shortName = PrimaryKeyUtil.getStringUniqueKey();
        String endWord;
        StringBuilder filePath = new StringBuilder();
        filePath.append(getStringData()+"upload/");
        if (fileName.contains(".")) {
            String folder = getFoldName(fileName, object);
            endWord = folder.substring(0, folder.indexOf("/"));
            filePath.append(folder).append("/").append(shortName).append(".").append(endWord);
        } else {
            filePath.append(getFoldName(fileName, object)).append("/").append(shortName);
        }
        StringBuilder fullPath = new StringBuilder();
        fullPath.append(path);
        fullPath.append("/");
        fullPath.append(filePath);
        file = new File(fullPath.toString());
        try {
            File dir = new File(file.getParent());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            uploadFile.transferTo(file);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return filePath.toString();
    }

    /**
     * 获取文件的大小；
     *
     * @param file ：文件大小；
     * @return 格式化的文件大小文本显示；
     */
    public static String getFileSize(File file) {
        DecimalFormat fmt = null;
        String size = null;
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE / 2) {
            fmt = new DecimalFormat("0.## GB");
            size = fmt.format(fileSize / 1024 / 1024 / 1024.00);
        } else if (fileSize < Integer.MAX_VALUE / 2 && fileSize > Integer.MAX_VALUE / 2048) {
            fmt = new DecimalFormat("0.## MB");
            size = fmt.format(fileSize / 1024 / 1024.00);
        } else {
            fmt = new DecimalFormat("0.## KB");
            size = fmt.format(fileSize / 1024.00);
        }
        return size;
    }

    /**
     * 根据类获取目录名称；
     *
     * @param object ：类对象；
     * @return 目录名称；
     */
    private static String getFoldName(String fileName, Class<?> object) {
        if (fileName.contains(".")) {
            fileName = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.US);
        } else {
            fileName = "unknow";
        }
        StringBuilder foldName = new StringBuilder(object.getName());
        foldName.delete(0, foldName.lastIndexOf(".") + 1);
        foldName.insert(0, "/");
        foldName.insert(0, fileName);
        foldName.append("/");
//        foldName.append(DateUtils.format(new Date(), "yyyy/MM/dd"));
        return foldName.toString().toLowerCase(Locale.US);
    }

    /**
     * 根据正则校验文件类型；
     *
     * @param fileName ：文件名称；
     * @param regex    ：正则表达式，支持多个；
     * @return 是否通过校验，true为是，false为否；
     */
    private static boolean checkFileType(String fileName, String... regex) {
        int length = regex.length;
        boolean flag = false;
        if (length <= 0) {
            flag = true;
        } else {
            fileName = fileName.toLowerCase(Locale.US);
            for (int i = 0; i < length; i++) {
                if (fileName.matches(regex[i])) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * 文件删除工具类
     * @param filePathList 文件路径集合
     * @return 错误信息列表
     */
    public static List<String> delFiles(List<String> filePathList){
        List<String> result = new ArrayList<>();
        try{
            if(CollectionUtils.isNotEmpty(filePathList)){
                for(String filePath : filePathList){
                    if(!StringUtils.isEmpty(filePath)){
                        File file = new File(filePath);
                        if(file.exists() && !file.delete()){
                            filePath = filePath.replaceAll("\\\\", "/");
                            String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
                            result.add(fileName + "删除不成功");
                        }
                    }
                }
            }
        }catch (Exception e){
            result.add("文件删除异常...");
            e.printStackTrace();
        }
        return result;
    }

    //获取年/月/日目录结构
    private static String getCurrentDateDir(){
        return DateUtils.format(new Date(), "yyyy-MM");
    }
}