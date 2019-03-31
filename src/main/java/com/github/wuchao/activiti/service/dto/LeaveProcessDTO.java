package com.github.wuchao.activiti.service.dto;

import com.github.wuchao.activiti.domain.LeaveApplication;
import lombok.Data;

import java.util.List;

@Data
public class LeaveProcessDTO {

    /**
     * 请假流程节点信息
     */
    private List<ActivitiTaskDTO> tasks;

    private LeaveApplication leaveApplication;

}
