package com.cheaito.twitter.model;

import com.cheaito.twitter.domain.Tweet;

public class TwitterApiResponse {
    public final Tweet data;

    public TwitterApiResponse(Tweet data) {
        this.data = data;
    }

    public Tweet getData() {
        return data;
    }
}
