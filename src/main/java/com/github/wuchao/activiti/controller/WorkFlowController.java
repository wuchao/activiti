package com.github.wuchao.activiti.controller;

import com.github.wuchao.activiti.security.SecurityUtils;
import com.github.wuchao.activiti.service.UserTaskAndMessageService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WorkFlowController {

    @Autowired
    private UserTaskAndMessageService userTaskAndMessageService;

    @PostMapping("/user/userTasks/{userTaskId}/claim")
    public ResponseEntity claim(@PathVariable("userTaskId") Long userTaskId) {
        long userId = SecurityUtils.getCurrentUser().getUserId();
        userTaskAndMessageService.claim(userTaskId, userId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "流程回撤到前面任意一个 task 节点", notes = "流程回撤到前面任意一个 task 节点")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "taskId", value = "taskID", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "destinationTaskDefinitionKey", value = "打回到的目标任务节点的 taskDefinitionKey", required = true, dataType = "String", paramType = "query")
    })
    @PostMapping("/tasks/{taskId}/reverse")
    public ResponseEntity reverse(@PathVariable("taskId") String taskId,
                                  @RequestParam String destinationTaskDefinitionKey) {
        userTaskAndMessageService.reverse(taskId, destinationTaskDefinitionKey);
        return ResponseEntity.ok().build();
    }

}
