package com.github.wuchao.activiti.controller;

import com.github.wuchao.activiti.security.CustomUser;
import com.github.wuchao.activiti.security.SecurityUtils;
import com.github.wuchao.activiti.service.UserTaskAndMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class UserTaskController {

    @Autowired
    private UserTaskAndMessageService userTaskAndMessageService;

    @GetMapping("/user/userTasks")
    public ResponseEntity tasksOfUser() {
        CustomUser customUser = SecurityUtils.getCurrentUser();
        if (customUser != null) {
            return ResponseEntity.ok(userTaskAndMessageService.userTasks(customUser.getUserId()));
        }
        return ResponseEntity.ok(new ArrayList<>(0));
    }

    @GetMapping("user/userTasks/{userTaskId}")
    public ResponseEntity task(@PathVariable("userTaskId") Long userTaskId) {
        CustomUser customUser = SecurityUtils.getCurrentUser();
        if (customUser != null) {
            return ResponseEntity.ok(userTaskAndMessageService.userTask(userTaskId));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/userTasks/{userTaskId}/agree")
    public ResponseEntity agree(@PathVariable("userTaskId") Long userTaskId) {
        userTaskAndMessageService.agree(userTaskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/userTasks/{userTaskId}/disagree")
    public ResponseEntity disagree(@PathVariable("userTaskId") Long userTaskId) {
        userTaskAndMessageService.disagree(userTaskId);
        return ResponseEntity.ok().build();
    }

}
