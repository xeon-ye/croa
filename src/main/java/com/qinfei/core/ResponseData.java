package com.qinfei.core;

import java.util.HashMap;
import java.util.Map;

public class ResponseData {

    private final String msg;
    private final int code;
    private final String success = "true";
    private final Map<String, Object> data = new HashMap<String, Object>();

    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public ResponseData putDataValue(String key, Object value) {
        data.put(key, value);
        return this;
    }

    private ResponseData(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getSuccess() {
        return success;
    }

    /**
     * 成功
     *
     * @return
     */
    public static ResponseData ok() {
        return new ResponseData(200, "Ok");
    }

    /**
     * 成功
     *
     * @return
     */
    public static ResponseData ok(Object o) {
        ResponseData ok = ok();
        ok.putDataValue("result", o);
        return ok;
    }

    /**
     * 成功
     *
     * @return
     */
    public static ResponseData ok(String message) {
        ResponseData ok = ok();
        ok.putDataValue("message", message);
        return ok;
    }

    /**
     * 找不到页面
     *
     * @return
     */
    public static ResponseData notFound() {
        return new ResponseData(404, "Not Found");
    }

    public static ResponseData badRequest() {
        return new ResponseData(400, "Bad Request");
    }

    /**
     * 没有权限访问
     *
     * @return
     */
    public static ResponseData forbidden() {
        return new ResponseData(403, "Forbidden");
    }

    public static ResponseData unauthorized() {
        return new ResponseData(401, "unauthorized");
    }

    /**
     * 未登录
     *
     * @return
     */
    public static ResponseData noLogin() {
        return new ResponseData(-1, "gged In");
    }

    /**
     * 内部服务器发生异常
     *
     * @return
     */
    public static ResponseData serverInternalError() {
        return new ResponseData(500, "Server Internal Error");
    }

    public static ResponseData customerError() {
        return new ResponseData(1001, "Customer Error");
    }

    public static ResponseData customerError(int code, String message) {
        return new ResponseData(code, message);
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "msg='" + msg + '\'' +
                ", code=" + code +
                ", success='" + success + '\'' +
                ", data=" + data +
                '}';
    }
}