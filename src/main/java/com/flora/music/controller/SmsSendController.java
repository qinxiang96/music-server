package com.flora.music.controller;

import com.flora.music.component.SmsComponent;
import com.flora.music.exception.BizCodeEnum;
import com.flora.music.utils.Consts;
import com.flora.music.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @Author qinxiang
 * @Date 2023/1/18-下午8:23
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @Autowired
    private SmsComponent smsComponent;
    @Autowired
    private StringRedisTemplate redisTemplate;
    // 发送短信
    @PostMapping("/sendcode")
    public R sendCode(HttpServletRequest request){
        String phone = request.getParameter("phone_num").trim();
        // 实现接口防刷
        String redisCode = redisTemplate.opsForValue().get(Consts.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            Long redisCodeTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - redisCodeTime < 60000) {
                // 60秒内不能再次发送
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        // 这种方式会带字母，不符合当前的短信内容格式
        // String code = UUID.randomUUID().toString().substring(0,6);
        String code = String.valueOf((int) (Math.random() * 900000 + 100000));
        // 验证码需要根据用户的提交进行校验，直接存在Redis
        redisTemplate.opsForValue().set(Consts.SMS_CODE_CACHE_PREFIX+phone,code+"_"+System.currentTimeMillis(),10, TimeUnit.MINUTES);


        smsComponent.sendSmsCode(phone,code);
        return R.ok();
    }
}
