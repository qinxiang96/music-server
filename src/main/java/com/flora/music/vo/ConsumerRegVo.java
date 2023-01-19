package com.flora.music.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * @Author qinxiang
 * @Date 2023/1/19-下午12:30
 * 接收用户注册页面传入的数据，用于验证
 * 注意这里的数据类型定义需要和前端传来的类型一致，名称也要一致
 *         String username = request.getParameter("username").trim();
 *         String password = request.getParameter("password").trim();
 *         String sex = request.getParameter("sex").trim();
 *         String phone_num = request.getParameter("phone_num").trim();
 *         String email = request.getParameter("email").trim();
 *         String birth = request.getParameter("birth").trim();
 *         String introduction = request.getParameter("introduction").trim();
 *         String location = request.getParameter("location").trim();
 *         String avator = request.getParameter("avator").trim();
 */
@Data
public class ConsumerRegVo {
    @NotEmpty(message = "用户名不能为空")
    //@Length(min = 6, max = 19, message="用户名长度在6-18字符")
    private String username;

    @NotEmpty(message = "密码必须填写")
    @Length(min = 6,max = 18,message = "密码必须是6—18位字符")
    private String password;

    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String phoneNum;

    @NotEmpty(message = "验证码不能为空")
    private String code;
    //性别
    private String sex;
    //邮箱
    private String email;
    //生日
    private String birth;
    //签名
    private String introduction;
    //地区
    private String location;
    //头像
    private String avator;
}
