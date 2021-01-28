package com.qinfei.qferp.utils;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.metadata.Sheet;
import com.qinfei.core.exception.QinFeiException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @CalssName EasyExcelUtil
 * @Description excel读取工具类
 * @Author xuxiong
 * @Date 2019/10/28 0028 14:01
 * @Version 1.0
 */
public class EasyExcelUtil {
    /**
     * 读取Excel内容，当startCell内容为空时，该行数据不返回
     * @param file 文件对象
     * @param sheetIndex sheet位置从1计数
     * @param startRow 开始行，从0开始计数
     * @param startCell 开始列，从0开始计数，
     * @return 行数据列表
     */
    public static List<Object[]> getExcelContent(File file, int sheetIndex, int startRow, int startCell){
        FileInputStream in = null;
        BufferedInputStream bin = null;
        List<Object[]> list = new ArrayList<>();
        if(file.exists()) {
            try {
                in = new FileInputStream(file);
                bin = new BufferedInputStream(in);
                EasyExcelDataListener listener = new EasyExcelDataListener();
                EasyExcelFactory.readBySax(bin, new Sheet(sheetIndex, startRow), listener);
                List<Object> datas = listener.getDatas(); //获取读取数据
                if (CollectionUtils.isNotEmpty(datas)) {
                    for (Object o : datas) {
                        if(o instanceof List){
                            List<String> obj = (List<String>) o;
                            if(obj.size() > startCell && StringUtils.isNotEmpty(obj.get(startCell))){ //开始列值不为空，则缓存起来
                                Object[] objects = new Object[obj.size() - startCell];
                                for(int i = startCell; i < obj.size(); i++){
                                    objects[i - startCell] = obj.get(i) != null ? obj.get(i) : "";
                                }
                                list.add(objects);
                            }
                        }
                    }
                    datas.clear();
                    datas = null;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new QinFeiException(1002, "文件不存在！");
            } catch (Exception e) {
                e.printStackTrace();
                throw new QinFeiException(1002, "文件读取异常！");
            } finally {
                try {
                    if(bin != null){
                        bin.close();
                    }
                    if(in != null){
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new QinFeiException(1002, "文件读取异常！");
                }
            }
        }
        return list;
    }
}
