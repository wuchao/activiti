package com.github.wuchao.activiti.service;

import com.github.wuchao.activiti.common.Constants;
import com.github.wuchao.activiti.domain.UserTask;
import com.github.wuchao.activiti.repository.UserTaskRepository;
import com.github.wuchao.activiti.service.dto.ActivitiTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.NeedsActiveTaskCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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

    /**
     * 获取当前 Task（当当前Task为会签任务时，查出来的Task是一个List集合，这里会报错）
     *
     * @param businessKey
     * @return
     */
    public ActivitiTaskDTO getCurrentTaskDTOByBusinessKey(String businessKey) {
        Task task = taskService.createTaskQuery()
                .processInstanceBusinessKey(businessKey)
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .singleResult();
        return task != null ? ActivitiTaskDTO.init(task) : null;
    }

    public Task getCurrentTaskByBusinessKeyAndTaskAssignee(String businessKey, String assignee) {
        return taskService.createTaskQuery()
                .processInstanceBusinessKey(businessKey)
                .taskAssignee(assignee)
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .singleResult();
    }

    public ActivitiTaskDTO getCurrentTaskDTOByBusinessKeyAndTaskAssignee(String businessKey, String assignee) {
        Task task = taskService.createTaskQuery()
                .processInstanceBusinessKey(businessKey)
                .taskAssignee(assignee)
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .singleResult();
        return task != null ? ActivitiTaskDTO.init(task) : null;
    }

    /**
     * 获取当前 Task
     *
     * @param businessKey
     * @param taskDefinitionKey
     * @return
     */
    public Task getCurrentTaskByBusinessKeyAndTaskDefinitionKey(String businessKey, String taskDefinitionKey) {
        return taskService.createTaskQuery()
                .processInstanceBusinessKey(businessKey)
                .taskDefinitionKey(taskDefinitionKey)
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .singleResult();
    }

    /**
     * 获取所有处于当前任务的流程任务
     *
     * @param taskDefinitionKey
     * @return
     */
    public List getAllTasksByTaskDefinitionKey(String taskDefinitionKey, String assignee) {
        return taskService.createTaskQuery()
                .taskDefinitionKey(taskDefinitionKey)
                .taskAssignee(assignee)
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .list();
    }

    /**
     * 获取所有处于当前任务（包括历史任务）的流程任务
     *
     * @param taskDefinitionKey
     * @param assignee
     * @return
     */
    public List getAllHistoricTasksByTaskDefinitionKey(String taskDefinitionKey, String assignee) {
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                .taskDefinitionKey(taskDefinitionKey)
                .taskAssignee(assignee)
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .list();
        if (CollectionUtils.isNotEmpty(historicTaskInstances)) {
            return historicTaskInstances.stream().map(historicTaskInstance -> ActivitiTaskDTO.init(historicTaskInstance)).collect(Collectors.toList());
        } else {
            return null;
        }
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
     * @param taskId                       当前任务的 taskId
     * @param destinationTaskDefinitionKey 打回操作目标任务节点的 taskDefinitionKey
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


    /**
     * 动态地在会签任务在原基础上加处理人
     *
     * @param currentTaskId                当前流程任务 ID
     * @param destinationTaskDefinitionKey 目标节点的 taskDefinitionKey
     * @param assignees                    要添加的 assignee 集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void dynamicAddAssignees(String currentTaskId, String destinationTaskDefinitionKey, List<String> assignees) {
        if (StringUtils.isNotBlank(currentTaskId) &&
                StringUtils.isNotBlank(destinationTaskDefinitionKey) &&
                CollectionUtils.isNotEmpty(assignees)) {

            Task currentTask = getTask(currentTaskId);
            if (currentTask != null) {
                String executionId = currentTask.getExecutionId();
                //获取流程定义
                Process process = repositoryService.getBpmnModel(currentTask.getProcessDefinitionId()).getMainProcess();
                //获取目标节点定义
                FlowNode targetNode = (FlowNode) process.getFlowElement(destinationTaskDefinitionKey);
                assignees.stream().forEach(assignee -> {
                    managementService.executeCommand(new SetFlowNodeAssigneeCmd(executionId, assignee, targetNode));
                });
            }
        }
    }

    public class SetFlowNodeAssigneeCmd implements Command<Void> {
        protected String executionId;

        protected String assignee;

        protected FlowNode flowElement;


        public SetFlowNodeAssigneeCmd(String executionId, String assignee, FlowNode flowElement) {
            this.executionId = executionId;
            this.assignee = assignee;
            this.flowElement = flowElement;
        }

        @Override
        public Void execute(CommandContext commandContext) {
            ProcessEngineConfigurationImpl pec = commandContext.getProcessEngineConfiguration();
            RuntimeService runtimeService = pec.getRuntimeService();
            TaskService taskService = pec.getTaskService();
            IdGenerator idGenerator = pec.getIdGenerator();
            Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
            ExecutionEntity parent = ((ExecutionEntity) execution).getParent();
            Task newTask = taskService.createTaskQuery().executionId(executionId).singleResult();
            ExecutionEntity newExecution = commandContext.getExecutionEntityManager().createChildExecution(parent);
            newExecution.setScope(false);
            newExecution.setSuspensionState(1);
            newExecution.setCurrentFlowElement(flowElement);
            TaskEntity t = (TaskEntity) newTask;
            TaskEntity taskEntity = new TaskEntityImpl();
            taskEntity.setCreateTime(new Date());
            taskEntity.setProcessDefinitionId(t.getProcessDefinitionId());
            taskEntity.setTaskDefinitionKey(t.getTaskDefinitionKey());
            taskEntity.setProcessInstanceId(t.getProcessInstanceId());
            taskEntity.setExecutionId(newExecution.getId());
            taskEntity.setName(newTask.getName());
            String taskId = idGenerator.getNextId();
            taskEntity.setId(taskId);
            taskEntity.setExecution(newExecution);
            taskEntity.setAssignee(assignee);
            taskEntity.setRevision(0);
            taskService.saveTask(taskEntity);
            Integer loopCounter = getLoopVariable(newExecution, "nrOfInstances");
            Integer nrOfActiveInstances = getLoopVariable(newExecution, "nrOfActiveInstances");
            setLoopVariable(newExecution, "nrOfInstances", loopCounter + 1);
            setLoopVariable(newExecution, "nrOfActiveInstances", nrOfActiveInstances + 1);
            HistoricTaskInstanceEntity historicTaskInstanceEntity = commandContext.getHistoricTaskInstanceEntityManager().create(taskEntity, newExecution);
            commandContext.getHistoricTaskInstanceEntityManager().insert(historicTaskInstanceEntity);
            return null;
        }
    }

    public static void setLoopVariable(ExecutionEntity execution, String variableName, Object value) {
        // 获取执行实例的父级
        ExecutionEntity parent = execution.getParent();
        parent.setVariableLocal(variableName, value);
    }

    public static Integer getLoopVariable(ExecutionEntity execution, String variableName) {
        Object value = execution.getVariableLocal(variableName);
        ExecutionEntity parent = execution.getParent();
        while (value == null && parent != null) {
            value = parent.getVariableLocal(variableName);
            parent = parent.getParent();
        }
        return (Integer) (value != null ? value : 0);
    }

}
