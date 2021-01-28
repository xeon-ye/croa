package com.qinfei.core.config.redis;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.io.Serializable;

@Slf4j
@Configuration
@EnableCaching//启用缓存
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 14400)
//    @Profile({"pro"})
//继承CachingConfigurerSupport，自定义生成KEY的策略
public class RedisConfig extends CachingConfigurerSupport {

    //从application.properties中获得以下参数
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private Integer port;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.timeout}")
    private long timeout;

    /**
     * 缓存管理器
     *
     * @param factory
     * @return
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory factory) {
        return RedisCacheManager.create(factory);
    }

    //        @Bean
////    public RedisTemplate<Serializable, Serializable> redisTemplate(JedisConnectionFactory factory,GenericJackson2JsonRedisSerializer serializer) {
//    public RedisTemplate<Serializable, Serializable> redisTemplate(JedisConnectionFactory factory) {
//        RedisTemplate<Serializable, Serializable> redisTemp = new RedisTemplate<>();
////        //以上4条配置可以不用
//        redisTemp.setConnectionFactory(factory);
//        FastJsonRedisSerializer fastJson=fastJsonRedisSerializer();
//        JdkSerializationRedisSerializer jdk = new JdkSerializationRedisSerializer();
////        SerializableSerializer str = new SerializableSerializer();
//        redisTemp.setKeySerializer(jdk);//key序列化
//        redisTemp.setValueSerializer(jdk);//key序列化
//        redisTemp.afterPropertiesSet();
//        return redisTemp;
//    }
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
//    public RedisTemplate<Serializable, Serializable> redisTemplate(LettuceConnectionFactory factory) {
    public RedisTemplate<Serializable, Serializable> redisTemplate(LettuceConnectionFactory factory, FastJsonRedisSerializer fast) {
        RedisTemplate<Serializable, Serializable> template = new RedisTemplate<>();
        JdkSerializationRedisSerializer jdk = new JdkSerializationRedisSerializer();
        template.setKeySerializer(jdk);
        template.setValueSerializer(fast);
        template.setHashKeySerializer(jdk);
        template.setHashValueSerializer(fast);
        template.setConnectionFactory(factory);
        return template;
    }

    @Bean
    public FastJsonRedisSerializer<Serializable> fastJsonRedisSerializer() {
//        ParserConfig.getGlobalInstance().addAccept("com.qinfei.qferp.entity.");
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat, SerializerFeature.WriteEnumUsingToString);
//        fastJsonConfig.setSerializerFeatures(
//                SerializerFeature.WriteNullListAsEmpty,
//                SerializerFeature.WriteDateUseDateFormat,
//                SerializerFeature.WriteEnumUsingToString,
//                SerializerFeature.WriteClassName);
        FastJsonRedisSerializer<Serializable> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Serializable.class);
        fastJsonRedisSerializer.setFastJsonConfig(fastJsonConfig);
        return fastJsonRedisSerializer;
    }
//    @Bean
//    public LettuceConnectionFactory lettuceConnectionFactory(){
//        RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration();
//        cfg.setHostName(host);
//        cfg.setPort(port);
//        cfg.setPassword(RedisPassword.of(password));
//        LettuceConnectionFactory factory=new LettuceConnectionFactory(cfg);
//        return factory;
//    }

//    @Bean
//    public JedisConnectionFactory redisConnectionFactory() {
//        RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration();
//        cfg.setHostName(host);
//        cfg.setPort(port);
//        cfg.setPassword(RedisPassword.of(password));
//        JedisClientConfiguration.JedisClientConfigurationBuilder connection = JedisClientConfiguration.builder();
//        connection.connectTimeout(Duration.ofMillis(timeout));//  connection timeout
//        JedisConnectionFactory factory = new JedisConnectionFactory(cfg,
//                connection.build());
//        return factory;
//    }


    /**
     * 自定义key.
     * 此方法将会根据类名+方法名+所有参数的值生成唯一的一个key
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, objects) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append("::" + method.getName() + ":");
            for (Object obj : objects) {
                sb.append(obj == null ? "" : obj.toString());
            }
            return sb.toString();
        };
    }

//    @Bean
//    public ObjectMapper objectMapper() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        //设置输入时忽略JSON字符串中存在而Java对象实际没有的属性
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
//        //将类名称序列化到json串中(此种方式会将类路径、名称序列化进json中，不利于以后类名、包名修改)
////      mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        return mapper;
//    }


}