package com.github.monster;

import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSource {
    public static void main(String[] args) {
        String MYBATIS_CONFIG_XML = "db/mybatis/config.xml";

        try {
            InputStream inputStream = Resources.getResourceAsStream(MYBATIS_CONFIG_XML);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

            List<Article> articles = getArticles(sqlSessionFactory);

            for (int i = 0; i < 10; i++) {
                new Thread(() -> injectToES(articles)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Article> getArticles(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.github.monster.MockMapper.selectArticleSeeds");
        }
    }

    private static void injectToES(List<Article> articles) {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            BulkRequest bulkRequest = new BulkRequest();
            for (Article article : articles) {
                IndexRequest request = new IndexRequest("articles");


                Map<String, Object> document = new HashMap<>();
                document.put("url", article.getUrl());
                document.put("title", article.getTitle());
                document.put("content", article.getContent());
                document.put("createdAt", article.getCreatedAt());
                document.put("updatedAt", article.getUpdatedAt());

                request.source(document, XContentType.JSON);

                bulkRequest.add(request);
            }
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println(Thread.currentThread().getName() + " " + bulkResponse.status().getStatus());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
