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
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // 待处理
        Queue<String> urlPool = new LinkedList<>();
        // 已处理
        Set<String> processedUrls = new HashSet<>();

        urlPool.add("https://sina.cn");

        while (!urlPool.isEmpty()) {
            String url = urlPool.poll();

            if (processedUrls.contains(url)) {
                continue;
            }

            if (!url.contains("sina.cn") || url.contains("passport.sina.cn")) {
                continue;
            }

            // 开始处理
            System.out.println("正在爬取：" + url);
            String html = getIndexPage(url);
            List<String> pageUrls = getPageUrls(html);
            urlPool.addAll(pageUrls);

            // 标记成已处理
            processedUrls.add(url);
        }
    }

    private static List<String> getPageUrls(String html) {
        List<String> pageUrls = new ArrayList<>();

        Document document = Jsoup.parse(html);
        Elements aTags = document.select("a");

        for (Element aTag : aTags) {
            pageUrls.add(aTag.attr("href"));
        }

        Elements articleTags = document.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                System.out.println("已收录文章：" + title);
            }
        }

        return pageUrls;
    }

    private static String getIndexPage(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36");

        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();

            return EntityUtils.toString(entity);
        }
    }
}
