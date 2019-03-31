package com.github.wuchao.activiti.repository;

import com.github.wuchao.activiti.domain.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {

    Optional<UserMessage> findByUserTaskIdAndDeletedIsNull(Long userTaskId);
}
