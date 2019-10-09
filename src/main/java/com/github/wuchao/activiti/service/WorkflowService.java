package com.github.wuchao.activiti.service;

import com.github.wuchao.activiti.common.Constants;
import com.github.wuchao.activiti.domain.UserTask;
import com.github.wuchao.activiti.repository.UserTaskRepository;
import com.github.wuchao.activiti.service.dto.ActivitiTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cmd.NeedsActiveTaskCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManagerImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private ManagementService managementService;

    @Autowired
    private UserTaskRepository userTaskRepository;

    @Autowired
    private UserTaskAndMessageService userTaskAndMessageService;

    /**
     * 启动流程
     *
     * @param authenticatedUserId
     * @param processDefinitionKey
     * @param businessKey
     * @param variables
     * @return
     */
    public ProcessInstance startWorkflow(String authenticatedUserId, String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        ProcessInstance processInstance;
        try {
            // 用来设置启动流程的人员 ID，Activiti 会自动把用户 ID 保存到 activiti:initiator 中
            identityService.setAuthenticatedUserId(authenticatedUserId);
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

    // https://www.cnblogs.com/laoxia/p/9761277.html
    // 使用 setAssignee 或 setAssignee + setOwner 是用来转办任务的，和原来的转办人没有任何关系了

    /**
     * 分配任务/转办任务（将当前办理人换成其他人，这是最好将owner也同步进行设置）
     *
     * @param taskId
     * @param userId
     */
    public void setAssignee(String taskId, String userId) {
        taskService.setAssignee(taskId, userId);
        log.info("将任务（ID: {}）委托给其他用户（ID：{}）", taskId, userId);
    }

    /**
     * 设置任务的所有者
     * 任务所有者可以将任务转给其他人执行
     *
     * @param taskId
     * @param userId
     */
    public void setOwner(String taskId, String userId) {
        taskService.setOwner(taskId, userId);
    }

    // 使用 delegateTask 来委托任务，被委托的任务完成后会回到委托人的任务列表中，task 的 assignee 也会变成委托人，和被委托人没有任何关系了

    /**
     * 任务所有者将任务委派给其他人代执行
     *
     * @param taskId
     * @param userId
     */
    public void delegateTask(String taskId, String userId) {
        taskService.delegateTask(taskId, userId);
        log.info("将任务（ID: {}）委托给其他用户（ID：{}）", taskId, userId);
    }

    /**
     * 被委派人完成任务
     * 被委派人完成任务后，委派任务会自动回到委派人的任务中
     *
     * @param taskId
     */
    @Transactional(rollbackFor = Exception.class)
    public void resolveTask(String taskId) {
        taskService.resolveTask(taskId);
        log.info("已完成（被委托）的 task 的 ID：" + taskId);
    }

    /**
     * 被委派人完成任务，并向下一个节点传递变量参数
     * 被委派人完成任务后，委派任务会自动回到委派人的任务中
     *
     * @param taskId
     * @param variables
     */
    @Transactional(rollbackFor = Exception.class)
    public void resolveTask(String taskId, Map variables) {
        taskService.resolveTask(taskId, variables);
    }

    /**
     * 领取任务
     * 领取任务时会检查该任务是否已经被认领，如果被认领则会抛出 ActivitiTaskAlreadyClaimedException
     *
     * @param taskId
     * @param userId
     */
    public void claim(String taskId, String userId) {
        taskService.claim(taskId, userId);
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


    public Task getTask(String taskId) {
        return taskService.createTaskQuery()
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .taskId(taskId)
                .singleResult();
    }

    public List<ActivitiTaskDTO> tasksWithVariables(String businessKey) {
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .list();
        return filterHistoricTaskInstances(historicTaskInstances,
                Long.valueOf(businessKey.substring(businessKey.lastIndexOf("_") + 1)));
    }

    public ActivitiTaskDTO getHistoricTask(String taskId) {
        HistoricTaskInstance hti = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .singleResult();
        return hti != null ? ActivitiTaskDTO.init(hti) : null;
    }

    /**
     * 过滤历史任务
     *
     * @param historicTaskInstances
     * @param businessId
     * @return
     */
    public List<ActivitiTaskDTO> filterHistoricTaskInstances(List<HistoricTaskInstance> historicTaskInstances, Long businessId) {
        if (CollectionUtils.isNotEmpty(historicTaskInstances)) {
            List<UserTask> userTasks = userTaskRepository.findAllByRelatedBusinessIdAndDeletedIsNull(businessId);
            if (CollectionUtils.isNotEmpty(userTasks)) {
                List<String> taskIds = userTasks.stream().map(e -> e.getTaskId()).collect(Collectors.toList());
                return historicTaskInstances.stream()
                        .filter(e -> taskIds.contains(e.getId()))
                        .map(t -> ActivitiTaskDTO.init(t))
                        .collect(Collectors.toList());
            } else {
                return Constants.LIST_EMPTY;
            }

        } else {
            return Constants.LIST_EMPTY;
        }
    }

    /**
     * 流程任务任意打回
     * https://blog.csdn.net/taisuiyu6397/article/details/89448217
     * https://blog.csdn.net/qq_29374433/article/details/80922795
     *
     * @param taskId
     * @param destinationTaskDefinitionKey 打回到的目标任务节点的 taskDefinitionKey
     */
    @Transactional(rollbackFor = Exception.class)
    public void reverse(String taskId, String destinationTaskDefinitionKey) {
        if (StringUtils.isNotBlank(taskId) && StringUtils.isNotBlank(destinationTaskDefinitionKey)) {
            Task task = getTask(taskId);
            if (task != null) {
                // 获取流程定义
                org.activiti.bpmn.model.Process process =
                        repositoryService.getBpmnModel(task.getProcessDefinitionId()).getMainProcess();
                // 获取目标节点定义
                FlowNode targetNode = (FlowNode) process.getFlowElementMap().get(destinationTaskDefinitionKey);
                // 删除当前运行任务
                String executionEntityId = managementService.executeCommand(new DeleteTaskCmd(taskId));
                // 流程执行到来源节点
                try {
                    // 流程执行到来源节点
                    managementService.executeCommand(new SetFLowNodeAndGoCmd(targetNode, executionEntityId));
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除当前运行时任务命令，并返回当前任务的执行对象id
     * 这里继承了 NeedsActiveTaskCmd，主要是很多跳转业务场景下，要求不能是挂起任务。可以直接继承 Command 即可
     */
    @SuppressWarnings("serial")
    public class DeleteTaskCmd extends NeedsActiveTaskCmd<String> {
        public DeleteTaskCmd(String taskId) {
            super(taskId);
        }

        @Override
        public String execute(CommandContext commandContext, TaskEntity currentTask) {
            //获取所需服务
            TaskEntityManagerImpl taskEntityManager = (TaskEntityManagerImpl) commandContext.getTaskEntityManager();
            //获取当前任务的来源任务及来源节点信息
            ExecutionEntity executionEntity = currentTask.getExecution();
            //删除当前任务,来源任务
            taskEntityManager.deleteTask(currentTask, "撤回审批", false, false);
            return executionEntity.getId();
        }

        @Override
        public String getSuspendedTaskException() {
            return "挂起的任务不能跳转";
        }
    }

    /**
     * 根据提供节点和执行对象 id，进行跳转命令
     */
    public class SetFLowNodeAndGoCmd implements Command<Void> {
        private FlowNode flowElement;
        private String executionId;

        public SetFLowNodeAndGoCmd(FlowNode flowElement, String executionId) {
            this.flowElement = flowElement;
            this.executionId = executionId;
        }

        @Override
        public Void execute(CommandContext commandContext) {
            // 获取目标节点的来源连线
            List<SequenceFlow> flows = flowElement.getIncomingFlows();
            if (flows == null || flows.size() < 1) {
                throw new ActivitiException("回退错误，目标节点没有来源连线");
            }
            // 随便选一条连线来执行，当前执行计划为，从连线流转到目标节点，实现跳转
            ExecutionEntity executionEntity = commandContext
                    .getExecutionEntityManager().findById(executionId);
            executionEntity.setCurrentFlowElement(flows.get(0));
            commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(
                    executionEntity, true);
            return null;
        }
    }

}
