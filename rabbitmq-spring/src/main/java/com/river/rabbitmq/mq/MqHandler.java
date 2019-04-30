package com.river.rabbitmq.mq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2018/12/23.
 */
@Configuration
public class MqHandler {

    @Autowired
    RabbitTemplate template;

    @Bean
    public Queue qu(){
        System.out.println("first");
        return new Queue("first");
    }

    @Bean
    @ConditionalOnBean(name = "qu")
    public void push(){
        System.out.println("second");
        template.convertAndSend("first","message");
    }

    @RabbitListener(queues = {"first"})
    public void handler(Message message) throws UnsupportedEncodingException {
        System.out.println("getmessage"+new String(message.getBody(),"UTF-8"));
    }
}
