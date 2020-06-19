package com.github.monster;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.github.monster.DAO.*;

public class Main {
    public static final int READY = 0;
    public static final int PROCESSED = 1;

    private static final String JDBC_URL = "jdbc:h2:file:/Users/home/workspace/j-crawler/news";
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "123456";

    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection(JDBC_URL, USER_NAME, PASSWORD);

        String url;
        while ((url = loadOneUrl(connection, READY)) != null) {
            if (hasProcessed(connection, url) || !isValid(url)) {
                continue;
            }

            // 标记已处理
            updateUrlStatus(connection, url, PROCESSED);

            // 开始处理
            Document document = parsePage(url);

            List<String> pageUrls = getPageUrls(connection, document, url);
            for (String pageUrl : pageUrls) {
                if (pageUrl.startsWith("//")) {
                    pageUrl = "https:" + pageUrl;
                }

                if (isValid(pageUrl) && !isUrlExist(connection, pageUrl) && !pageUrl.toLowerCase().contains("javascript")) {
                    insertUrl(connection, pageUrl);
                }
            }
        }
    }

    private static List<String> getPageUrls(Connection connection, Document document, String url) throws SQLException {
        List<String> pageUrls = new ArrayList<>();

        document.select("a").stream()
                .map(aTag -> aTag.attr("href"))
                .forEach(pageUrls::add);

        storeArticle(connection, document, url);

        return pageUrls;
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
