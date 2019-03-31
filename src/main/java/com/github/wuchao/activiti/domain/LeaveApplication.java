package com.github.wuchao.activiti.domain;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 请假申请
 */
@Data
@Entity
@Table(name = "leave_application")
public class LeaveApplication extends AbstractAuditingEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 请假类型
     */
    @Column(name = "leave_type")
    private String type;

    /**
     * 请假原因
     */
    @Column(name = "reason")
    private String reason;

    /**
     * 请假开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 请假结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 请假状态
     */
    @Column(name = "status")
    private String status;

    /**
     * 申请人 ID
     */
    @Column(name = "applicant_id")
    private Long applicantId;

    @ManyToOne
    @JoinColumn(name = "applicant_id", insertable = false, updatable = false)
    private User applicant;

}
