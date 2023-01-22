package com.flora.music.controller;

import com.alibaba.fastjson.JSONObject;
import com.flora.music.domain.Consumer;
import com.flora.music.exception.BizCodeEnum;
import com.flora.music.service.ConsumerService;
import com.flora.music.utils.Consts;
import com.flora.music.utils.R;
import com.flora.music.vo.ConsumerLoginVo;
import com.flora.music.vo.ConsumerRegVo;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author qinxiang
 * @Date 2023/1/2-下午6:28
 */
@RestController
@RequestMapping("/consumer")
public class ConsumerController {
    @Autowired
    private ConsumerService consumerService;
    // 注意这里引用的是StringRedisTemplate,而不是RedisTemplate，否则获取不到值
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 新增用户
     * @param consumerRegVo
     * @return
     */
    //@ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public R addConsumer(@Valid ConsumerRegVo consumerRegVo, BindingResult result){
        // 处理数据格式校验的结果
        if (result.hasErrors()){
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return R.error().setData(errors);
        }
        // 校验用户名在数据库是否已存在
        String username = consumerRegVo.getUsername();
        Consumer econsumer = consumerService.selectByUsername(username);
        if (econsumer != null) {
            return R.error(0,"username already exists!");
        }
        // 校验验证码
        String phoneNum = consumerRegVo.getPhoneNum();
        String code = consumerRegVo.getCode();
        String redisCode = redisTemplate.opsForValue().get(Consts.SMS_CODE_CACHE_PREFIX + phoneNum);
        if (redisCode != null && redisCode.length() != 0) {
            String c = redisCode.split("_")[0];
            // 踩坑 注意字符串比较用.equals 因为string重写了equals方法，在内存地址不同的情况下比较值，内存地址不同用==的结果是false
            if (code.equals(c)){
                // 新增用户成功后，删除Redis中的验证码，防止后续新增过程出现问题，
                // 如果不删除缓存重新提交时在验证码有效期间可无需再发送验证码，但此时若用户重新获取了验证码，而Redis中数据没有更新时，会导致校验验证码失败
                // 因此选择在校验成功之后马上删除
                redisTemplate.delete(Consts.SMS_CODE_CACHE_PREFIX+phoneNum);
                // 转换接收的日期格式
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date birthDate = new Date();
                try {
                    birthDate = dateFormat.parse(consumerRegVo.getBirth());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Consumer consumer = new Consumer();
                consumer.setUsername(username);
                // 对密码进行加密保存
                String password = consumerRegVo.getPassword();
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                consumer.setPassword(passwordEncoder.encode(password));

                consumer.setSex(new Byte(consumerRegVo.getSex()));
                consumer.setPhoneNum(phoneNum);
                consumer.setEmail(consumerRegVo.getEmail());
                consumer.setBirth(birthDate);
                consumer.setIntroduction(consumerRegVo.getIntroduction());
                consumer.setLocation(consumerRegVo.getLocation());
                consumer.setAvator(consumerRegVo.getAvator());
                boolean flag = consumerService.insert(consumer);
                if (flag) {
                    return R.ok();
                }
                return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(),BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
            }
        }
        return R.error(BizCodeEnum.SMS_CODE_VALID_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_VALID_EXCEPTION.getMsg());
    }
    /**
     * 修改用户
     * @param request
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Object updateConsumer(HttpServletRequest request){
        JSONObject jsonObject = new JSONObject();
        String id = request.getParameter("id").trim();
        String username = request.getParameter("username").trim();
        String password = request.getParameter("password").trim();
        String sex = request.getParameter("sex").trim();
        String phone_num = request.getParameter("phone_num").trim();
        String email = request.getParameter("email").trim();
        String birth = request.getParameter("birth").trim();
        String introduction = request.getParameter("introduction").trim();
        String location = request.getParameter("location").trim();
        //把生日转换成Date格式
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date birthDate = new Date();
        try {
            birthDate = dateFormat.parse(birth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Consumer consumer = new Consumer();
        consumer.setId(Integer.parseInt(id));
        consumer.setUsername(username);
        consumer.setPassword(password);
        consumer.setSex(new Byte(sex));
        consumer.setPhoneNum(phone_num);
        consumer.setEmail(email);
        consumer.setBirth(birthDate);
        consumer.setIntroduction(introduction);
        consumer.setLocation(location);
        boolean flag = consumerService.update(consumer);
        if (flag){
            jsonObject.put(Consts.CODE, 1);
            jsonObject.put(Consts.MSG, "update successfully");
            return jsonObject;
        }
        jsonObject.put(Consts.CODE, 0);
        jsonObject.put(Consts.MSG,"update failed");
        return jsonObject;
    }
    /**
     * 删除用户
     * @param request
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public Object deleteConsumer(HttpServletRequest request){
        JSONObject jsonObject = new JSONObject();
        String id = request.getParameter("id").trim();
        boolean flag = consumerService.delete(Integer.parseInt(id));
        if (flag){
            jsonObject.put(Consts.CODE, 1);
            jsonObject.put(Consts.MSG, "delete successfully");
            return jsonObject;
        }
        jsonObject.put(Consts.CODE, 0);
        jsonObject.put(Consts.MSG,"delete failed");
        return jsonObject;
    }
    /**
     * 根据主键查询整个对象
     * @param request
     * @return
     */
    @RequestMapping(value = "/selectByPrimaryKey", method = RequestMethod.GET)
    public Object selectByPrimaryKey(HttpServletRequest request){
        String id = request.getParameter("id").trim();
        return consumerService.selectByPrimaryKey(Integer.parseInt(id));
    }
    /**
     * 查询所有用户
     * @param request
     * @return
     */
    @RequestMapping(value = "/allConsumer", method = RequestMethod.GET)
    public Object allConsumer(HttpServletRequest request){
        return consumerService.allConsumer();
    }
    /**
     * 根据账号查询列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/selectByUsername", method = RequestMethod.GET)
    public Object selectByUsername(HttpServletRequest request){
        String username = request.getParameter("username").trim();
        return consumerService.selectByUsername(username);
    }
    /**
     * 前端用户登录验证
     * @param consumerLoginVo
     * @return
     */
    @RequestMapping(value = "/verifyPassword", method = RequestMethod.POST)
    public R loginStatus(@Valid ConsumerLoginVo consumerLoginVo, BindingResult result){
        // 处理数据格式校验的结果
        if (result.hasErrors()){
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return R.error().setData(errors);
        }
        // 验证密码
        String username = consumerLoginVo.getUsername();
        String password = consumerLoginVo.getPassword();
        Consumer econsumer = consumerService.selectByUsername(username);
        BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
        boolean flag = bcryptPasswordEncoder.matches(password, econsumer.getPassword());
        // 因为需要拿到返回的对象信息，不用下面这种方式，否则需要查2遍数据库，直接根据用户名查询对象进行验证
        // boolean flag = consumerService.verifyPassword(username, password);
        // 处理返回信息
        if (flag){
            return R.ok().put("userMsg", econsumer);
        }
        return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(),BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
    }
    /**
     * 更新图片
     * @param avatorFile
     * @param id
     * @return
     */
    @RequestMapping(value = "/updatePic", method = RequestMethod.POST)
    public Object updatePic(@RequestParam("file") MultipartFile avatorFile, @RequestParam("id") int id){
        JSONObject jsonObject = new JSONObject();
        if (avatorFile.isEmpty()){
            jsonObject.put(Consts.CODE, 0);
            jsonObject.put(Consts.MSG, "upload pic failed");
            return jsonObject;
        }
        //文件名=当前时间到毫秒+原来的文件名
        String fileName = System.currentTimeMillis()+avatorFile.getOriginalFilename();
        //文件路径
        String filePath = System.getProperty("user.dir") + System.getProperty("file.separator")+"avatorImages";
        //如果文件路径不存在，新增该路径
        File file1 = new File(filePath);
        if (!file1.exists()){
            file1.mkdir();
        }
        //实际的文件地址
        File dest = new File(filePath + System.getProperty("file.separator") + fileName);
        //存储到数据库的相对文件地址
        String storeAvatorPath = "/avatorImages/"+fileName;
        try {
            avatorFile.transferTo(dest);
            Consumer consumer = new Consumer();
            consumer.setId(id);
            consumer.setAvator(storeAvatorPath);
            boolean flag = consumerService.update(consumer);
            if (flag){
                jsonObject.put(Consts.CODE, 1);
                jsonObject.put(Consts.MSG, "upload successfully");
                jsonObject.put("avator", storeAvatorPath);
                return jsonObject;
            }
            jsonObject.put(Consts.CODE, 0);
            jsonObject.put(Consts.MSG, "upload failed");
            return jsonObject;
        } catch (IOException e) {
            jsonObject.put(Consts.CODE, 0);
            jsonObject.put(Consts.MSG, "upload failed" + e.getMessage());
        }finally {
            return jsonObject;
        }
    }


}
