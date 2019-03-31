package com.github.wuchao.activiti.controller;

import com.github.wuchao.activiti.security.CustomUser;
import com.github.wuchao.activiti.security.SecurityUtils;
import com.github.wuchao.activiti.service.LeaveApplyService;
import com.github.wuchao.activiti.service.dto.LeaveApplyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 请假申请
 */
@RestController
@RequestMapping("/api")
public class LeaveApplyController {

    @Autowired
    private LeaveApplyService leaveApplyService;

    @PostMapping("/leave")
    public ResponseEntity applyForLeave(@RequestBody LeaveApplyDTO leaveApplyDTO) {
        CustomUser customUser = SecurityUtils.getCurrentUser();
        if (customUser != null) {
            leaveApplyService.applyForLeave(customUser.getUserId(), leaveApplyDTO);
        }
        return ResponseEntity.ok().build();
    }

}
