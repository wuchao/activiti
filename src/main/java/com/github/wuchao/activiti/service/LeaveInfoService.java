package com.github.wuchao.activiti.service;

import com.github.wuchao.activiti.common.Constants;
import com.github.wuchao.activiti.domain.LeaveApplication;
import com.github.wuchao.activiti.repository.LeaveApplicationRepository;
import com.github.wuchao.activiti.repository.UserRepository;
import com.github.wuchao.activiti.service.dto.ActivitiTaskDTO;
import com.github.wuchao.activiti.service.dto.LeaveProcessDTO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveInfoService {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;


    /**
     * 查询用户的所有请假申请
     *
     * @param userId
     * @return
     */
    public List<LeaveApplication> leaves(Long userId) {
        List<LeaveApplication> leaveApplications =
                leaveApplicationRepository.findAllByApplicantIdAndDeletedIsFalseOrderByIdDesc(userId);
        if (leaveApplications == null) {
            leaveApplications = new ArrayList<>(0);
        }
        return leaveApplications;
    }


    /**
     * 查询用户请假流程信息
     *
     * @param leaveId
     * @return
     */
    public LeaveProcessDTO leaveProcess(Long leaveId) {
        LeaveProcessDTO leaveProcessDTO = new LeaveProcessDTO();

        leaveApplicationRepository.findTopById(leaveId)
                .map(leaveApplication -> {
                    leaveProcessDTO.setLeaveApplication(leaveApplication);
                    return leaveApplication;
                })
                .orElseGet(() -> {
                    leaveProcessDTO.setLeaveApplication(null);
                    return null;
                });

        List<ActivitiTaskDTO> tasks = workflowService
                .tasksWithVariables(Constants.LEAVE + "_" + leaveId);
        if (CollectionUtils.isEmpty(tasks)) {
            tasks = new ArrayList<>(0);
        }
        leaveProcessDTO.setTasks(tasks);

        return leaveProcessDTO;
    }

}
