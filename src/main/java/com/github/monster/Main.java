package com.github.monster;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Main {
    private static final int READY = 0;
    private static final int PROCESSED = 1;

    private static final String JDBC_URL = "jdbc:h2:file:/Users/home/workspace/j-crawler/news";
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "123456";

    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection(JDBC_URL, USER_NAME, PASSWORD);

        String url;
        while ((url = loadOneUrl(connection, READY)) != null) {
            // 标记已处理
            updateUrlStatus(connection, url, PROCESSED);

            if (hasProcessed(connection, url) || !isValid(url)) {
                continue;
            }

            // 开始处理
            Document document = parsePage(url);

            List<String> pageUrls = getPageUrls(document);
            for (String pageUrl : pageUrls) {
                if (isValid(pageUrl) && !isUrlExist(connection, pageUrl)) {
                    insertUrl(connection, pageUrl);
                }
            }
        }
    }

    private static boolean hasProcessed(Connection connection, String url) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select * from urls where url = ? and status = 1")) {
            statement.setString(1, url);

            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    private static boolean isUrlExist(Connection connection, String url) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select * from urls where url = ?")) {
            statement.setString(1, url);

            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    private static void updateUrlStatus(Connection connection, String url, int status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("update urls set status = ?  where url = ?")) {
            statement.setInt(1, status);
            statement.setString(2, url);

            statement.execute();
        }
    }

    private static void insertUrl(Connection connection, String url) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into urls (url, status) values(?, ?)")) {
            statement.setString(1, url);
            statement.setInt(2, READY);

            statement.execute();
        }
    }

    private static String loadOneUrl(Connection connection, int status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select url from urls where status = ? limit 1");) {
            statement.setInt(1, status);

            ResultSet resultSet = statement.executeQuery();

            return resultSet.next() ? resultSet.getString(1) : null;
        }
    }

    private static List<String> getPageUrls(Document document) {
        List<String> pageUrls = new ArrayList<>();

        document.select("a").stream()
                .map(aTag -> aTag.attr("href"))
                .forEach(pageUrls::add);

        storeArticle(document);

        return pageUrls;
    }

    private static void storeArticle(Document document) {
        Elements articleTags = document.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                System.out.println("已收录文章：" + title);
            }
        }
    }

    private static Document parsePage(String url) throws IOException {
        System.out.println("正在爬取：" + url);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36");

        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();

            String html = EntityUtils.toString(entity);

            return Jsoup.parse(html);
        }
    }

    private static boolean isValid(String url) {
        return (isNewsPage(url) || isIndexPage(url)) && isNotLoginPage(url);
    }

    private static boolean isIndexPage(String url) {
        return "https://sina.cn".equals(url);
    }

    private static boolean isNewsPage(String url) {
        return url.contains("news.sina.cn");
    }

    private static boolean isNotLoginPage(String url) {
        return !url.contains("passport.sina.cn");
    }
}
