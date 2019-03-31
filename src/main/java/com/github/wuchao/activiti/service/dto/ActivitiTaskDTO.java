package com.github.wuchao.activiti.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ActivitiTaskDTO implements Serializable {

    @JsonIgnore
    private String processInstanceId;

    @JsonIgnore
    private String executionId;

    private String taskId;

    @JsonIgnore
    private String taskDefinitionKey;

    private String taskName;

    @JsonIgnore
    private String owner;

    @JsonIgnore
    private String assignee;

    private Date createTime;

    private Date endTime;

    @JsonIgnore
    private String businessKey;

    @JsonIgnore
    private ProcessInstance processInstance;

    @JsonIgnore
    private Map processVariables;

    @JsonIgnore
    private Map taskLocalVariables;

    public static ActivitiTaskDTO init(HistoricTaskInstance hti) {
        return ActivitiTaskDTO.builder()
                .processInstanceId(hti.getProcessInstanceId())
                .executionId(hti.getExecutionId())
                .taskId(hti.getId())
                .taskName(hti.getName())
                .taskDefinitionKey(hti.getTaskDefinitionKey())
                .owner(hti.getOwner())
                .assignee(hti.getAssignee())
                .createTime(hti.getCreateTime())
                .endTime(hti.getEndTime())
                .processVariables(hti.getProcessVariables())
                .taskLocalVariables(hti.getTaskLocalVariables())
                .build();
    }

    public static ActivitiTaskDTO init(Task hti) {
        return ActivitiTaskDTO.builder()
                .processInstanceId(hti.getProcessInstanceId())
                .executionId(hti.getExecutionId())
                .taskId(hti.getId())
                .taskName(hti.getName())
                .taskDefinitionKey(hti.getTaskDefinitionKey())
                .owner(hti.getOwner())
                .assignee(hti.getAssignee())
                .createTime(hti.getCreateTime())
                .processVariables(hti.getProcessVariables())
                .taskLocalVariables(hti.getTaskLocalVariables())
                .build();
    }

}
