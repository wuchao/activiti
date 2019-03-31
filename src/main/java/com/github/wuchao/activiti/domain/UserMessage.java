package com.github.wuchao.activiti.domain;

import lombok.Data;

import javax.persistence.*;

/**
 * 用户消息
 */
@Data
@Entity
@Table(name = "user_message")
public class UserMessage extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 消息接收人
     */
    @Column(name = "accepter_id")
    private Long accepterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepter_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User accepter;

    /**
     * 消息内容
     */
    @Column(name = "content")
    private String content;

    /**
     * 已读状态（1表示已读，0表示未读）
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 关联的个人任务
     */
    @Column(name = "user_task_id")
    private Long userTaskId;

    @OneToOne
    @JoinColumn(name = "user_task_id", insertable = false, updatable = false)
    private UserTask userTask;

}
