package com.qinfei.qferp.controller;


import com.qinfei.core.config.Config;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

//import org.apache.poi.xwpf.converter.core.BasicURIResolver;
//import org.apache.poi.xwpf.converter.core.FileImageExtractor;
//import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
//import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;


/**
 * @Author Gong.zhiwei
 */
@RestController
@RequestMapping("/api/excel")
@AllArgsConstructor
@Slf4j
class ExcelController {

    final
    Config config;

    @PostMapping("paper")
    public String mockPaper(@RequestParam("file") MultipartFile file) throws IOException {
//        try {
//            XWPFDocument xwpfDocument = new XWPFDocument(file.getInputStream());
//            XWPFParagraph para;
//            Iterator<XWPFParagraph> iterator = xwpfDocument.getParagraphsIterator();
//            while (iterator.hasNext()) {
//                para = iterator.next();
//                log.info("输出：{}", para.getText());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        OutputStreamWriter writer = null;
        File path = new File(ResourceUtils.getURL("classpath:").getPath());
        String imagePath = config.getUploadDir() + "images";
        String targetFileName = path.getAbsolutePath() + "\\static\\word.html";
//        try {
//            XWPFDocument document = new XWPFDocument(file.getInputStream());
//            XHTMLOptions options = XHTMLOptions.create();
//            // 存放图片的文件夹
//            options.setExtractor(new FileImageExtractor(new File(imagePath)));
////            // html中图片的路径
//            options.URIResolver(new BasicURIResolver(config.getWebDir()+"images"));
//            writer = new OutputStreamWriter(new FileOutputStream(targetFileName), StandardCharsets.UTF_8);
//            XHTMLConverter xhtmlConverter = (XHTMLConverter) XHTMLConverter.getInstance();
//            xhtmlConverter.convert(document, writer, options);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (writer != null) {
//                writer.close();
//            }
//        }

        return targetFileName;

    }
}
