package com.flora.music.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @Author qinxiang
 * @Date 2023/1/20-下午12:31
 * 接收用户登录页面传入的数据
 */
@Data
public class ConsumerLoginVo {
    // 登录用户名
    @NotEmpty(message = "用户名不能为空")
    private String username;
    // 密码
    @NotEmpty(message = "密码必须填写")
    private String password;
}
