package com.github.wuchao.activiti.repository;

import com.github.wuchao.activiti.domain.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTaskRepository extends JpaRepository<UserTask, Long> {

    List<UserTask> findAllByRelatedBusinessIdAndDeletedIsNull(Long relatedBusinessId);

    List<UserTask> findAllByAssignorIdAndDeletedIsNull(Long assignorId);

    Optional<UserTask> findByTaskIdAndAssignorIdAndDeletedIsNull(String taskId, Long assignorId);

    Optional<UserTask> findByTaskIdAndDeletedIsNull(String taskId);

    Optional<UserTask> findByIdAndDeletedIsNull(Long userTaskId);

}
