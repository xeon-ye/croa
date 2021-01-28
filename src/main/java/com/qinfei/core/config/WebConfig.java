package com.qinfei.core.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.qinfei.core.interceptor.SecurityInterceptor;
import com.qinfei.core.login.SessionListener;
import com.qinfei.core.utils.DateUtils;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Configuration
//@EnableWebMvc
//@EnableAutoConfiguration
public class WebConfig extends WebMvcConfigurationSupport {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    Config config;
    @Resource
    private Environment env;
    private static final String DEFAULT = "yyyy-MM-dd HH:mm:ss";
    private static final String SHORT = "yyyy-MM-dd";
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        if (thymeleafViewResolver != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("version", DateUtils.format(new Date(), "yyyyMMdd"));
            thymeleafViewResolver.setStaticVariables(map);
        }
    }

    @Bean
    public ServletListenerRegistrationBean<SessionListener> servletListenerRegistrationBean() {
        ServletListenerRegistrationBean<SessionListener> slrBean = new ServletListenerRegistrationBean<SessionListener>();
        slrBean.setListener(new SessionListener());
        return slrBean;
    }

    @Bean
    public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
        return new ResourceUrlEncodingFilter();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")//设置允许跨域的路径
                .allowedOrigins("*")//设置允许跨域请求的域名
                .allowCredentials(true)//是否允许证书 不再默认开启
                .allowedMethods("GET", "POST", "PUT", "DELETE")//设置允许的方法
                .maxAge(3600);//跨域允许时间
    }

    @Resource
    private void configureThymeleafStaticVars(ThymeleafViewResolver viewResolver) {
        if (viewResolver != null) {
//            Map<String, Object> vars = Maps.newHashMap();
//            vars.put("appName", config.getAppName());
//            viewResolver.setStaticVariables(vars);
            viewResolver.addStaticVariable("AppName", env.getProperty("const.appName"));
        }
    }

//    @Override
//    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
//        // 删除MappingJackson2HttpMessageConverter
//        converters.removeIf(httpMessageConverter -> httpMessageConverter instanceof MappingJackson2HttpMessageConverter);
//        // 添加GsonHttpMessageConverter
//        converters.add(new GsonHttpMessageConverter());
//    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        // 注册Spring data jpa pageable的参数分解器
        argumentResolvers.add(new PageableHandlerMethodArgumentResolver());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/").setViewName("forward:/index.html");
//        registry.addViewController("/").setViewName("forward:/index.html");
//        registry.addViewController("/static").setViewName("forward:/static/index.html");
//        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        Map<String, String> pageUrls = config.getPageUrls();
        for (Map.Entry<String, String> entry : pageUrls.entrySet()) {
            registry.addViewController(entry.getKey().replaceAll("-", "/")).setViewName(entry.getValue());
        }
//        registry.addViewController("/").setViewName("index");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        super.addViewControllers(registry);
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
//        registry.addResourceHandler("/**").addResourceLocations("classpath:/templates/");
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
//        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/js/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler(config.getWebDir() + "**").addResourceLocations("file:" + config.getUploadDir());
        Map<String, String> pages = config.getStaticPages();
        for (Map.Entry<String, String> entry : pages.entrySet()) {
            registry.addResourceHandler(entry.getKey()).addResourceLocations(entry.getValue());
        }
//        super.addResourceHandlers(registry);

    }

    @Bean
    public PageHelper pageHelper() {
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("offsetAsPageNum", "true");
        properties.setProperty("rowBoundsWithCount", "true");
        properties.setProperty("reasonable", "true");
        properties.setProperty("dialect", "mysql");    //配置mysql数据库的方言
        pageHelper.setProperties(properties);
        return pageHelper;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        super.configurePathMatch(configurer);
    }


    @Bean
    public SecurityInterceptor securityInterceptor() {
        return new SecurityInterceptor();
    }

    /**
     * 配置拦截器
     *
     * @param registry
     * @author lance
     */
    public void addInterceptors(InterceptorRegistry registry) {
        String[] exUrls = config.getExUrls();
        registry.addInterceptor(securityInterceptor()).addPathPatterns(config.getUrls()).excludePathPatterns(exUrls);
    }

    /**
     * json时间格式化
     *
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 初始化转换器
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        // 初始化一个转换器配置
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        //PrettyFormat 格式
        //DisableCircularReferenceDetect 用禁止循环引
        //WriteEnumUsingToString枚举
//        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat,SerializerFeature.DisableCircularReferenceDetect,SerializerFeature.WriteEnumUsingToString);

//        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat, SerializerFeature.WriteEnumUsingToString);
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.MapSortField,
                SerializerFeature.IgnoreNonFieldGetter,
                // 循环引用
                SerializerFeature.DisableCircularReferenceDetect);

        // 将配置设置给转换器并添加到HttpMessageConverter转换器列表中
        fastConverter.setFastJsonConfig(fastJsonConfig);
        converters.add(fastConverter);
        super.configureMessageConverters(converters);
    }

//    /**
//     * 增加字符串转日期的功能
//     */
//    @PostConstruct
//    public void initEditableValidation() {
//        ConfigurableWebBindingInitializer initializer = (ConfigurableWebBindingInitializer) handlerAdapter
//                .getWebBindingInitializer();
//        if (initializer.getConversionService() != null) {
//            GenericConversionService genericConversionService = (GenericConversionService) initializer
//                    .getConversionService();
//            genericConversionService.addConverter(new StringToDateConverter());
//        }
//    }

    @Override
    protected void addFormatters(FormatterRegistry registry) {
//        registry.addFormatter(new DateFormatter(DEFAULT));
        registry.addConverter(addNewConvert());
    }

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> connector.setProperty("relaxedQueryChars", "|{}[]\\"));
        return factory;
    }

    @Bean
    public Converter<String, Date> addNewConvert() {
        return new Converter<String, Date>() {
            @Override
            public Date convert(String source) {
                Date date = null;
                try {
                    if (source.contains("-")) {
                        SimpleDateFormat sdf;
                        if (source.contains(":")) {
                            sdf = new SimpleDateFormat(DEFAULT);
                        } else {
                            sdf = new SimpleDateFormat(SHORT);
                        }
                        return sdf.parse(source);
                    } else if (source.matches("^\\d+$")) {
                        Long time = new Long(source);
                        return new Date(time);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return date;
            }
        };
    }
}
