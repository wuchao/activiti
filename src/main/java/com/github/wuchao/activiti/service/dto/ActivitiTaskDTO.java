package com.github.wuchao.activiti.service.dto;

import lombok.*;
import org.activiti.engine.runtime.ProcessInstance;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ActivitiTaskDTO implements Serializable {

    private String processDefinitionId;

    private String processInstanceId;

    private String executionId;

    private String taskId;

    private String taskDefinitionKey;

    private String taskName;

    private String owner;

    private String assignee;

    private Date createTime;

    private Date endTime;

    private String businessKey;

    private ProcessInstance processInstance;

    private Map processVariables;

    private Map taskLocalVariables;

}
