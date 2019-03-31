package com.github.wuchao.activiti.listener;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LeaveApplyEventListener implements ActivitiEventListener {

    @Override
    public void onEvent(ActivitiEvent event) {

    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
