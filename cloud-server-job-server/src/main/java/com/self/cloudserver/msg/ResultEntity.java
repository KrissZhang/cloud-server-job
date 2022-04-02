package com.self.cloudserver.msg;

import java.io.Serializable;

/**
 * 通用返回对象包装
 * @param <T> T
 */
public class ResultEntity<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code = "200";

    private String msg = "成功";

    private String tip = "";

    private transient T data;

    public ResultEntity() {
    }

    public ResultEntity(T data) {
        this.data = data;
    }

    public ResultEntity(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultEntity(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResultEntity(String code, String msg, String tip, T data) {
        this.code = code;
        this.msg = msg;
        this.tip = tip;
        this.data = data;
    }

    public static <T> ResultEntity<T> addError(String code, String msg) {
        return new ResultEntity<>(code, msg);
    }

    public static <T> ResultEntity<T> addError(String code, String msg, T data) {
        return new ResultEntity<>(code, msg, data);
    }

    public static <T> ResultEntity<T> addError(String code, String msg, String tip, T data) {
        return new ResultEntity<>(code, msg, tip, data);
    }

    public static <T> ResultEntity<T> ok() {
        return new ResultEntity<>("200", "成功");
    }

    public static <T> ResultEntity<T> ok(T data) {
        ResultEntity<T> restResponse = new ResultEntity<>("200", "成功");
        restResponse.data = data;
        return restResponse;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTip() {
        return this.tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

}