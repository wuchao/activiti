package com.github.wuchao.activiti.service.dto;

import com.github.wuchao.activiti.domain.LeaveApplication;
import lombok.Data;

@Data
public class LeaveApplyDTO {

    private LeaveApplication leaveApplication;

    private Long leaderId;

}
