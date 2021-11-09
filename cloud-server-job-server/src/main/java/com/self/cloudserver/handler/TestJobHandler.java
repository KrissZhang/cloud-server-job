package com.self.cloudserver.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class TestJobHandler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RestTemplate restTemplate;

    @XxlJob(value = "testJobHandler")
    public void testJobHandler() throws Exception {
        //2.3.0统一只接受一个String类型的参数
        String param = XxlJobHelper.getJobParam();

        //打印之后的文件，在调度中心的调度日志管理中，操作选项下的执行日志可以查看到
        XxlJobHelper.log("===TestJobHandler=== testJobHandler");

        //执行返回结果可以在执行日志中查看到
        XxlJobHelper.handleSuccess("执行成功");
    }

    @XxlJob(value = "testHttpJobHandler")
    public void testHttpJobHandler() throws Exception {
        String param = XxlJobHelper.getJobParam();
        JSONObject object = JSON.parseObject(param);
        String req = Optional.ofNullable(object.getString("req")).orElse("");

        Map<String, String> map = new HashMap<>();
        req += ("|" + DATE_TIME_FORMATTER.format(LocalDateTime.now()));
        map.put("req", req);
        String url = "http://127.0.0.1:9000/rpc/cloud/server/two/test?req={req}";
        ResponseEntity<JSONObject> responseEntity = restTemplate.getForEntity(url, JSONObject.class, map);

        JSONObject result = responseEntity.getBody();
        String data = Optional.ofNullable(result.getString("data")).orElse("");

        XxlJobHelper.log("===testHttpJobHandler=== 获取返回结果：" + data);
        System.out.println("===testHttpJobHandler=== 获取返回结果：" + data);

        XxlJobHelper.handleSuccess("执行成功");
    }

}
