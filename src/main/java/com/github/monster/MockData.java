package com.github.monster;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

public class MockData {
    private static final int TARGET_ROW_COUNT = 100_0000;

    public static void main(String[] args) {
        String MYBATIS_CONFIG_XML = "db/mybatis/config.xml";

        try {
            InputStream inputStream = Resources.getResourceAsStream(MYBATIS_CONFIG_XML);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

            mock(sqlSessionFactory, TARGET_ROW_COUNT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void mock(SqlSessionFactory sqlSessionFactory, int targetCount) {
        Random random = new Random();

        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<Article> articles = session.selectList("com.github.monster.MockMapper.selectArticleSeeds");

            int count = targetCount - articles.size();

            try {
                while (count-- > 0) {
                    int index = random.nextInt(articles.size());
                    Article seed = new Article(articles.get(index));

                    Instant randomTime = seed.getCreatedAt().minusSeconds(random.nextInt(3600 * 24 * 365));

                    seed.setCreatedAt(randomTime);
                    seed.setUpdatedAt(randomTime);

                    session.insert("com.github.monster.MockMapper.mockArticles", seed);

                    System.out.println("还剩：" + count);
                }

                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }
}
