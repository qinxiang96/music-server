package com.flora.music;

import com.alibaba.fastjson.JSON;
import com.flora.music.component.SmsComponent;
import com.flora.music.config.ElasticsearchConfig;
import com.flora.music.domain.Singer;
import com.flora.music.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class MusicApplicationTests {
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private SmsComponent smsComponent;
    // 获取操作elasticsearch的客户端
    @Test
    void contextLoads() {
        System.out.println(client);
    }
    // 添加
    @Test
    void indexData() throws IOException {
        Singer singer = new Singer();
        singer.setName("周杰伦");
        singer.setLocation("中国");
        singer.setIntroduction("华语乐坛天王");
        singer.setSex(new Byte("1"));
        String s = JSON.toJSONString(singer);
        IndexRequest indexRequest = new IndexRequest("singer");
        indexRequest.id("1");
        indexRequest.source(s, XContentType.JSON);
        IndexResponse index = client.index(indexRequest, ElasticsearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }
    //查询
    @Test
    void searchData() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("singer");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name","周杰伦"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = client.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);
        SearchHits hits = search.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit:searchHits){
//            Map<String, Object> map = hit.getSourceAsMap();
//            System.out.println(map);
            String sourceAsString = hit.getSourceAsString();
            // 把返回结果封装成Singer类
            Singer singer = JSON.parseObject(sourceAsString, Singer.class);
            System.out.println(singer);
        }
    }
    //测试短信发送-阿里云
    @Test
    void sendCode() {
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "762689c204704b2eb982e5647936edec";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", "15545334996");
        querys.put("param", "**code**:888888,**minute**:5");
        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodys = new HashMap<String, String>();


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //封装短信功能后进行测试
    @Test
    void sendMs(){
        smsComponent.sendSmsCode("15545334996","282531");

    }
    // 测试加密
    @Test
    void md(){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println(passwordEncoder.encode("zige"));
        System.out.println(passwordEncoder.encode("xiaopengyou"));
    }

}
