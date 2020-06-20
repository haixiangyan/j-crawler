package com.github.monster;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SearchEngine {
    public static void main(String[] args) throws IOException {
        while (true) {
            System.out.print("\n输入关键字：");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String keyword = reader.readLine();

            if (keyword.equals("exit")) {
                break;
            } else {
                SearchHits result = search(keyword);

                printResult(result);
            }
        }
    }

    private static SearchHits search(String keyword) throws IOException {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            SearchRequest request = new SearchRequest("articles");

            request.source(new SearchSourceBuilder().query(new MultiMatchQueryBuilder(keyword, "title", "content")));

            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            return response.getHits();
        }
    }

    private static void printResult(SearchHits hits) {
        hits.forEach(hit -> System.out.println(hit.getSourceAsString()));
    }
}
