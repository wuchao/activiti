package com.github.wuchao.activiti.controller;

import com.github.wuchao.activiti.security.CustomUser;
import com.github.wuchao.activiti.security.SecurityUtils;
import com.github.wuchao.activiti.service.LeaveInfoService;
import com.github.wuchao.activiti.service.dto.LeaveProcessDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class LeaveInfoController {

    @Autowired
    private LeaveInfoService leaveService;

    @GetMapping("/user/leaves")
    public ResponseEntity leavesOfUser() {
        CustomUser customUser = SecurityUtils.getCurrentUser();
        if (customUser != null) {
            return ResponseEntity.ok(leaveService.leaves(customUser.getUserId()));
        }
        return ResponseEntity.ok(new ArrayList<>(0));
    }

    @GetMapping("/user/leaves/{leaveId}")
    public ResponseEntity<LeaveProcessDTO> leaveProcess(@PathVariable("leaveId") Long leaveId) {
        CustomUser customUser = SecurityUtils.getCurrentUser();
        if (customUser != null) {
            return ResponseEntity.ok(leaveService.leaveProcess(leaveId));
        }
        return ResponseEntity.badRequest().build();
    }

}
