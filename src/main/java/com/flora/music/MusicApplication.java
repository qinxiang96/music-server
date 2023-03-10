package com.flora.music;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 热更新 热加载
 * 1、command+shift+A-->搜索registry...,注意是带三个点的那个，然后找到compiler.automake.allow.when.app.running。勾选
 * 2、执行快捷键command+F9才能进行热加载
 */
@EnableRabbit
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
@MapperScan("com.flora.music.dao")
public class MusicApplication {

    public static void main(String[] args) {SpringApplication.run(MusicApplication.class, args);}

}
