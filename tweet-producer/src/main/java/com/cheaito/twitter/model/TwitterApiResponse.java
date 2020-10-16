package com.cheaito.twitter.model;

public class TwitterApiResponse {
    public final Tweet data;

    public TwitterApiResponse(Tweet data) {
        this.data = data;
    }

    public Tweet getData() {
        return data;
    }
}
