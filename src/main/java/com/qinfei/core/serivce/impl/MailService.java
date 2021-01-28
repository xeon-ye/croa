package com.qinfei.core.serivce.impl;

import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 发送邮件工具类
 */
@Slf4j
@Service
class MailService {
    @Autowired
    private JavaMailSender mailSender; //自动注入的Bean

    @Value("${spring.mail.username}")
    private String userName; //读取配置文件中的参数
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;  //自动注入

    /**
     * 发送简单邮件
     *
     * @param receive 接收人
     * @param subject 邮件标题
     * @param content 邮件内容
     */
    public void sendSimpleMail(String receive, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(userName);
            message.setTo(receive); //自己给自己发送邮件
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.debug("邮件发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("邮件发送失败", e);
        }
    }

    /**
     * 发送HTML邮件
     *
     * @param receive 接收人
     * @param subject 邮件标题
     * @param content 邮件内容
     */
    public void sendHtmlMail(String receive, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(userName);
            helper.setTo(receive);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            log.debug("邮件发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("邮件发送失败", e);
        }
    }

    /**
     * 发送带附件的邮件
     *
     * @param receive 接收人
     * @param subject 邮件标题
     * @param content 邮件内容
     * @param files   附件列表
     */
    public void sendAttachmentsMail(String receive, String subject, String content, List<File> files) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(userName);
            helper.setTo(receive);
            helper.setSubject(subject);
            helper.setText(content);
            for (File file : files) {
                //加入邮件
                helper.addAttachment(file.getName(), file);
            }
            mailSender.send(message);
            log.debug("邮件发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("邮件发送失败", e);
        }
    }

    /**
     * 发送带静态资源的邮件
     *
     * @param receive 接收人
     * @param subject 邮件标题
     * @param content 邮件内容
     * @param files   附件列表
     */
    public void sendInlineMail(String receive, String subject, String content, List<File> files) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(userName);
            helper.setTo(receive);
            helper.setSubject(subject);
            //第二个参数指定发送的是HTML格式,同时cid:是固定的写法
            helper.setText(content, true);
//            FileSystemResource file = new FileSystemResource(new File("src/main/resources/static/image/picture.jpg"));
//            helper.addInline("picture",file);
            for (File file : files) {
                //加入邮件
                helper.addInline("picture", file);
            }
            mailSender.send(message);
            log.debug("邮件发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("邮件发送失败", e);
        }
    }

    /**
     * 发送模板邮件
     *
     * @param receive      接收人
     * @param subject      邮件标题
     * @param templateName 模板名称
     * @param model        模板数据
     */
    public void sendTemplateMail(String receive, String subject, String templateName, Map<String, Object> model) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(userName);
            helper.setTo(receive);
            helper.setSubject(subject);

            //读取 html 模板
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate(templateName);
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            helper.setText(html, true);
            mailSender.send(message);
            log.debug("邮件发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("邮件发送失败", e);
        }
    }


}
