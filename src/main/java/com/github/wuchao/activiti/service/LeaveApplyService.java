package com.github.wuchao.activiti.service;

import com.github.wuchao.activiti.domain.LeaveApplication;
import com.github.wuchao.activiti.repository.LeaveApplicationRepository;
import com.github.wuchao.activiti.repository.UserRepository;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class LeaveApplyService {

    private final WorkflowService workflowService;

    private final UserRepository userRepository;

    private final LeaveApplicationRepository leaveApplicationRepository;

    public LeaveApplyService(WorkflowService workflowService, UserRepository userRepository, LeaveApplicationRepository leaveApplicationRepository) {
        this.workflowService = workflowService;
        this.userRepository = userRepository;
        this.leaveApplicationRepository = leaveApplicationRepository;
    }

    /**
     * 创建请假申请，并启动流程
     *
     * @param userId
     * @param leaveApplication
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyForLeave(Long userId, String username, LeaveApplication leaveApplication) {
        // 创建请假申请
        leaveApplication.setApplicantId(userId);
        leaveApplication.setStatus("等待 HR 审核");
        leaveApplicationRepository.save(leaveApplication);

        // 启动请假申请流程
        startupLeaveApplyProcess(username, leaveApplication.getId(), null);
    }

    /**
     * 启动请假申请流程
     *
     * @param username
     * @param variables 要保存的流程中的参数，供流程使用
     */
    @Transactional(rollbackFor = Exception.class)
    public void startupLeaveApplyProcess(String username, Long leaveId, Map<String, Object> variables) {
        if (leaveId != null && StringUtils.isNotBlank(username)) {
            // 流程图文件中的 process 标签的 id
            String processDefinitionKey = "leave";
            // 自定义的业务相关的标识
            String businessKey = "leave" + "_" + leaveId;
            // 流程启动成功，会返回一个流程实例对象
            ProcessInstance processInstance = workflowService.startWorkflow(username, processDefinitionKey, businessKey, variables);
            if (processInstance != null) {
                Task task = workflowService.getCurrentTaskByProcessInstanceId(processInstance.getProcessInstanceId());
                if (task != null) {
                    // 提交申请，将流程转到“hr 审核”
                    workflowService.completeTask(task.getId());
                }
            }
        }
    }

}
