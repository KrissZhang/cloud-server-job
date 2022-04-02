package com.self.cloudserver.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.self.cloudserver.constants.CommonConstants;
import com.self.cloudserver.enums.RespCodeEnum;
import com.self.cloudserver.msg.ResultEntity;
import com.self.cloudserver.req.XxlJobInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.net.HttpCookie;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class XxlJobUtils {

    private static final Logger logger = LoggerFactory.getLogger(XxlJobUtils.class);

    private static List<HttpCookie> cookies;

    @Value("${xxl.job.admin.addresses:http://10.12.2.13:8099}")
    private String xxlJobAdminAddresses;

    @Value("${xxl.job.executor.appname:smart-build-executor-dev}")
    private String xxlJobExecutorAppname;

    @Value("${xxl.job.admin.userName:admin}")
    private String userName;

    @Value("${xxl.job.admin.password:123}")
    private String password;

    @Autowired
    private RedisUtils redisUtils;

    private List<HttpCookie> getCookieByStr(String cookieStr){
        List<HttpCookie> result = Lists.newArrayList();
        if(StringUtils.isBlank(cookieStr)){
            return result;
        }

        try{
            JSONArray array = JSONArray.parseArray(cookieStr);
            for (Object o : array) {
                JSONObject object = (JSONObject)o;
                HttpCookie httpCookie = new HttpCookie(object.getString("name"), object.getString("value"));
                httpCookie.setDiscard(object.getBooleanValue("discard"));
                httpCookie.setDomain(object.getString("domain"));
                httpCookie.setHttpOnly(object.getBooleanValue("httpOnly"));
                httpCookie.setMaxAge(object.getLongValue("maxAge"));
                httpCookie.setPath(object.getString("path"));
                httpCookie.setSecure(object.getBooleanValue("secure"));
                httpCookie.setVersion(object.getIntValue("version"));
                result.add(httpCookie);
            }
        }catch (Exception e){
            logger.error("Cookie转换失败：", e);
        }

        return result;
    }

    /**
     * 获取登陆cookie
     */
    private ResultEntity<Object> setCookie() throws Exception {
        try{
            //获取缓存的cookie(30分钟)
            String cookieStr = redisUtils.get(CommonConstants.XXL_JOB_COOKIE);
            if(StringUtils.isNotBlank(cookieStr)){
                cookies = getCookieByStr(cookieStr);
            }

            List<HttpCookie> cookieList = login();
            if(CollectionUtils.isEmpty(cookieList)){
                throw new RuntimeException("获取cookie失败");
            }

            cookieStr = JSON.toJSONString(cookieList);
            redisUtils.set(CommonConstants.XXL_JOB_COOKIE, cookieStr, 1800);
            cookies = cookieList;
        }catch (Exception e){
            logger.error("setCookie失败：", e);
            throw e;
        }

        return ResultEntity.ok();
    }

    /**
     * 登陆admin
     * @return cookie
     */
    private List<HttpCookie> login() throws Exception {
        try{
            String reqUrl = xxlJobAdminAddresses + "/login";
            Map<String, Object> map = new HashMap<>();
            map.put("userName", userName);
            map.put("password", password);

            HttpResponse httpResponse = HttpRequest.post(reqUrl).form(map).execute();
            return httpResponse.getCookies();
        }catch (Exception e){
            logger.error("登陆xxl-job admin失败：", e);
            throw e;
        }
    }

    /**
     * 获取jobGroupId
     * @param name jobGroupName
     * @return jobGroupId
     */
    private Integer getAppGroupIdByName(String name) throws Exception {
        if(StringUtils.isBlank(name)){
            return null;
        }

        try{
            //设置Cookie
            setCookie();

            Integer jobGroupId = null;
            String reqUrl = xxlJobAdminAddresses + "/jobgroup/loadByAppName";
            Map<String, Object> map = new HashMap<>();
            map.put("appName", name);

            HttpResponse httpResponse = HttpRequest.get(reqUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE).form(map).cookie(cookies).execute();
            if(httpResponse.getStatus() == HttpStatus.HTTP_OK){
                String respBody = httpResponse.body();
                JSONObject respBodyObj = JSON.parseObject(respBody);
                jobGroupId = respBodyObj.getJSONObject("content").getIntValue("id");
            }

            return jobGroupId;
        }catch (Exception e){
            logger.error("获取 jobGroupId 失败：", e);
            throw e;
        }
    }

    /**
     * 新增任务
     * @param jobDesc 任务描述
     * @param author 任务负责人
     * @param alarmEmail 告警邮件地址
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param scheduleType 调度类型
     * @param scheduleConf 调度配置
     * @param executorHandler 执行器
     * @param executorParam 执行参数，json格式
     * @param childJobId 子任务id，逗号分隔
     * @return 任务 jobId
     * @throws Exception Exception
     */
    public ResultEntity<Integer> addJob(String jobDesc, String author, String alarmEmail, Date beginTime, Date endTime, String scheduleType, String scheduleConf, String executorHandler, String executorParam, String childJobId) throws Exception {
        //校验参数
        if(StringUtils.isBlank(jobDesc)){
            throw new RuntimeException("任务描述不能为空");
        }

        if(StringUtils.isBlank(scheduleType)){
            throw new RuntimeException("调度类型不能为空");
        }

        if(StringUtils.isBlank(executorHandler)){
            throw new RuntimeException("执行器不能为空");
        }

        //获取jobGroupId
        Integer jobGroupId = getAppGroupIdByName(xxlJobExecutorAppname);
        if(jobGroupId == null){
            throw new RuntimeException("获取jobGroupId失败");
        }

        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        xxlJobInfo.setJobGroup(jobGroupId);
        xxlJobInfo.setJobDesc(jobDesc);
        xxlJobInfo.setAuthor(author);
        xxlJobInfo.setAlarmEmail(alarmEmail);
        xxlJobInfo.setBeginTime(beginTime);
        xxlJobInfo.setEndTime(endTime);
        xxlJobInfo.setScheduleType(scheduleType);
        xxlJobInfo.setScheduleConf(scheduleConf);
        xxlJobInfo.setMisfireStrategy("DO_NOTHING");
        xxlJobInfo.setExecutorRouteStrategy("FIRST");
        xxlJobInfo.setExecutorHandler(executorHandler);
        xxlJobInfo.setExecutorParam(executorParam);
        xxlJobInfo.setExecutorBlockStrategy("SERIAL_EXECUTION");
        xxlJobInfo.setExecutorTimeout(0);
        xxlJobInfo.setExecutorFailRetryCount(0);
        xxlJobInfo.setGlueType("BEAN");
        xxlJobInfo.setGlueSource("");
        xxlJobInfo.setGlueRemark("GLUE代码初始化");
        xxlJobInfo.setChildJobId(StringUtils.isBlank(childJobId) ? "" : childJobId);
        xxlJobInfo.setTriggerStatus(0);
        xxlJobInfo.setTriggerLastTime(0);
        xxlJobInfo.setTriggerNextTime(0);

        Integer jobId = null;

        String reqUrl = xxlJobAdminAddresses + "/jobinfo/add";

        Map<String, Object> xxlJobInfoMap = BeanUtil.beanToMap(xxlJobInfo);
        xxlJobInfoMap.put("beginTime", beginTime == null ? null : DateTimeUtils.dateToString(beginTime));
        xxlJobInfoMap.put("endTime", endTime == null ? null : DateTimeUtils.dateToString(endTime));
        HttpResponse httpResponse = HttpRequest.get(reqUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE).form(xxlJobInfoMap).cookie(cookies).execute();
        if(httpResponse.getStatus() == HttpStatus.HTTP_OK){
            String respBody = httpResponse.body();
            JSONObject respBodyObj = JSON.parseObject(respBody);
            jobId = respBodyObj.getIntValue("content");
        }

        return ResultEntity.ok(jobId);
    }

    /**
     * 启动任务
     * @param jobId 任务id
     * @return ResultEntity
     */
    public ResultEntity<Integer> startJob(Integer jobId) throws Exception {
        //校验参数
        if(jobId == null){
            throw new RuntimeException("任务id不能为空");
        }

        //设置Cookie
        setCookie();

        boolean result = false;

        String reqUrl = xxlJobAdminAddresses + "/jobinfo/start";
        Map<String, Object> map = new HashMap<>();
        map.put("id", jobId);

        HttpResponse httpResponse = HttpRequest.get(reqUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE).form(map).cookie(cookies).execute();
        JSONObject respBodyObj;
        if(httpResponse.getStatus() == HttpStatus.HTTP_OK){
            String respBody = httpResponse.body();
            respBodyObj = JSON.parseObject(respBody);
            result = (respBodyObj.getIntValue("code") == HttpStatus.HTTP_OK);
        }

        return result ? ResultEntity.ok(jobId) : ResultEntity.addError(RespCodeEnum.FAIL_SYS.getCode(), "任务启动失败");
    }

    /**
     * 批量启动任务
     * @param jobIds 任务id
     * @return ResultEntity
     */
    public ResultEntity<List<Integer>> batchStartJob(List<Integer> jobIds) throws Exception {
        //校验参数
        if(CollectionUtils.isEmpty(jobIds)){
            throw new RuntimeException("任务id不能为空");
        }

        //设置Cookie
        setCookie();

        boolean result = false;

        String reqUrl = xxlJobAdminAddresses + "/jobinfo/batchStart";
        Map<String, Object> map = new HashMap<>();
        String ids = StringUtils.join(jobIds, ",");
        map.put("ids", ids);

        HttpResponse httpResponse = HttpRequest.get(reqUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE).form(map).cookie(cookies).execute();
        JSONObject respBodyObj;
        if(httpResponse.getStatus() == HttpStatus.HTTP_OK){
            String respBody = httpResponse.body();
            respBodyObj = JSON.parseObject(respBody);
            result = (respBodyObj.getIntValue("code") == HttpStatus.HTTP_OK);
        }

        return result ? ResultEntity.ok(jobIds) : ResultEntity.addError(RespCodeEnum.FAIL_SYS.getCode(), "任务启动失败");
    }

    /**
     * 新增并启动任务
     * @param jobDesc 任务描述
     * @param author 任务负责人
     * @param alarmEmail 告警邮件地址
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param scheduleType 调度类型
     * @param scheduleConf 调度配置
     * @param executorHandler 执行器
     * @param executorParam 执行参数，json格式
     * @param childJobId 子任务id，逗号分隔
     * @return 任务 jobId
     * @throws Exception Exception
     */
    public ResultEntity<Integer> addAndStartJob(String jobDesc, String author, String alarmEmail, Date beginTime, Date endTime, String scheduleType, String scheduleConf, String executorHandler, String executorParam, String childJobId) throws Exception {
        ResultEntity<Integer> resultEntity = addJob(jobDesc, author, alarmEmail, beginTime, endTime, scheduleType, scheduleConf, executorHandler, executorParam, childJobId);
        return startJob(resultEntity.getData());
    }

    /**
     * 停止任务
     * @param jobId 任务id
     * @return true-成功，false-失败
     */
    public ResultEntity<Object> stopJob(Integer jobId) throws Exception {
        //校验参数
        if(jobId == null){
            throw new RuntimeException("任务id不能为空");
        }

        //设置Cookie
        setCookie();

        String reqUrl = xxlJobAdminAddresses + "/jobinfo/stop";
        Map<String, Object> map = new HashMap<>();
        map.put("id", jobId);

        HttpResponse httpResponse = HttpRequest.get(reqUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE).form(map).cookie(cookies).execute();
        return httpResponse.getStatus() == HttpStatus.HTTP_OK ? ResultEntity.ok() : ResultEntity.addError(RespCodeEnum.FAIL_SYS.getCode(), "任务停止失败");
    }

    /**
     * 批量停止任务
     * @param jobIds 任务id
     * @return true-成功，false-失败
     */
    public ResultEntity<Object> batchStopJob(List<Integer> jobIds) throws Exception {
        //校验参数
        if(CollectionUtils.isEmpty(jobIds)){
            throw new RuntimeException("任务id不能为空");
        }

        //设置Cookie
        setCookie();

        String reqUrl = xxlJobAdminAddresses + "/jobinfo/batchStop";
        Map<String, Object> map = new HashMap<>();
        String ids = StringUtils.join(jobIds, ",");
        map.put("ids", ids);

        HttpResponse httpResponse = HttpRequest.get(reqUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE).form(map).cookie(cookies).execute();
        return httpResponse.getStatus() == HttpStatus.HTTP_OK ? ResultEntity.ok() : ResultEntity.addError(RespCodeEnum.FAIL_SYS.getCode(), "任务停止失败");
    }

    /**
     * 删除任务
     * @param jobId 任务id
     * @return true-成功，false-失败
     */
    public ResultEntity<Object> removeJob(Integer jobId) throws Exception {
        //校验参数
        if(jobId == null){
            throw new RuntimeException("任务id不能为空");
        }

        //设置Cookie
        setCookie();

        String reqUrl = xxlJobAdminAddresses + "/jobinfo/remove";
        Map<String, Object> map = new HashMap<>();
        map.put("id", jobId);

        HttpResponse httpResponse = HttpRequest.get(reqUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE).form(map).cookie(cookies).execute();
        return httpResponse.getStatus() == HttpStatus.HTTP_OK ? ResultEntity.ok() : ResultEntity.addError(RespCodeEnum.FAIL_SYS.getCode(), "任务删除失败");
    }

    /**
     * 批量删除任务
     * @param jobIds 任务id
     * @return true-成功，false-失败
     */
    public ResultEntity<Object> batchRemoveJob(List<Integer> jobIds) throws Exception {
        //校验参数
        if(CollectionUtils.isEmpty(jobIds)){
            throw new RuntimeException("任务id不能为空");
        }

        //设置Cookie
        setCookie();

        String reqUrl = xxlJobAdminAddresses + "/jobinfo/batchRemove";
        Map<String, Object> map = new HashMap<>();
        String ids = StringUtils.join(jobIds, ",");
        map.put("ids", ids);

        HttpResponse httpResponse = HttpRequest.get(reqUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE).form(map).cookie(cookies).execute();
        return httpResponse.getStatus() == HttpStatus.HTTP_OK ? ResultEntity.ok() : ResultEntity.addError(RespCodeEnum.FAIL_SYS.getCode(), "任务删除失败");
    }

    /**
     * 停止并删除任务
     * @param jobId 任务id
     * @return true-成功，false-失败
     * @throws Exception Exception
     */
    public ResultEntity<Object> stopAndRemoveJob(Integer jobId) throws Exception {
        boolean result = false;
        boolean stopStatus = (String.valueOf(HttpStatus.HTTP_OK).equals(stopJob(jobId).getCode()));
        if(stopStatus){
            result = (String.valueOf(HttpStatus.HTTP_OK).equals(removeJob(jobId).getCode()));
        }

        return result ? ResultEntity.ok() : ResultEntity.addError(RespCodeEnum.FAIL_SYS.getCode(), "任务删除失败");
    }

}
