package com.flora.music.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @Author qinxiang
 * @Date 2023/1/21-下午5:26
 * RabbitMQ消息中间件的配置
 * 延迟队列的配置
 */
@Configuration
public class MyRabbitConfig3 {
    //测试
    // 首先在controller类的方法中给MQ发送消息，然后在到期后需要执行的方法上进行监听
    @RabbitListener(queues = "order.release.order.queue")
    public void listener(Message msg){
        System.out.println("");

    }
    /**
     * 死信队列
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        //String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments
        //            Queue(String name,  队列名字
        //            boolean durable,  是否持久化
        //            boolean exclusive,  是否排他
        //            boolean autoDelete, 是否自动删除
        //            Map<String, Object> arguments) 属性
        //x-dead-letter-exchange: order-event-exchange x-dead-letter-routing-key: order.release.order x-message-ttl: 60000
        HashMap<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange","order-event-exchange");
        args.put("x-dead-letter-routing-key","order.release.order");
        args.put("x-message-ttl",60000); // 消息过期时间 1分钟
        return new Queue("order.delay.queue",true,false,false,args);
    }
    /**
     * 普通队列
     *
     * @return
     */
    @Bean
    public Queue orderReleaseQueue() {

        Queue queue = new Queue("order.release.order.queue", true, false, false);

        return queue;
    }

    /**
     * TopicExchange
     *
     * @return
     */
    @Bean
    public TopicExchange orderEventExchange() {
        /*
         *   String name,
         *   boolean durable,
         *   boolean autoDelete,
         *   Map<String, Object> arguments
         * */
        return new TopicExchange("order-event-exchange", true, false);

    }


    @Bean
    public Binding orderCreateBinding() {
        /*
         * String destination, 目的地（队列名或者交换机名字）
         * DestinationType destinationType, 目的地类型（Queue、Exhcange）
         * String exchange,
         * String routingKey,
         * Map<String, Object> arguments
         * */
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    @Bean
    public Binding orderReleaseBinding() {

        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }

}
