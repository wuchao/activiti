package com.github.wuchao.activiti.service;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WorkflowService {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    /**
     * 启动流程
     *
     * @param assignee
     * @param processDefinitionKey
     * @param businessKey
     * @param variables
     * @return
     */
    public ProcessInstance startWorkflow(String assignee, String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        ProcessInstance processInstance;
        try {
            // 用来设置启动流程的人员 ID，Activiti 会自动把用户 ID 保存到 activiti:initiator 中
            identityService.setAuthenticatedUserId(assignee);
            processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
            if (processInstance != null && log.isDebugEnabled()) {
                log.debug("start process of {processDefinitionKey={}, businessKey={}, processInstanceId={}, variables={}}",
                        new Object[]{processDefinitionKey, businessKey, processInstance.getId(), variables});
            }
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
        return processInstance;
    }


    /**
     * 根据 processInstanceId 获取当前 Task
     *
     * @param processInstanceId
     * @return
     */
    public Task getCurrentTaskByProcessInstanceId(String processInstanceId) {
        if (StringUtils.isNotBlank(processInstanceId)) {
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .list();
            return tasks.get(0);
        }
        return null;
    }

    /**
     * 完成任务，并向下一个节点传递变量参数
     *
     * @param taskId
     * @param variables
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, Map variables) {
        taskService.complete(taskId, variables);
    }

    /**
     * 完成任务
     *
     * @param taskId
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId) {
        completeTask(taskId, null);
    }

}
