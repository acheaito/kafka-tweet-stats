package com.cheaito.twitter.domain;

public class Tweet {
    private final String id;
    private final String lang;
    private final String text;

    public Tweet(String id, String lang, String text) {
        this.id = id;
        this.lang = lang;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }

    public String getText() {
        return text;
    }
}
