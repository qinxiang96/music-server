package com.flora.music.controller;

import com.alibaba.fastjson.JSONObject;
import com.flora.music.domain.Collect;
import com.flora.music.exception.BizCodeEnum;
import com.flora.music.service.CollectService;
import com.flora.music.utils.Consts;
import com.flora.music.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author qinxiang
 * @Date 2023/1/7-上午10:39
 */
@RestController
@RequestMapping("/collect")
public class CollectController {
    @Autowired
    private CollectService collectService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 增加收藏
     * @param request
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public R addCollect(HttpServletRequest request){
        JSONObject jsonObject = new JSONObject();
        String userId = request.getParameter("userId");
        String type = request.getParameter("type").trim();
        String songId = request.getParameter("songId");
        String songListId = request.getParameter("songListId");
        String collectToken = request.getParameter("collectToken");
        // 测试接口幂等性方案-token防重令牌
        // TODO 注意此处是模仿的提交订单，在确认订单页面到达之前，需要保证多次提交订单的幂等性问题，和这里原本想解决的减少查数据库不是一个问题，减少查询数据库，数据直接缓存在Redis即可
        // 目前逻辑的效果：收藏成功-设置token-再次点击-验证token后删除Redis-再次点击-查询数据库成功-设置token-再次点击-验证token后删除Redis-再次点击-查询数据库成功...
        String redisCollectToken = redisTemplate.opsForValue().get(Consts.CONSUMER_COLLECT_TOKEN_PREFIX + userId+"_"+songId);
        if (Integer.parseInt(type) == 0) {
            // 收藏类型为歌曲
            if (songId == null || songId.equals("")) {
                return R.error(BizCodeEnum.COLLECT_SONG_EMPTY_EXCEPTION.getCode(),BizCodeEnum.COLLECT_SONG_EMPTY_EXCEPTION.getMsg());
            } else if (collectToken != null && collectToken.equals(redisCollectToken)){
                //token验证通过，删除redis
                redisTemplate.delete(Consts.CONSUMER_COLLECT_TOKEN_PREFIX + userId);
                return R.error(BizCodeEnum.COLLECT_SONG_EXIST_EXCEPTION.getCode(),BizCodeEnum.COLLECT_SONG_EXIST_EXCEPTION.getMsg());
            }else if (collectService.selectByUserAndSongId(Integer.parseInt(userId), Integer.parseInt(songId))) {
                // 如果查询到收藏歌曲，缓存到Redis
                String token = UUID.randomUUID().toString().replace("-","");
                redisTemplate.opsForValue().set(Consts.CONSUMER_COLLECT_TOKEN_PREFIX+userId+"_"+songId,token,30, TimeUnit.MINUTES);
                return R.error(BizCodeEnum.COLLECT_SONG_EXIST_EXCEPTION.getCode(),BizCodeEnum.COLLECT_SONG_EXIST_EXCEPTION.getMsg()).put(Consts.CONSUMER_COLLECT_TOKEN,token);
            }else {
                Collect collect = new Collect();
                collect.setSongId(Integer.parseInt(songId));
                collect.setUserId(Integer.parseInt(userId));
                collect.setType(new Byte(type));
                boolean flag = collectService.insert(collect);
                if (flag) {
                    // 执行新增收藏歌曲
                    // TODO 如果想要收藏，设置防重令牌，这里应该是点击收藏之前放在提交数据中的，模拟如果收藏向提交订单一样需要执行复杂的业务逻辑，这时候连续点击收藏，带防重令牌去执行，执行结束后验证令牌，验证成功后删除Redis，这样其他请求就无法成功
                    String token = UUID.randomUUID().toString().replace("-","");
                    redisTemplate.opsForValue().set(Consts.CONSUMER_COLLECT_TOKEN_PREFIX+userId+"_"+songId,token,30, TimeUnit.MINUTES);

                    return R.ok().put(Consts.CONSUMER_COLLECT_TOKEN,token);
                }
            }
        } else {
            // 收藏类型为歌单
            if (songListId == null || songListId.equals("")) {
                return R.error(BizCodeEnum.COLLECT_SONGLIST_EMPTY_EXCEPTION.getCode(),BizCodeEnum.COLLECT_SONGLIST_EMPTY_EXCEPTION.getMsg());
            } else if (collectToken != null && collectToken.equals(redisCollectToken)){
                redisTemplate.delete(Consts.CONSUMER_COLLECT_TOKEN_PREFIX + userId);
                return R.error(BizCodeEnum.COLLECT_SONGLIST_EXIST_EXCEPTION.getCode(),BizCodeEnum.COLLECT_SONGLIST_EXIST_EXCEPTION.getMsg());
            }else if (collectService.selectByUserAndSongListId(Integer.parseInt(userId), Integer.parseInt(songListId))) {
                return R.error(BizCodeEnum.COLLECT_SONGLIST_EXIST_EXCEPTION.getCode(),BizCodeEnum.COLLECT_SONGLIST_EXIST_EXCEPTION.getMsg());
            }else {
                // 执行新增收藏歌单
                Collect collect = new Collect();
                collect.setSongListId(Integer.parseInt(songListId));
                collect.setUserId(Integer.parseInt(userId));
                collect.setType(new Byte(type));
                boolean flag = collectService.insert(collect);
                if (flag) {
                    // 如果收藏成功，设置防刷令牌
                    String token = UUID.randomUUID().toString().replace("-","");
                    redisTemplate.opsForValue().set(Consts.CONSUMER_COLLECT_TOKEN_PREFIX+userId,token,30, TimeUnit.MINUTES);
                    return R.ok().put(Consts.CONSUMER_COLLECT_TOKEN,token);
                }
            }
        }
        return R.error();

    }
    /**
     * 根据用户ID和歌曲ID删除收藏
     * @param request
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public Object deleteCollect(HttpServletRequest request){
        JSONObject jsonObject = new JSONObject();
        String userId = request.getParameter("userId").trim();
        String songId = request.getParameter("songId").trim();
        boolean flag = collectService.delete(Integer.parseInt(userId),Integer.parseInt(songId));
        if (flag) {
            jsonObject.put(Consts.CODE,1);
            jsonObject.put(Consts.MSG, "delete successfully");
            return jsonObject;
        }
        jsonObject.put(Consts.CODE,0);
        jsonObject.put(Consts.MSG, "delete failed");
        return jsonObject;
    }
    /**
     * 查询所有收藏
     * @param request
     * @return
     */
    @RequestMapping(value = "/allCollect", method = RequestMethod.GET)
    public Object allCollect(HttpServletRequest request){
        return collectService.allCollect();
    }
    /**
     * 查询某个用户的收藏列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/collectByUserId", method = RequestMethod.GET)
    public Object collectByUserId(HttpServletRequest request){
        String userId = request.getParameter("userId").trim();
        return collectService.selectByUserId(Integer.parseInt(userId));

    }
    /**
     * 查询某个用户是否已经收藏了某个歌单
     * @param request
     * @return
     */
    @RequestMapping(value = "/collectByUserIdAndSongListId", method = RequestMethod.GET)
    public Object collectByUserIdAndSongListId(HttpServletRequest request){
        String userId = request.getParameter("userId").trim();
        String songListId = request.getParameter("songListId").trim();
        return collectService.selectByUserAndSongListId(Integer.parseInt(userId),Integer.parseInt(songListId));

    }
}
