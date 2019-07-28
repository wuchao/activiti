package com.github.wuchao.activiti.service;

import com.github.wuchao.activiti.common.Constants;
import com.github.wuchao.activiti.domain.UserTask;
import com.github.wuchao.activiti.repository.LeaveApplicationRepository;
import com.github.wuchao.activiti.repository.UserMessageRepository;
import com.github.wuchao.activiti.repository.UserTaskRepository;
import com.github.wuchao.activiti.service.dto.LeaveProcessDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserTaskAndMessageService {

    @Autowired
    private UserTaskRepository userTaskRepository;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private LeaveInfoService leaveInfoService;

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private UserMessageRepository userMessageRepository;

    /**
     * 查询个人所有任务信息
     *
     * @param userId
     * @return
     */
    public List<UserTask> userTasks(Long userId) {
        return userTaskRepository.findAllByAssignorIdAndDeletedIsNull(userId);
    }

    /**
     * 审批人查询用户请假流程信息
     *
     * @param userTaskId
     * @return
     */
    public LeaveProcessDTO userTask(Long userTaskId) {
        return userTaskRepository.findById(userTaskId)
                .map(userTask -> {
                    Long leaveApplicationId = userTask.getRelatedBusinessId();
                    return leaveApplicationRepository.findTopById(leaveApplicationId)
                            .map(leaveApplication -> {
                                return leaveInfoService
                                        .leaveProcess(leaveApplicationId);
                            })
                            .orElseGet(null);

                })
                .orElse(new LeaveProcessDTO());
    }

    /**
     * 批准
     *
     * @param userTaskId
     */
    @Transactional(rollbackFor = Exception.class)
    public void agree(Long userTaskId) {
        userTaskRepository.findById(userTaskId).ifPresent(userTask -> {
            userTask.setStatus(1);

            Map<String, Object> variables = new HashMap<>();
            variables.put(Constants.PASS, true);
            workflowService.completeTask(userTask.getTaskId(), variables);
        });
    }

    /**
     * 不批准
     *
     * @param userTaskId
     */
    @Transactional(rollbackFor = Exception.class)
    public void disagree(Long userTaskId) {
        userTaskRepository.findById(userTaskId).ifPresent(userTask -> {
            userTask.setStatus(0);

            Map<String, Object> variables = new HashMap<>();
            variables.put(Constants.PASS, false);
            workflowService.completeTask(userTask.getTaskId(), variables);
        });
    }

    /**
     * 领取任务
     *
     * @param userTaskId
     * @param userId
     */
    @Transactional(rollbackFor = Exception.class)
    public void claim(Long userTaskId, Long userId) {
        userTaskRepository.findByIdAndDeletedIsNull(userTaskId).ifPresent(userTask -> {
            workflowService.claim(userTask.getTaskId(), String.valueOf(userId));
            userTask.setAssignorId(userId);
        });
    }

    /**
     * 流程回撤到前面任意一个 task 节点
     *
     * @param taskId                       流程节点任务 ID
     * @param destinationTaskDefinitionKey 打回到的目标任务节点的 taskDefinitionKey
     */
    @Transactional(rollbackFor = Exception.class)
    public void reverse(String taskId, String destinationTaskDefinitionKey) {
        workflowService.reverse(taskId, destinationTaskDefinitionKey);
    }

    /**
     * 删除用户任务和消息
     *
     * @param userTaskId
     * @param status
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userTaskId, Integer status) {
        userTaskRepository.findByIdAndDeletedIsNull(userTaskId).ifPresent(userTask -> {
            userTask.setDeleted(1);
            userTask.setStatus(status);

            userMessageRepository.findByUserTaskIdAndDeletedIsNull(userTaskId).ifPresent(userMessage -> {
                userMessage.setDeleted(1);
            });
        });
    }

}
