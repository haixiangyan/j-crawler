package com.github.monster;

import java.time.Instant;

public class Article {
    private Integer id;
    private String title;
    private String content;
    private String url;
    private Instant createdAt;
    private Instant updatedAt;

    public Article() {}

    public Article(String title, String content, String url) {
        this.title = title;
        this.content = content;
        this.url = url;
    }

    public Article(Article target) {
        this.id = target.id;
        this.title = target.title;
        this.content = target.content;
        this.url = target.url;
        this.createdAt = target.createdAt;
        this.updatedAt = target.updatedAt;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
