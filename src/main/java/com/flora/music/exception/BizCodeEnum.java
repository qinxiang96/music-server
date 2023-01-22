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
    SMS_CODE_EXCEPTION(10002,"短信验证码频率太高,请稍后再试"),
    SMS_CODE_VALID_EXCEPTION(10003,"短信验证码验证失败，请重试"),
    USER_EXIST_EXCEPTION(10004,"username or password is wrong"),
    COLLECT_SONG_EXIST_EXCEPTION(10005,"collect song is be collected already"),
    COLLECT_SONGLIST_EXIST_EXCEPTION(10006,"collect songList is be collected already"),
    COLLECT_SONG_EMPTY_EXCEPTION(10007,"collect song is empty"),
    COLLECT_SONGLIST_EMPTY_EXCEPTION(10008,"collect songList is empty");

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
