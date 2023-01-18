package com.flora.music.exception;

/**
 * @Author qinxiang
 * @Date 2023/1/18-下午11:32
 * 记录异常错误码返回
 * 错误码列表：
 * 10：通用 001：参数格式校验
 * 11：其他服务
 */
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002,"短信验证码频率太高,请稍后再试");

    private int code;
    private String msg;
    BizCodeEnum(int code, String msg){
        this.code = code;
        this.msg = msg;
    }
    public int getCode(){
        return code;
    }
    public String getMsg(){
        return msg;
    }
}
