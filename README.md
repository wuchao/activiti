# Activiti 流程引擎

## 初识 Activiti
### 工作流与工作流引擎
工作流（workflow）就是工作流程的计算模型，即将工作流程中的工作如何前后组织在一起的逻辑和规则在计算机中以恰当的模型进行表示并对其实施计算。它主要解决的是“使在多个参与者之间按照某种预定义的规则传递文档、信息或任务的过程自动进行，从而实现某个预期的业务目标，或者促使此目标的实现”。（我的理解就是：将部分或者全部的工作流程、逻辑让计算机帮你来处理，实现自动化）

所谓工作流引擎是指 workflow 作为应用系统的一部分，并为之提供对各应用系统有决定作用的根据角色、分工和条件的不同决定信息传递路由、内容等级等核心解决方案。

例如开发一个系统最关键的部分不是系统的界面，也不是和数据库之间的信息交换，而是如何根据业务逻辑开发出符合实际需要的程序逻辑并确保其稳定性、易维护性和弹性。

比如你的系统中有一个任务流程，一般情况下这个任务的代码逻辑、流程你都要自己来编写。实现它是没有问题的。但是谁能保证逻辑编写的毫无纰漏？经过无数次的测试与改进，这个流程没有任何漏洞也是可以实现的，但是明显就会拖慢整个项目的进度。

工作流引擎解决的就是这个问题：如果应用程序缺乏强大的逻辑层，势必变得容易出错（信息的路由错误、死循环等等）。

Activiti 是一个开源的工作流引擎，它实现了BPMN 2.0 规范，可以发布设计好的流程定义，并通过 api 进行流程调度。

## Activiti 核心组件介绍
### 关键对象
- Deployment：流程部署对象，部署一个流程时创建。

- ProcessDefinition：流程定义，部署成功后自动创建。

- ProcessInstance：流程实例，启动流程时创建。

- Task：任务，在 Activiti 中的 Task 仅指有角色参与的任务，即定义中的 UserTask。

- Execution：代表分支的执行线，与分支是一对一的关系。

### Service API
- ProcessEngine：流程引擎的抽象，通过它我们可以获得我们需要的一切服务。

- RepositoryService：Activiti 中每一个不同版本的业务流程的定义都需要使用一些定义文件，部署文件和支持数据(例如 BPMN2.0 XML文件，表单定义文件，流程定义图像文件等)，这些文件都存储在 Activiti 内建的 Repository 中。RepositoryService 提供了对 repository 的存取服务。

- RuntimeService：在 Activiti 中，每当一个流程定义被启动一次之后，都会生成一个相应的流程对象实例。RuntimeService 提供了启动流程、查询流程实例、设置获取流程实例变量等功能。此外它还提供了对流程部署，流程定义和流程实例的存取服务。

- TaskService: 在 Activiti 中业务流程定义中的每一个执行节点被称为一个 Task，对流程中的数据存取，状态变更等操作均需要在Task中完成。TaskService 提供了对用户 Task 和 Form 相关的操作。它提供了运行时任务查询、领取、完成、删除以及变量设置等功能。

- IdentityService: Activiti 中内置了用户以及组管理的功能，必须使用这些用户和组的信息才能获取到相应的 Task。IdentityService 提供了对 Activiti 系统中的用户和组的管理功能。

- ManagementService: ManagementService 提供了对 Activiti 流程引擎的管理和维护功能，这些功能不在工作流驱动的应用程序中使用，主要用于 Activiti 系统的日常维护。

- HistoryService: HistoryService 用于获取正在运行或已经完成的流程实例的信息，与 RuntimeService 中获取的流程信息不同，历史信息包含已经持久化存储的永久信息，并已经被针对查询优化。

### 事件监听器
#### ActivitiEventListener 
> [ActivitiEventListener](https://www.cnblogs.com/jimboi/p/8470134.html)

#### ExecutionListener
> [执行监听器（ Execution listener）](https://www.cnblogs.com/jimboi/p/8472623.html)

#### TaskListener
> [任务侦听器（Task listener）](https://www.cnblogs.com/jimboi/p/8477014.html)

ExecutionListener 和 TaskListener 事件的执行顺序：
- ExecutionListener#start
- TaskListener#create
- TaskListener#{assignment}*
- TaskListener#{complete, delete}
- ExecutionListener#end

### 数据库表
Activiti 工作流总共包含 23 张数据表，这些表的表名默认以 `ACT_` 开头。并且表名的第二部分用两个字母表明表的用例，而这个用例也基本上跟 Service API 匹配。详见 [Activiti 数据库表结构(表详细版)](https://blog.csdn.net/hj7jay/article/details/51302829)。


## 代码案例
### 案例说明
以请假为例，用户 1 发起请假流程，然后让所有 hr 批准，只要有一个 hr 批准通过，就进入下一个审批环节，否则直接关闭该请假申请；hr 同意后，
再让所有 manager 批准，并且要求所有 manager 批准通过后才通过请假申请，否则也直接关闭该请假申请。

### 设计请假流程的流程图
使用官方打的包（[activiti-app](https://github.com/Activiti/Activiti/releases/download/activiti-6.0.0/activiti-6.0.0.zip)），在 tomcat 中运行起来，然后来画流程图，具体参考：[activiti入门-activiti官方应用使用](https://blog.csdn.net/yongboyhood/article/details/70833021)。

画好流程图后，导出到项目资源文件夹下的 `processes` 文件夹中（Activiti 默认目录），重命名为 `leave.bpmn`。

参考：
> [Activiti 数据库表结构](https://www.devdoc.cn/activiti-table-summary.html)
> 
> [Activiti工作流引擎使用](https://blog.csdn.net/xwnxwn/article/details/52303862)
