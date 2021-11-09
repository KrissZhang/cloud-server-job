package com.self.cloudserver.handler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class TestJobHandler {

    @XxlJob(value = "testJobHandler")
    public void testJobHandler() throws Exception {
        //2.3.0统一只接受一个String类型的参数
        String param = XxlJobHelper.getJobParam();

        //打印之后的文件，在调度中心的调度日志管理中，操作选项下的执行日志可以查看到
        XxlJobHelper.log("===TestJobHandler=== testJobHandler");

        //执行返回结果可以在执行日志中查看到
        XxlJobHelper.handleSuccess("执行成功");
    }

}
