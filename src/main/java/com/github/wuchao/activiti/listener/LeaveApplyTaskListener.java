package com.github.wuchao.activiti.listener;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
public class LeaveApplyTaskListener implements TaskListener {

    final String EVENTNAME_CREATE = "create";
    final String EVENTNAME_ASSIGNMENT = "assignment";
    final String EVENTNAME_COMPLETE = "complete";
    final String EVENTNAME_DELETE = "delete";

    @Override
    public void notify(DelegateTask delegateTask) {
        log.debug("task event: " + delegateTask.getEventName());

        if (EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
            // hr 审批流程设置任务执行候选人，任务需要认领（claim）
            if ("hrs_approve".equals(delegateTask.getTaskDefinitionKey())) {
                delegateTask.addCandidateUsers(Arrays.asList("hr1", "hr2", "hr3"));
            }

            // manager 审批流程设置任务执行人
            if ("managers_approve".equals(delegateTask.getTaskDefinitionKey())) {
                delegateTask.setVariable("managerIds", Arrays.asList("manager1", "manager2"));
            }
        }
    }
}
