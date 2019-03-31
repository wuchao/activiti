package com.github.wuchao.activiti.repository;

import com.github.wuchao.activiti.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findTopByUsername(String username);

}
