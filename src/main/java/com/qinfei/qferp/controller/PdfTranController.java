//package com.qinfei.qferp.controller;
//
//import com.qinfei.core.exception.QinFeiException;
//import lombok.extern.slf4j.Slf4j;
//import org.artofsolving.jodconverter.OfficeDocumentConverter;
//import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
//import org.artofsolving.jodconverter.office.OfficeManager;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.annotation.PreDestroy;
//import java.io.File;
//
///**
// * Created by yanhonghao on 2019/7/13 16:43.
// */
//@Slf4j
//@RestController
//@RequestMapping("pdfTran")
//public class PdfTranController {
//    @Value("${const.uploadDir}")
//    private String uploadDir;
//    @Value("${const.webDir}")
//    private String webDir;
//    private OfficeManager officeManager;
//
//    public PdfTranController() {
//        try {
//            // 启动OpenOffice的服务
//            DefaultOfficeManagerConfiguration config = new DefaultOfficeManagerConfiguration();
////            config.setOfficeHome("C:\\Program Files (x86)\\OpenOffice 4");
//            config.setOfficeHome("/opt/openoffice4");
//            officeManager = config.buildOfficeManager();
//            officeManager.start();
//        } catch (Exception e) {
//            log.error(e.toString());
//        }
//    }
//
//    @GetMapping
//    public String pdfTran(String inputFile) {
//        inputFile = inputFile.replace(webDir, uploadDir);
//        File f1 = new File(inputFile);
//        if (f1.length() > 8 * 1024 * 1024) throw new QinFeiException(50000, "文件大于8m,无法预览");
//
//        assert webDir != null;
//        assert uploadDir != null;
//
//        String outFilePath = inputFile.substring(0, inputFile.lastIndexOf(".")) + ".pdf";
//        File f2 = new File(outFilePath);
//        if (f2.exists()) return outFilePath.replace(uploadDir, webDir);
//        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
//        converter.convert(f1, f2);
//        return outFilePath.replace(uploadDir, webDir);
//    }
//
//    @PreDestroy
//    public void destroy() {
//        officeManager.stop();
//    }
//}
