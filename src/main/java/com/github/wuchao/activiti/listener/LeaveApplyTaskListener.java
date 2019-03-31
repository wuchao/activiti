package com.github.wuchao.activiti.listener;

import com.github.wuchao.activiti.common.Constants;
import com.github.wuchao.activiti.domain.User;
import com.github.wuchao.activiti.repository.UserRepository;
import com.github.wuchao.activiti.repository.UserTaskRepository;
import com.github.wuchao.activiti.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component("leaveApplyTaskListener")
public class LeaveApplyTaskListener implements TaskListener {

    final String EVENTNAME_CREATE = "create";
    final String EVENTNAME_ASSIGNMENT = "assignment";
    final String EVENTNAME_COMPLETE = "complete";
    final String EVENTNAME_DELETE = "delete";

    private UserRepository userRepository;

    private UserTaskRepository userTaskRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notify(DelegateTask delegateTask) {
        log.debug("task event: " + delegateTask.getEventName());

        userRepository = (UserRepository) SpringContextUtil.getBean(UserRepository.class);
        userTaskRepository = (UserTaskRepository) SpringContextUtil.getBean(UserTaskRepository.class);

        if (EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
            // hr 审批流程设置任务执行候选人，任务需要认领（claim）
            if (Constants.APPLY_HRS_APPROVE.equals(delegateTask.getTaskDefinitionKey())) {
                List<User> users = userRepository.findTopByUsernameLike("hr%");
                if (CollectionUtils.isNotEmpty(users)) {
                    List<String> userIds = users.stream()
                            .map(u -> String.valueOf(u.getId()))
                            .collect(Collectors.toList());
                    delegateTask.addCandidateUsers(userIds);
                }
            }

            // manager 审批流程设置任务执行人
            if (Constants.APPLY_MANAGERS_APPROVE.equals(delegateTask.getTaskDefinitionKey())) {
                List<User> users = userRepository.findTopByUsernameLike("manager%");
                if (CollectionUtils.isNotEmpty(users)) {
                    delegateTask.setVariable("managerIds", users.stream()
                            .map(u -> String.valueOf(u.getId())).collect(Collectors.toList()));
                }
            }

        } else if (EVENTNAME_COMPLETE.equals(delegateTask.getEventName())) {

            // 修改 UserTask 状态
            Long assignorId = StringUtils.isNotBlank(delegateTask.getAssignee()) ?
                    Long.valueOf(delegateTask.getAssignee()) : Long.valueOf(delegateTask.getOwner());
            if (assignorId != null) {
                userTaskRepository.findByTaskIdAndAssignorIdAndDeletedIsNull(delegateTask.getId(), assignorId).ifPresent(userTask -> {
                    userTask.setStatus(1);
                    userTask.setFinishedDate(LocalDateTime.now());
                });
            }
        }
    }
}
