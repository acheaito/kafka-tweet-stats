package com.cheaito.twitter.model;

public class Url {
    private final String url;
    private final int start;
    private final int end;

    public Url(String url, int start, int end) {
        this.url = url;
        this.start = start;
        this.end = end;
    }

    @Deprecated
    private Url() {
        url = null;
        start = 0;
        end = 0;
    }

    public String getUrl() {
        return url;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
