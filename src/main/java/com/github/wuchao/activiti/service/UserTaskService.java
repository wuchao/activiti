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
public class UserTaskService {

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
     * 流程回退到上一节点
     *
     * @param userTaskId
     */
    @Transactional(rollbackFor = Exception.class)
    public void reverse(Long userTaskId) {
        userTaskRepository.findById(userTaskId).ifPresent(userTask -> {
            workflowService.reverse(userTask.getTaskId());
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void close(Long userTaskId, Integer status) {
        userTaskRepository.findByIdAndDeletedIsNull(userTaskId).ifPresent(userTask -> {
            userTask.setDeleted(1);
            userTask.setStatus(status);

            userMessageRepository.findByUserTaskIdAndDeletedIsNull(userTaskId).ifPresent(userMessage -> {
                userMessage.setDeleted(1);
            });
        });
    }

}
