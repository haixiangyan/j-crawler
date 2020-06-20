package com.github.monster;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MyBatisCrawlerDao implements CrawlerDao {
    private final static String MYBATIS_CONFIG_XML = "db/mybatis/config.xml";
    private SqlSessionFactory sqlSessionFactory;

    public MyBatisCrawlerDao() {
        try {
            InputStream inputStream = Resources.getResourceAsStream(MYBATIS_CONFIG_XML);
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized String loadOneUrl(int status) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return (String) session.selectOne("com.github.monster.Url.loadOneUrl");
        }
    }

    @Override
    public void insertArticle(String title, String content, String url) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.monster.Article.insertArticle", new Article(title, content, url));
        }
    }

    @Override
    public void insertUrl(String url) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.monster.Url.insertUrl", new Url(url, Crawler.READY));
        }
    }

    @Override
    public void updateUrlStatus(String url, int status) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.update("com.github.monster.Url.updateUrlStatus", new Url(url, status));
        }
    }

    @Override
    public boolean isUrlExist(String url) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Map<String, Object> param = new HashMap<>();
            param.put("url", url);

            return (Integer) session.selectOne("com.github.monster.Url.countUrl", param) != 0;
        }
    }

    @Override
    public boolean hasProcessed(String url) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Map<String, Object> param = new HashMap<>();
            param.put("url", url);
            param.put("status", Crawler.PROCESSED);

            return (Integer) session.selectOne("com.github.monster.Url.countUrl", param) != 0;
        }
    }
}
