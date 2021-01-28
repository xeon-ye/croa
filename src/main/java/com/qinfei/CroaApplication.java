package com.qinfei;

import com.qinfei.core.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.MultipartConfigElement;
import java.io.File;


@SuppressWarnings("SpringBootApplicationSetup")
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.qinfei.qferp", "com.qinfei.core"})
@EnableTransactionManagement
@MapperScan(basePackages = {"com.qinfei.qferp.mapper", "com.qinfei.core.mapper"})
@EnableScheduling
@EnableCaching
@EnableSwagger2
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
@Import({SpringUtils.class})
@ServletComponentScan
public class CroaApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CroaApplication.class, args);
        System.out.println("启动成功.....");
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 单个文件最大
        factory.setMaxFileSize("2048MB"); // KB,MB
        /// 设置总上传数据总大小 10485760
        factory.setMaxRequestSize("20480MB");
        // 更改 文件上传临时路径
        String location = System.getProperty("user.dir") + "/data/tmp";
        File tmpFile = new File(location);
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }
        factory.setLocation(location);
        return factory.createMultipartConfig();
    }

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> connector.setProperty("relaxedQueryChars", "|{}[]\\"));
        return factory;
    }
//
//	/**
//	 * WAR
//	 */
//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//		return builder.sources(CroaApplication.class);
//	}

//    @Bean
//    public CommandLineRunner init(final RepositoryService repositoryService, final RuntimeService runtimeService) {
//        return strings -> {
//            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
//            List<String> modifyFileNames = new ArrayList<>();
//            List<String> oldFileNames = new ArrayList<>();
//            String fileName;
//            for (ProcessDefinition processDefinition : list) {
//                fileName = processDefinition.getResourceName();
//                if (fileName.contains("AdministrativeOnbusinessNew")/*||fileName.contains("NewMediaSelfRefundProcess")||fileName.contains("NewBallotProcess")*/) {
//                    repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
//                    modifyFileNames.add(fileName);
//                } else {
//                    oldFileNames.add(fileName);
//                }
//            }
//            // 增加事件监听
//            runtimeService.addEventListener(new JobListener());
//            File f = ResourceUtils.getFile("classpath:");
//            File[] files = new File(f, PROCESSES).listFiles();
//            for (File file : files) {
//                String fileName1 = file.getName();
//                fileName = PROCESSES + File.separator + fileName1;
//                if (modifyFileNames.contains(fileName)||!oldFileNames.contains(fileName)) {
//                    // 流程部署
//                    repositoryService.createDeployment().addClasspathResource(fileName).name(fileName1).deploy();
//                }
//            }
//        };
//    }
}