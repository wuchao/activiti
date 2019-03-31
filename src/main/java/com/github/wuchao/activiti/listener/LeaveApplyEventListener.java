package com.github.wuchao.activiti.listener;

import com.github.wuchao.activiti.common.Constants;
import com.github.wuchao.activiti.domain.User;
import com.github.wuchao.activiti.domain.UserMessage;
import com.github.wuchao.activiti.domain.UserTask;
import com.github.wuchao.activiti.repository.LeaveApplicationRepository;
import com.github.wuchao.activiti.repository.UserMessageRepository;
import com.github.wuchao.activiti.repository.UserRepository;
import com.github.wuchao.activiti.repository.UserTaskRepository;
import com.github.wuchao.activiti.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LeaveApplyEventListener implements ActivitiEventListener {

    private UserRepository userRepository;

    private LeaveApplicationRepository leaveApplicationRepository;

    private UserTaskRepository userTaskRepository;

    private UserMessageRepository userMessageRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onEvent(ActivitiEvent event) {
        TaskEntityImpl task = (TaskEntityImpl) ((ActivitiEntityEventImpl) event).getEntity();

        // 任务创建成功，发送消息通知
        if (ActivitiEventType.TASK_CREATED.equals(event.getType())) {

            userRepository = (UserRepository) SpringContextUtil.getBean(UserRepository.class);
            userTaskRepository = (UserTaskRepository) SpringContextUtil.getBean(UserTaskRepository.class);
            userMessageRepository = (UserMessageRepository) SpringContextUtil.getBean(UserMessageRepository.class);
            leaveApplicationRepository = (LeaveApplicationRepository) SpringContextUtil.getBean(LeaveApplicationRepository.class);

            if (userRepository != null && userTaskRepository != null && userMessageRepository != null) {

                Long leaveApplicationId = (Long) task.getVariable(Constants.LEAVE_APPLICATION_ID);
                if (leaveApplicationId != null) {

                    Long[] assigneeId = new Long[1];
                    List<Long> candidateUserIds = new ArrayList<>();
                    if (StringUtils.isNotBlank(task.getAssignee())) {
                        assigneeId[0] = Long.valueOf(task.getAssignee());
                    } else if (CollectionUtils.isNotEmpty(task.getCandidates())) {
                        for (IdentityLink candidate : task.getCandidates()) {
                            candidateUserIds.add(Long.valueOf(candidate.getUserId()));
                        }
                    }

                    leaveApplicationRepository.findTopById(leaveApplicationId)
                            .ifPresent(app -> {

                                app.setStatus(task.getName());

                                if (ArrayUtils.isNotEmpty(assigneeId)) {

                                    // 个人任务
                                    UserTask userTask = new UserTask();
                                    userTask.setAssignorId(assigneeId[0]);
                                    userTask.setTaskId(task.getId());
                                    userTask.setTaskStep(task.getName());
                                    userTask.setTaskName("user1的" + app.getType() + "申请");
                                    userTask.setStatus(0);
                                    userTask.setPlanStartDate(LocalDateTime
                                            .ofInstant(task.getCreateTime().toInstant(), ZoneId.systemDefault()));
                                    userTask.setRelatedBusinessId(leaveApplicationId);
                                    userTaskRepository.save(userTask);

                                    // 个人消息
                                    UserMessage userMessage = new UserMessage();
                                    userMessage.setAccepterId(assigneeId[0]);
                                    userMessage.setContent(task.getName());
                                    userMessage.setStatus(0);
                                    userMessage.setUserTaskId(userTask.getId());
                                    userMessageRepository.save(userMessage);

                                } else if (CollectionUtils.isNotEmpty(candidateUserIds)) {

                                    List<User> users = userRepository.findAllByIdIn(candidateUserIds);
                                    if (CollectionUtils.isNotEmpty(users)) {

                                        // 个人任务
                                        UserTask userTask = new UserTask();
                                        userTask.setAssignorId(null);
                                        List<String> candidateUserIdStrs = candidateUserIds.stream()
                                                .map(id -> String.valueOf(id) + ',')
                                                .collect(Collectors.toList());
                                        userTask.setCandidateUsers(StringUtils.join(candidateUserIdStrs, ','));
                                        userTask.setTaskId(task.getId());
                                        userTask.setTaskStep(task.getName());
                                        userTask.setTaskName("user1的" + app.getType() + "申请");
                                        userTask.setStatus(0);
                                        userTask.setPlanStartDate(LocalDateTime
                                                .ofInstant(task.getCreateTime().toInstant(), ZoneId.systemDefault()));
                                        userTask.setRelatedBusinessId(leaveApplicationId);
                                        userTaskRepository.save(userTask);

                                        users.forEach(user -> {
                                            // 个人消息
                                            UserMessage userMessage = new UserMessage();
                                            userMessage.setAccepterId(user.getId());
                                            userMessage.setContent(task.getName());
                                            userMessage.setStatus(0);
                                            userMessage.setUserTaskId(userTask.getId());
                                            userMessageRepository.save(userMessage);
                                        });

                                    }
                                }
                            });

                    return;

                }
            }

            throw new RuntimeException("流程创建任务和消息失败。");
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

}
