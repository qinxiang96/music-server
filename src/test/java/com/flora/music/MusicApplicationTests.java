package com.flora.music;

import com.alibaba.fastjson.JSON;
import com.flora.music.config.ElasticsearchConfig;
import com.flora.music.domain.Singer;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@SpringBootTest
class MusicApplicationTests {
    @Autowired
    private RestHighLevelClient client;
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

}
