package com.github.wuchao.activiti.controller;

import com.github.wuchao.activiti.security.SecurityUtils;
import com.github.wuchao.activiti.service.UserTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WorkFlowController {

    @Autowired
    private UserTaskService userTaskService;

    @PostMapping("/user/tasks/{taskId}/claim")
    public ResponseEntity claim(@PathVariable("taskId") Long taskId) {
        long userId = SecurityUtils.getCurrentUser().getUserId();
        userTaskService.claim(taskId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/tasks/{taskId}/reverse")
    public ResponseEntity reverse(@PathVariable("taskId") Long taskId) {
        userTaskService.reverse(taskId);
        return ResponseEntity.ok().build();
    }

}
