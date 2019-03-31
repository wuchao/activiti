package com.github.wuchao.activiti.listener;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LeaveApplyExecutionListener implements ExecutionListener {

    final String EVENTNAME_START = "start";
    final String EVENTNAME_END = "end";
    final String EVENTNAME_TAKE = "take";

    @Override
    public void notify(DelegateExecution execution) {
        log.debug("execution event: " + execution.getEventName());
    }
}
