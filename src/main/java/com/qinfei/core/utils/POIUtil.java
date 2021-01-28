//package com.qinfei.core.utils;
//
//
//import org.apache.poi.hssf.usermodel.*;
//import org.apache.poi.hwpf.HWPFDocument;
//import org.apache.poi.hwpf.converter.PicturesManager;
//import org.apache.poi.hwpf.converter.WordToHtmlConverter;
//import org.apache.poi.hwpf.usermodel.Picture;
//import org.apache.poi.hwpf.usermodel.PictureType;
//import org.apache.poi.ss.usermodel.HorizontalAlignment;
//import org.apache.poi.xwpf.converter.core.BasicURIResolver;
//import org.apache.poi.xwpf.converter.core.FileImageExtractor;
//import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
//import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
//import org.apache.poi.xwpf.usermodel.XWPFDocument;
//import org.springframework.util.FileCopyUtils;
//import org.w3c.dom.Document;
//import sun.misc.BASE64Encoder;
//
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.transform.OutputKeys;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Map;
//
///**
// * poi 导出excel 工具类
// *
// * @author GZW
// */
//public class POIUtil {
//
//    /**
//     * 1.创建 workbook
//     *
//     * @return
//     */
//    private HSSFWorkbook getHSSFWorkbook() {
//        return new HSSFWorkbook();
//    }
//
//    /**
//     * 2.创建 sheet
//     *
//     * @param hssfWorkbook
//     * @param sheetName    sheet 名称
//     * @return
//     */
//    private HSSFSheet getHSSFSheet(HSSFWorkbook hssfWorkbook, String sheetName) {
//        return hssfWorkbook.createSheet(sheetName);
//    }
//
//    /**
//     * 3.写入表头信息
//     *
//     * @param hssfWorkbook
//     * @param hssfSheet
//     * @param headInfoList List<Map<String, Object>>
//     *                     key: title         列标题
//     *                     columnWidth   列宽
//     *                     dataKey       列对应的 dataList item key
//     */
//    private void writeHeader(HSSFWorkbook hssfWorkbook, HSSFSheet hssfSheet, List<Map<String, Object>> headInfoList) {
//        HSSFCellStyle cs = hssfWorkbook.createCellStyle();
//        HSSFFont font = hssfWorkbook.createFont();
//        font.setFontHeightInPoints((short) 12);
//        font.setBold(true);
//        cs.setFont(font);
//        cs.setAlignment(HorizontalAlignment.CENTER);
//
//        HSSFRow r = hssfSheet.createRow(0);
//        r.setHeight((short) 380);
//        HSSFCell c = null;
//        Map<String, Object> headInfo = null;
//        //处理excel表头
//        for (int i = 0, len = headInfoList.size(); i < len; i++) {
//            headInfo = headInfoList.get(i);
//            c = r.createCell(i);
//            c.setCellValue(headInfo.get("title").toString());
//            c.setCellStyle(cs);
//            if (headInfo.containsKey("columnWidth")) {
//                hssfSheet.setColumnWidth(i, (short) (((Integer) headInfo.get("columnWidth") * 8) / ((double) 1 / 20)));
//            }
//        }
//    }
//
//    /**
//     * 4.写入内容部分
//     *
//     * @param hssfWorkbook
//     * @param hssfSheet
//     * @param startIndex   从1开始，多次调用需要加上前一次的dataList.size()
//     * @param headInfoList List<Map<String, Object>>
//     *                     key: title         列标题
//     *                     columnWidth   列宽
//     *                     dataKey       列对应的 dataList item key
//     * @param dataList
//     */
//    private void writeContent(HSSFWorkbook hssfWorkbook, HSSFSheet hssfSheet, int startIndex,
//                              List<Map<String, Object>> headInfoList, List<Map<String, Object>> dataList) {
//        Map<String, Object> headInfo = null;
//        HSSFRow r = null;
//        HSSFCell c = null;
//        //处理数据
//        Map<String, Object> dataItem = null;
//        Object v = null;
//        for (int i = 0, rownum = startIndex, len = (startIndex + dataList.size()); rownum < len; i++, rownum++) {
//            r = hssfSheet.createRow(rownum);
//            r.setHeightInPoints(16);
//            dataItem = dataList.get(i);
//            for (int j = 0, jlen = headInfoList.size(); j < jlen; j++) {
//                headInfo = headInfoList.get(j);
//                c = r.createCell(j);
//                v = dataItem.get(headInfo.get("dataKey").toString());
//
//                if (v instanceof String) {
//                    c.setCellValue((String) v);
//                } else if (v instanceof Boolean) {
//                    c.setCellValue((Boolean) v);
//                } else if (v instanceof Calendar) {
//                    c.setCellValue((Calendar) v);
//                } else if (v instanceof Double) {
//                    c.setCellValue((Double) v);
//                } else if (v instanceof Integer
//                        || v instanceof Long
//                        || v instanceof Short
//                        || v instanceof Float) {
//                    c.setCellValue(Double.parseDouble(v.toString()));
//                } else if (v instanceof HSSFRichTextString) {
//                    c.setCellValue((HSSFRichTextString) v);
//                } else {
//                    c.setCellValue(v.toString());
//                }
//            }
//        }
//    }
//
//    private void write2FilePath(HSSFWorkbook hssfWorkbook, String filePath) throws IOException {
//        FileOutputStream fileOut = null;
//        try {
//            fileOut = new FileOutputStream(filePath);
//            hssfWorkbook.write(fileOut);
//        } finally {
//            if (fileOut != null) {
//                fileOut.close();
//            }
//        }
//    }
//
//
//    /**
//     * 导出excel
//     * code example:
//     * List<Map<String, Object>> headInfoList = new ArrayList<Map<String,Object>>();
//     * Map<String, Object> itemMap = new HashMap<String, Object>();
//     * itemMap.put("title", "序号1");
//     * itemMap.put("columnWidth", 25);
//     * itemMap.put("dataKey", "XH1");
//     * headInfoList.add(itemMap);
//     * <p>
//     * itemMap = new HashMap<String, Object>();
//     * itemMap.put("title", "序号2");
//     * itemMap.put("columnWidth", 50);
//     * itemMap.put("dataKey", "XH2");
//     * headInfoList.add(itemMap);
//     * <p>
//     * itemMap = new HashMap<String, Object>();
//     * itemMap.put("title", "序号3");
//     * itemMap.put("columnWidth", 25);
//     * itemMap.put("dataKey", "XH3");
//     * headInfoList.add(itemMap);
//     * <p>
//     * List<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
//     * Map<String, Object> dataItem = null;
//     * for(int i=0; i < 100; i++){
//     * dataItem = new HashMap<String, Object>();
//     * dataItem.put("XH1", "data" + i);
//     * dataItem.put("XH2", 88888888f);
//     * dataItem.put("XH3", "脉兜V5..");
//     * dataList.add(dataItem);
//     * }
//     * POIUtil.exportExcel2FilePath("test sheet 1","F:\\temp\\customer2.xls", headInfoList, dataList);
//     *
//     * @param sheetName    sheet名称
//     * @param filePath     文件存储路径， 如：f:/a.xls
//     * @param headInfoList List<Map<String, Object>>
//     *                     key: title         列标题
//     *                     columnWidth   列宽
//     *                     dataKey       列对应的 dataList item key
//     * @param dataList     List<Map<String, Object>> 导出的数据
//     * @throws java.io.IOException
//     */
//    public static void exportExcel2FilePath(String sheetName, String filePath,
//                                            List<Map<String, Object>> headInfoList,
//                                            List<Map<String, Object>> dataList) throws IOException {
//        POIUtil poiUtil = new POIUtil();
//        //1.创建 Workbook
//        HSSFWorkbook hssfWorkbook = poiUtil.getHSSFWorkbook();
//        //2.创建 Sheet
//        HSSFSheet hssfSheet = poiUtil.getHSSFSheet(hssfWorkbook, sheetName);
//        //3.写入 head
//        poiUtil.writeHeader(hssfWorkbook, hssfSheet, headInfoList);
//        //4.写入内容
//        poiUtil.writeContent(hssfWorkbook, hssfSheet, 1, headInfoList, dataList);
//        //5.保存文件到filePath中
//        poiUtil.write2FilePath(hssfWorkbook, filePath);
//    }
//
//    private static String wordToHtml(String fileName, String uploadDir, String wordPath, String webDir) {
//        String htmlPath = uploadDir + "htmls/" + fileName + ".html";
//        try {
//            String imagePath = uploadDir + "/htmls/" + "images";
//            File parentFile = new File(htmlPath).getParentFile();
//            if (!parentFile.exists()) parentFile.mkdirs();
//            FileInputStream in = new FileInputStream(wordPath);
//            XWPFDocument document = new XWPFDocument(in);
//            XHTMLOptions options = XHTMLOptions.create();
//            // 存放图片的文件夹
//            options.setExtractor(new FileImageExtractor(new File(imagePath)));
////            // html中图片的路径
//            options.URIResolver(new BasicURIResolver(webDir + "/htmls/images"));
//            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(htmlPath), StandardCharsets.UTF_8);
//            XHTMLConverter.getInstance().convert(document,new FileOutputStream(htmlPath), options);
////            XHTMLConverter xhtmlConverter = XHTMLConverter.getInstance();
////            xhtmlConverter.convert(document, new FileOutputStream(htmlPath), options);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return htmlPath;
//    }
//
//    private static String word03ToHtml(String fileName, String uploadDir, String wordPath, String webDir) {
//        String htmlPath = uploadDir + "htmls/" + fileName + ".html";
//        FileWriter out = null;
//        try {
//            InputStream input = new FileInputStream(wordPath);
//            HWPFDocument wordDocument = new HWPFDocument(input);
//            WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
//                    DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
//            wordToHtmlConverter.setPicturesManager(new PicturesManager() {
//                public String savePicture(byte[] content, PictureType pictureType,
//                                          String suggestedName, float widthInches, float heightInches) {     //图片在html页面加载路径
//                    String encode = new BASE64Encoder().encode(content);
//                    int suffixIndex = suggestedName.lastIndexOf(".");
//                    if (suffixIndex > -1)
//                        return "data:image/" + suggestedName.substring(suffixIndex + 1) + ";base64," + encode;
//                    return "data:image/png;base64," + encode;
////                    return "image\\" + suggestedName;
//                }
//            });
//            wordToHtmlConverter.processDocument(wordDocument);
//            //获取文档中所有图片
//            List pics = wordDocument.getPicturesTable().getAllPictures();
//            if (pics != null) {
//                for (int i = 0; i < pics.size(); i++) {
//                    Picture pic = (Picture) pics.get(i);
//                    try {//图片保存在文件夹的路径
//                        pic.writeImageContent(new FileOutputStream(uploadDir
//                                + pic.suggestFullFileName()));
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            //创建html页面并将文档中内容写入页面
//            Document htmlDocument = wordToHtmlConverter.getDocument();
//            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//            DOMSource domSource = new DOMSource(htmlDocument);
//            StreamResult streamResult = new StreamResult(outStream);
//            TransformerFactory tf = TransformerFactory.newInstance();
//            Transformer serializer = tf.newTransformer();
//            serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
//            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
//            serializer.setOutputProperty(OutputKeys.METHOD, "html");
//            serializer.transform(domSource, streamResult);
//            outStream.close();
//            String content = new String(outStream.toString("UTF-8"));
//            out = new FileWriter(htmlPath);
//            FileCopyUtils.copy(content, out);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (out != null)
//                    out.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return htmlPath;
//    }
//
//    public static String wordToHtml(String originalFileName, String fileName, String uploadDir, String wordPath, String webDir) {
//        String htmlPath;
//        if (originalFileName.endsWith(".docx") || originalFileName.endsWith(".DOCX")) {
//            htmlPath = POIUtil.wordToHtml(fileName, uploadDir, wordPath, webDir);
//        } else {
//            htmlPath = POIUtil.word03ToHtml(fileName, uploadDir, wordPath, null);
//        }
//        return htmlPath;
//    }
//}
