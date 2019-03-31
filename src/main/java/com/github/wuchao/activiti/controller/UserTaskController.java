package com.github.wuchao.activiti.controller;

import com.github.wuchao.activiti.security.CustomUser;
import com.github.wuchao.activiti.security.SecurityUtils;
import com.github.wuchao.activiti.service.UserTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class UserTaskController {

    @Autowired
    private UserTaskService userTaskService;

    @GetMapping("/user/userTasks")
    public ResponseEntity tasksOfUser() {
        CustomUser customUser = SecurityUtils.getCurrentUser();
        if (customUser != null) {
            return ResponseEntity.ok(userTaskService.userTasks(customUser.getUserId()));
        }
        return ResponseEntity.ok(new ArrayList<>(0));
    }

    @GetMapping("user/userTasks/{userTaskId}")
    public ResponseEntity task(@PathVariable("userTaskId") Long userTaskId) {
        CustomUser customUser = SecurityUtils.getCurrentUser();
        if (customUser != null) {
            return ResponseEntity.ok(userTaskService.userTask(userTaskId));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/userTasks/{userTaskId}/agree")
    public ResponseEntity agree(@PathVariable("userTaskId") Long userTaskId) {
        userTaskService.agree(userTaskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/userTasks/{userTaskId}/disagree")
    public ResponseEntity disagree(@PathVariable("userTaskId") Long userTaskId) {
        userTaskService.disagree(userTaskId);
        return ResponseEntity.ok().build();
    }

}
