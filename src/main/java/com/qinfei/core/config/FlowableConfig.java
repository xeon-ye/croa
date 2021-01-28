package com.qinfei.core.config;

import com.qinfei.core.flowable.JobListener;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * flowable配置----解决中中文乱码
 */
@Component
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {
    @Override
    public void configure(SpringProcessEngineConfiguration engineConfiguration) {
        engineConfiguration.setActivityFontName("宋体");
        engineConfiguration.setLabelFontName("宋体");
        engineConfiguration.setAnnotationFontName("宋体");

        List<FlowableEventListener> list = new ArrayList<>();
        list.add(new JobListener());
        Map<String, List<FlowableEventListener>> map = new HashMap<>();
        map.put("JOB_EXECUTION_SUCCESS", list);
        map.put("JOB_EXECUTION_FAILURE", list);

        engineConfiguration.setTypedEventListeners(map);
    }


}