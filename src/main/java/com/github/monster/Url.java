package com.github.monster;

public class Url {
    private String url;
    private int status;

    public Url(String url, int status) {
        this.url = url;
        this.status = status;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public int getStatus() {
        return status;
    }
}
