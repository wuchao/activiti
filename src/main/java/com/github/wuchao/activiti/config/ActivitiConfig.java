package com.github.wuchao.activiti.config;

import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ActivitiConfig extends AbstractProcessEngineAutoConfiguration implements ProcessEngineConfigurationConfigurer {

    @Bean
    @DependsOn({"dataSource", "transactionManager"})
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(DataSource atomikosDataSourceBean,
                                                                             PlatformTransactionManager transactionManager,
                                                                             SpringAsyncExecutor springAsyncExecutor) throws IOException {
        return this.baseSpringProcessEngineConfiguration(atomikosDataSourceBean, transactionManager, springAsyncExecutor);
    }

    @Autowired
    private ActivitiEventListener activitiEventListener;

    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
        Map<String, List<ActivitiEventListener>> eventListeners = new HashMap<>();
        eventListeners.put(ActivitiEventType.TASK_CREATED + "," + ActivitiEventType.TASK_COMPLETED,
                Arrays.asList(activitiEventListener));
        processEngineConfiguration.setTypedEventListeners(eventListeners);
    }

}
