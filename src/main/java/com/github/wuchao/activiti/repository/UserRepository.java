package com.github.wuchao.activiti.repository;

import com.github.wuchao.activiti.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findTopByUsername(String username);

    Optional<User> findById(Long id);

    List<User> findAllByIdIn(List<Long> ids);

    List<User> findTopByUsernameLike(String usernameLike);

}
