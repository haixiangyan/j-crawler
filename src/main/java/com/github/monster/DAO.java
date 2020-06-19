package com.github.monster;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

import static com.github.monster.Main.READY;

public class DAO {
    public static String loadOneUrl(Connection connection, int status) throws SQLException {
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

    public static void storeArticle(Connection connection, Document document, String url) throws SQLException {
        Elements articleTags = document.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                String content = articleTag.select("p")
                        .stream()
                        .map(Element::text)
                        .collect(Collectors.joining("\n"));

                System.out.println("已收录文章：" + title);
                insertArticle(connection, title, content, url);
            }
        }
    }

    public static void insertArticle(Connection connection, String title, String content, String url) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into news (TITLE, CONTENT, URL, CREATED_AT, UPDATED_AT) values (?, ?, ?, now(), now())")) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, url);

            statement.execute();
        }
    }

    public static void insertUrl(Connection connection, String url) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into urls (url, status) values(?, ?)")) {
            statement.setString(1, url);
            statement.setInt(2, READY);

            statement.execute();
        }
    }

    public static void updateUrlStatus(Connection connection, String url, int status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("update urls set status = ?  where url = ?")) {
            statement.setInt(1, status);
            statement.setString(2, url);

            statement.execute();
        }
    }

    public static boolean isUrlExist(Connection connection, String url) throws SQLException {
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

    public static boolean hasProcessed(Connection connection, String url) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select * from urls where url = ? and status = 1")) {
            statement.setString(1, url);

            resultSet = statement.executeQuery();
            return resultSet.next();
        } finally {
            if (resultSet != null) {
                resultSet.close();
                ;
            }
        }
    }
}
