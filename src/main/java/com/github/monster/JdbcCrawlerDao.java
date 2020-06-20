package com.github.monster;

import java.sql.*;

import static com.github.monster.Crawler.READY;

public class JdbcCrawlerDao implements CrawlerDao{
    private static final String JDBC_URL = "jdbc:h2:file:/Users/home/workspace/j-crawler/sina";
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "123456";

    private final Connection connection;

    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection(JDBC_URL, USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String loadOneUrl(int status) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select url from urls where status = ? limit 1")) {
            statement.setInt(1, status);

            resultSet = statement.executeQuery();

            return resultSet.next() ? resultSet.getString(1) : null;
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    @Override
    public void insertArticle(String title, String content, String url) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into articles (TITLE, CONTENT, URL, CREATED_AT, UPDATED_AT) values (?, ?, ?, now(), now())")) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, url);

            statement.execute();
        }
    }

    @Override
    public void insertUrl(String url) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into urls (url, status) values(?, ?)")) {
            statement.setString(1, url);
            statement.setInt(2, READY);

            statement.execute();
        }
    }

    @Override
    public void updateUrlStatus(String url, int status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("update urls set status = ?  where url = ?")) {
            statement.setInt(1, status);
            statement.setString(2, url);

            statement.execute();
        }
    }

    @Override
    public boolean isUrlExist(String url) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select * from urls where url = ?")) {
            statement.setString(1, url);

            resultSet = statement.executeQuery();
            return resultSet.next();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    @Override
    public boolean hasProcessed(String url) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select * from urls where url = ? and status = 1")) {
            statement.setString(1, url);

            resultSet = statement.executeQuery();
            return resultSet.next();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }
}
