package com.self.cloudserver.controller;

import com.self.cloudserver.utils.XxlJobUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 测试
 * @author zp
 */
@RestController
public class TestController {

    @Autowired
    private XxlJobUtils xxlJobUtils;

    @GetMapping("/test")
    public void test() throws Exception {
        String jobDesc = "测试任务1";
        String author = "dev";
        String alarmEmail = null;
        Date beginTime = null;
        Date endTime = null;
        String scheduleType = "FIX_RATE";
        String scheduleConf = "20";
        String executorHandler = "testJob";
        String executorParam = "{}";
        String childJobId = null;

        xxlJobUtils.addJob(jobDesc, author, alarmEmail, beginTime, endTime, scheduleType, scheduleConf, executorHandler, executorParam, childJobId);
    }

}
