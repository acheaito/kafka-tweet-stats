package com.cheaito.twitter.model;

public class Url {
    private final String url;
    private final int start;
    private final int end;
    private final String expanded_url;
    private final String display_url;

    public Url(String url, int start, int end, String expanded_url, String display_url) {
        this.url = url;
        this.start = start;
        this.end = end;
        this.expanded_url = expanded_url;
        this.display_url = display_url;
    }

    @Deprecated
    private Url() {
        url = null;
        start = 0;
        end = 0;
        expanded_url = null;
        display_url = null;
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

    public String getExpanded_url() {
        return expanded_url;
    }

    public String getDisplay_url() {
        return display_url;
    }
}
