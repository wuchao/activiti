package com.github.wuchao.activiti.service;

import com.github.wuchao.activiti.common.Constants;
import com.github.wuchao.activiti.domain.LeaveApplication;
import com.github.wuchao.activiti.repository.LeaveApplicationRepository;
import com.github.wuchao.activiti.repository.UserRepository;
import com.github.wuchao.activiti.service.dto.LeaveApplyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class LeaveApplyService {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private LeaveApplyService leaveApplyService;

    /**
     * 创建请假申请，并启动流程
     *
     * @param userId
     * @param leaveApplyDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyForLeave(Long userId, LeaveApplyDTO leaveApplyDTO) {
        if (leaveApplyDTO.getLeaderId() != null) {
            // 创建请假申请
            LeaveApplication leaveApplication = leaveApplyDTO.getLeaveApplication();
            if (leaveApplication != null) {
                leaveApplication.setApplicantId(userId);

                userRepository.findById(leaveApplyDTO.getLeaderId()).ifPresent(user -> {
                    leaveApplication.setStatus("等待" + user.getNickname() + "审核");
                });

                leaveApplicationRepository.save(leaveApplication);

                Map<String, Object> variables = new HashMap<>();
                variables.put(Constants.APPLY_LEADER1_ID, leaveApplyDTO.getLeaderId());
                variables.put(Constants.APPLY_LEADER2_ID, leaveApplyDTO.getLeaderId());

                // 启动请假申请流程
                leaveApplyService.startupLeaveApplyProcess(userId, leaveApplication.getId(), variables);
            }
        } else {
            throw new RuntimeException();
        }
    }

    /**
     * 启动请假申请流程
     *
     * @param variables 要保存的流程中的参数，供流程使用
     */
    @Transactional(rollbackFor = Exception.class)
    public void startupLeaveApplyProcess(Long userId, Long leaveApplicationId, Map<String, Object> variables) {
        if (userId != null && leaveApplicationId != null) {
            // 流程图文件中的 process 标签的 id
            String processDefinitionKey = Constants.LEAVE;
            // 自定义的业务相关的标识
            String businessKey = Constants.LEAVE + "_" + leaveApplicationId;
            if (variables == null) {
                variables = new HashMap<>();
            }
            variables.put(Constants.PROCESS_INITIATOR_ID, userId);
            variables.put(Constants.LEAVE_APPLICATION_ID, leaveApplicationId);

            // 启动流程
            workflowService.startWorkflow(userId.toString(), processDefinitionKey, businessKey, variables);
        }
    }

}
