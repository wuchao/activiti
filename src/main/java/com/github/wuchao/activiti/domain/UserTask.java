package com.github.wuchao.activiti.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户任务
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "user_task")
public class UserTask extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 任务步骤
     */
    @Column(name = "task_step")
    private String taskStep;

    /**
     * 任务名称
     */
    @Column(name = "task_name")
    private String taskName;

    /**
     * 任务类型
     */
    @Column(name = "task_type")
    private String taskType;

    /**
     * 流程任务ID
     */
    @Column(name = "task_id")
    private String taskId;

    /**
     * 任务指派人ID
     */
    @Column(name = "assignor_id")
    private Long assignorId;

    /**
     * 任务指派人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignor_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User assignor;

    @Column(name = "candidate_users")
    private String candidateUsers;

    @Column(name = "related_business_id")
    private Long relatedBusinessId;

    /**
     * 计划开始时间
     */
    @Column(name = "plan_start_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime planStartDate;

    /**
     * 计划结束时间
     */
    @Column(name = "plan_end_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime planEndDate;

    /**
     * 任务实际完成时间
     */
    @Column(name = "finished_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime finishedDate;

    /**
     * 处理状态（0 未处理，1 处理，2 撤销）
     */
    @Column(name = "status")
    private Integer status;

}
