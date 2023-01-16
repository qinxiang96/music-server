package com.flora.music.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author qinxiang
 * @Date 2023/1/16-下午7:35
 * elasticsearch的配置
 * 当前配置的是本机的es 如果有多个，可以new多个，指定主机、端口号及协议名
 * 用@bean注入到容器
 */
@Configuration
public class ElasticsearchConfig {
    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }
    @Bean
    public RestHighLevelClient esRestClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                new HttpHost("localhost",9200,"http")
                ));
        return client;

    }
}
