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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Crawler extends Thread {
    public static final int READY = 0;
    public static final int PROCESSED = 1;

    private final CrawlerDao dao;

    public Crawler(CrawlerDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        try {
            String url;
            while ((url = dao.loadOneUrl(READY)) != null) {
                if (dao.hasProcessed(url) || !isValid(url)) {
                    continue;
                }

                // 标记已处理
                dao.updateUrlStatus(url, PROCESSED);

                // 开始处理
                Document document = parsePage(url);

                List<String> pageUrls = getPageUrls(document, url);
                for (String pageUrl : pageUrls) {
                    if (pageUrl.startsWith("//")) {
                        pageUrl = "https:" + pageUrl;
                    }

                    if (isValid(pageUrl) && !dao.isUrlExist(pageUrl) && !pageUrl.toLowerCase().contains("javascript")) {
                        dao.insertUrl(pageUrl);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void storeArticle(Document document, String url) throws SQLException {
        Elements articleTags = document.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                String content = articleTag.select("p")
                        .stream()
                        .map(Element::text)
                        .collect(Collectors.joining("\n"));

                System.out.println("已收录文章：" + title);
                dao.insertArticle(title, content, url);
            }
        }
    }

    private List<String> getPageUrls(Document document, String url) throws SQLException {
        List<String> pageUrls = new ArrayList<>();

        document.select("a").stream()
                .map(aTag -> aTag.attr("href"))
                .forEach(pageUrls::add);

        storeArticle(document, url);

        return pageUrls;
    }

    private Document parsePage(String url) throws IOException {
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

    private boolean isValid(String url) {
        return (isNewsPage(url) || isIndexPage(url)) && isNotLoginPage(url);
    }

    private boolean isIndexPage(String url) {
        return "https://sina.cn".equals(url);
    }

    private boolean isNewsPage(String url) {
        return url.contains("news.sina.cn");
    }

    private boolean isNotLoginPage(String url) {
        return !url.contains("passport.sina.cn");
    }
}
