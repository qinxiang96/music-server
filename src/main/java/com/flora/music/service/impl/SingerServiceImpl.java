package com.flora.music.service.impl;

import com.flora.music.dao.SingerMapper;
import com.flora.music.domain.Singer;
import com.flora.music.service.SingerService;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @Author qinxiang
 * @Date 2022/12/30-下午2:27
 */
@RabbitListener(queues = {"hello-queue"})
@Service
public class SingerServiceImpl implements SingerService {
    @Autowired
    private SingerMapper singerMapper;
    /**
     * 增加
     *
     * @param singer
     * @return
     */
    @CacheEvict(value = "singer",allEntries = true)
    @Override
    public boolean insert(Singer singer) {
        return singerMapper.insert(singer)>0;
    }

    /**
     * 更新
     *
     * @param singer
     * @return
     */
    @CacheEvict(value = "singer",allEntries = true)
    @Override
    public boolean update(Singer singer) {
        return singerMapper.update(singer)>0;
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @CacheEvict(value = "singer",allEntries = true)
    @Override
    public boolean delete(Integer id) {
        return singerMapper.delete(id)>0;
    }

    /**
     * 根据主键查询整个对象
     *
     * @param id
     * @return
     */
    @Override
    public Singer selectByPrimaryKey(Integer id) {
        return singerMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询所有歌手
     *
     * @return
     */
    @Cacheable(value = {"singer"},key = "#root.method.name",sync = true)
    @Override
    public List<Singer> allSinger() {
        return singerMapper.allSinger();
    }

    /**
     * 根据歌手名字查询列表
     *
     * @param name
     * @return
     */
    @Override
    public List<Singer> selectByName(String name) {
        return singerMapper.selectByName(name);
    }

    /**
     * 根据性别查询列表
     *
     * @param sex
     * @return
     */
    @Override
    public List<Singer> selectBySex(Integer sex) {
        return singerMapper.selectBySex(sex);
    }

    /**
     * 更新歌手图片
     *
     * @param id
     * @return
     */
    @Override
    public boolean updatePic(Integer id){return singerMapper.updatePic(id)>0;};

    // @RabbitListener 可以标注在类和方法上，但只能通过Message接收消息
    // @RabbitHandler 可以标注方法上，配合@RabbitListener(queues = {"hello-queue"})标注在类上，对不同的消息类型标注在不同的方法上进行不同的处理
    // queues:声明需要监听的所有队列
//    @RabbitListener(queues = {"hello-queue"})
//    public void listenMsg(Message msg){
//        byte[] body = msg.getBody();
//        String s = new String(body);
//        System.out.println("监听到的消息内容"+s+"监听到的原消息内容"+body);
//        监听到的消息内容{"id":null,"name":"xiaopengyou","sex":null,"pic":null,"birth":1674296295163,"location":null,"introduction":null}监听到的原消息内容[B@1b99cec9
//    }
    @RabbitHandler
    public void listenMsg2(Message message, Singer msg, Channel channel){
        System.out.println("监听到的消息内容"+msg);
        // 监听到的消息内容Singer(id=null, name=xiaopengyou, sex=null, pic=null, birth=Sat Jan 21 19:35:32 CST 2023, location=null, introduction=null)
        // Channel内按顺序自增-deliveryTag
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            // 签收信息 不批量签收（业务成功，签收）
            channel.basicAck(deliveryTag,false);
            // 拒签信息 不批量拒签（业务失败，拒签，信息可被重新处理）
            // channel.basicNack(deliveryTag,false,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
